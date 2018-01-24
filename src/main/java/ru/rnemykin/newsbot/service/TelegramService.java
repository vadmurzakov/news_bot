package ru.rnemykin.newsbot.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendPhoto;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.rnemykin.newsbot.config.factory.ChatAdminsFactory;
import ru.rnemykin.newsbot.config.properties.ChatAdmin;
import ru.rnemykin.newsbot.model.Keyboard;
import ru.rnemykin.newsbot.model.ModerateMessage;
import ru.rnemykin.newsbot.model.Post;
import ru.rnemykin.newsbot.model.enums.ModerationStatusEnum;
import ru.rnemykin.newsbot.model.enums.PostStatusEnum;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static ru.rnemykin.newsbot.model.enums.ModerationStatusEnum.ACCEPT;
import static ru.rnemykin.newsbot.model.enums.ModerationStatusEnum.REJECT;

@Slf4j
@Service
public class TelegramService {

    private final MessageFormatter messageFormatter;
    private final PostService postService;
    private final TelegramBot client;
    private final ChatAdminsFactory chatAdminsFactory;
    private final ModerateMessageService moderateMessageService;

    @Autowired
    public TelegramService(MessageFormatter msgFormatter,
                           PostService postService,
                           TelegramBot client,
                           ChatAdminsFactory chatAdminsFactory,
                           ModerateMessageService moderateMessageService) {
        this.messageFormatter = msgFormatter;
        this.postService = postService;
        this.client = client;
        this.chatAdminsFactory = chatAdminsFactory;
        this.moderateMessageService = moderateMessageService;
    }

    public boolean sendPhoto(Integer chatId, String urlPhoto, String caption) {
		SendPhoto request = new SendPhoto(186736203, urlPhoto)
				.caption(caption)
				.replyMarkup(Keyboard.DEFAULT);
		return client.execute(request).isOk();
	}

	public boolean sendMessage(Post post, Integer chatId) {
    	return sendMessage(post, chatId, null);
	}

    public boolean sendMessage(Post post, Integer chatId, InlineKeyboardMarkup keyboard) {
    	assertNotNull(chatId);

        SendMessage request = new SendMessage(chatId, messageFormatter.format(post))
                .parseMode(ParseMode.HTML)
				.replyMarkup(keyboard)
                .disableWebPagePreview(false);

        return execute(request, chatId, post);
    }

	/**
	 * Исполняет запросы всех типов
	 * @param request new SendMessage, SendPhoto, SendVideo and etc
	 * @param chatId ид чата
	 * @param post новость
	 * @return true - если все хорошо, false - в противном случае
	 */
	private <T extends BaseRequest, R extends SendResponse> boolean execute(BaseRequest<T, R> request, Integer chatId, Post post) {
		SendResponse response = client.execute(request);
		if (response.isOk()) {
			log.info("Sending postId={} for {}", post.getId(), chatId); //todo[vmurzakov] заменить chatId на админа или название паблика

			ModerateMessage msg = ModerateMessage.builder()
					.postId(post.getId())
					.adminId(chatId)
					.telegramMessageId(response.message().messageId())
					.build();

			moderateMessageService.save(msg);
			return true;
		} else {
			log.error("Error sending postId={} for {}", post.getId(), chatId); //todo[vmurzakov] заменить chatId на админа или название паблика
			return false;
		}
	}

    /**
     * 1. Сообщение с Новостью редактируется для всех админов
     * 2. Убирается клавиатура
     * 3. Вначале сообщения прописывается статус, который был присвоен Новости
     * Если один из админов промодерировал Новость, другие админы это увидят и не смогут промодерировать Новость
     *
     * @param callbackQuery - событие, которое срабатывает при нажатии на клавиатуру
     */
    public void processPressKeyboardInline(CallbackQuery callbackQuery) {
        Integer actorId = callbackQuery.from().id();
        ModerateMessage msg = moderateMessageService.findByTlgrmIdAndAdminId(callbackQuery.message().messageId(), actorId);

        Post post = msg.getPost();
        ModerationStatusEnum moderationStatus = ModerationStatusEnum.from(callbackQuery.data());
        if (moderationStatus == ACCEPT) {
            msg.getPost().setStatus(PostStatusEnum.MODERATED);
        } else if (moderationStatus == REJECT) {
            msg.getPost().setCancelDate(LocalDateTime.now());
            msg.getPost().setStatus(PostStatusEnum.CANCELED);
        }
        postService.save(post);

        Long postId = post.getId();
        List<ChatAdmin> chatAdmins = chatAdminsFactory.findAll(post.getCity());
        List<ModerateMessage> editMessages = chatAdmins.stream()
                .filter(a -> a.getId() != actorId)
                .map(a -> moderateMessageService.findByPostIdAndAdminId(postId, a.getId()))
                .collect(toList());

        editMessages.add(msg);
        editMessages.forEach(m -> {
            client.execute(makeEditMessage(callbackQuery, m.getAdminId(), m.getTelegramMessageId()));
            m.setProcessedStatus(moderationStatus);
            m.setProcessedTime(LocalDateTime.now());
            moderateMessageService.save(m);
        });

        log.info("{} moderated postId={} with status {}", callbackQuery.from().username(), post.getId(), callbackQuery.data());
    }

    private EditMessageText makeEditMessage(CallbackQuery callbackQuery, Integer chatId, Integer messageId) {
        log.info("Edit messageId={} for {}", messageId, chatAdminsFactory.findById(chatId).getName());
        return new EditMessageText(
                chatId,
                messageId,
				messageFormatter.format(callbackQuery)
        ).parseMode(ParseMode.Markdown).disableWebPagePreview(true);
    }

}
