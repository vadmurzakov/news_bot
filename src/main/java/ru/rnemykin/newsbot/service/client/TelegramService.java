package ru.rnemykin.newsbot.service.client;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.DeleteMessage;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.response.BaseResponse;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.rnemykin.newsbot.config.factory.ChatAdminsFactory;
import ru.rnemykin.newsbot.config.properties.ChatAdmin;
import ru.rnemykin.newsbot.config.telegram.TelegramProperties;
import ru.rnemykin.newsbot.model.ModerateMessage;
import ru.rnemykin.newsbot.model.Post;
import ru.rnemykin.newsbot.model.SendMessage;
import ru.rnemykin.newsbot.model.SendPhoto;
import ru.rnemykin.newsbot.model.enums.ModerationStatusEnum;
import ru.rnemykin.newsbot.model.enums.PostStatusEnum;
import ru.rnemykin.newsbot.service.MessageFormatter;
import ru.rnemykin.newsbot.service.impl.ModerateMessageService;
import ru.rnemykin.newsbot.service.impl.PostService;

import javax.annotation.Nullable;
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
    private final TelegramProperties telegramProperties;

    @Autowired
    public TelegramService(MessageFormatter msgFormatter,
                           PostService postService,
                           TelegramBot client,
                           ChatAdminsFactory chatAdminsFactory,
                           ModerateMessageService moderateMessageService,
                           TelegramProperties telegramProperties) {
        this.messageFormatter = msgFormatter;
        this.postService = postService;
        this.client = client;
        this.chatAdminsFactory = chatAdminsFactory;
        this.moderateMessageService = moderateMessageService;
        this.telegramProperties = telegramProperties;
    }


    public SendResponse sendPhoto(Object chatId, String urlPhoto, String caption, @Nullable InlineKeyboardMarkup keyboard) {
		assertNotNull(chatId, "chatId can not be null");

		SendPhoto request = new SendPhoto(chatId, urlPhoto)
				.caption(caption)
				.replyMarkup(keyboard);

		return execute(request);
	}

	public SendResponse sendMessage(Object chatId, String text, @Nullable Long postId, @Nullable InlineKeyboardMarkup keyboard) {
		assertNotNull(chatId, "chatId can not be null");

		SendMessage request = new SendMessage(chatId, text)
				.withPostId(postId)
				.replyMarkup(keyboard)
				.parseMode(ParseMode.HTML);

		return execute(request);
	}

    public boolean sendMessageToChannel(Post post) {
		SendResponse response;
        String chatId = telegramProperties.getCityChatId().get(post.getCity());

		if (postService.isPostAsPhoto(post)) {
			response = sendPhoto(chatId, post.getPostAttachments().get(0).getUrlPhoto(), post.getTextAsString(), null);
		} else {
			response = sendMessage(chatId, messageFormatter.format(post), post.getId(), null);
		}

        return response.isOk();
    }

    public boolean sendMessageOnModeration(Post post, Integer chatId, InlineKeyboardMarkup keyboard) {
    	SendResponse response;

    	if (postService.isPostAsPhoto(post)) {
			response = sendPhoto(chatId, post.getPostAttachments().get(0).getUrlPhoto(), post.getTextAsString(), keyboard);
		} else {
			response = sendMessage(chatId, messageFormatter.format(post), post.getId(), keyboard);
		}

		ModerateMessage msg = ModerateMessage.builder()
				.postId(post.getId())
				.adminId(chatId)
				.telegramMessageId(response.message().messageId())
				.build();

		moderateMessageService.save(msg);

		return response.isOk();
    }

	private <T extends BaseRequest, R extends BaseResponse> R execute(BaseRequest<T, R> request) {
		R response = client.execute(request);
		if (response.isOk()) {
			log.info("Successfully " + messageFormatter.format(request, response));
		} else {
			log.error("Error " + messageFormatter.format(request, response));
		}
		return response;
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
            execute(new DeleteMessage(m.getAdminId(), m.getTelegramMessageId()));
            m.setProcessedStatus(moderationStatus);
            m.setProcessedTime(LocalDateTime.now());
            moderateMessageService.save(m);
        });

        log.info("{} moderated postId={} with status {}", callbackQuery.from().username(), post.getId(), callbackQuery.data());
    }

    private EditMessageText makeEditMessage(CallbackQuery callbackQuery, Integer chatId, Integer messageId) {
        return new EditMessageText(
                chatId,
                messageId,
				messageFormatter.format(callbackQuery)
        ).parseMode(ParseMode.Markdown).disableWebPagePreview(true);
    }

}
