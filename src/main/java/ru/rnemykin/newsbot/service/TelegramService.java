package ru.rnemykin.newsbot.service;

import com.google.common.collect.Iterables;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.GetUpdates;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.GetUpdatesResponse;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.rnemykin.newsbot.config.factory.ChatAdminsFactory;
import ru.rnemykin.newsbot.config.properties.ChatAdmin;
import ru.rnemykin.newsbot.config.telegram.TelegramProperties;
import ru.rnemykin.newsbot.model.Keyboard;
import ru.rnemykin.newsbot.model.ModerateMessage;
import ru.rnemykin.newsbot.model.Post;
import ru.rnemykin.newsbot.model.enums.ModerationStatusEnum;
import ru.rnemykin.newsbot.model.enums.PostStatusEnum;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static ru.rnemykin.newsbot.model.enums.ModerationStatusEnum.ACCEPT;
import static ru.rnemykin.newsbot.model.enums.ModerationStatusEnum.REJECT;

@Slf4j
@Service
public class TelegramService {
    private int offset = 0;

    private final MessageFormatter messageFormatter;
    private final TelegramProperties telegramProperties;
    private final PostService postService;
    private final TelegramBot client;
    private final ChatAdminsFactory chatAdminsFactory;
    private final ModerateMessageService moderateMessageService;

    @Autowired
    public TelegramService(MessageFormatter msgFormatter,
                           TelegramProperties tgrmProperties,
                           PostService postService,
                           TelegramBot client,
                           ChatAdminsFactory chatAdminsFactory,
                           ModerateMessageService moderateMessageService) {
        this.messageFormatter = msgFormatter;
        this.telegramProperties = tgrmProperties;
        this.postService = postService;
        this.client = client;
        this.chatAdminsFactory = chatAdminsFactory;
        this.moderateMessageService = moderateMessageService;

        List<Update> updates = getUpdates(offset);
        setOffset(updates);
    }


    @Scheduled(fixedDelayString = "${telegram.processNewsModeration.interval}")
    private void processNewsModeration() {
        List<Update> updates = getUpdates(offset);
        for (Update update : updates) {
            if (update.message() != null) {
                Message message = update.message();
                log.info(message.toString());
            } else if (update.callbackQuery() != null) {
                log.info(update.callbackQuery().toString());
                processPressKeyboardInline(update.callbackQuery());
            }
        }
    }

    private List<Update> getUpdates(int offset) {
        GetUpdates getUpdates = new GetUpdates().offset(offset + 1);
        GetUpdatesResponse response = client.execute(getUpdates);

        if (!response.isOk()) {
            log.error("Error get updates: " + response.toString());
            return Collections.emptyList();
        }

        List<Update> updates = response.updates();
        setOffset(updates);

        return updates.isEmpty() ? Collections.emptyList() : updates;
    }

    private void setOffset(List<Update> updates) {
        Update last = Iterables.getLast(updates, null);
        this.offset = last == null ? 0 : last.updateId();
    }

    public void sendMessageToGroupAdmins(Post post) {
        List<ChatAdmin> cityAdmins = chatAdminsFactory.findAll(post.getCity());
        cityAdmins.forEach(adminEnum -> {
            SendMessage request = new SendMessage(adminEnum.getId(), post.getTextAsString())
                    .parseMode(ParseMode.HTML)
                    .replyMarkup(Keyboard.DEFAULT)
                    .disableWebPagePreview(false);

            SendResponse sendResponse = client.execute(request);
            if (sendResponse.isOk()) {
                log.info("Send news in chat for {}, telegramMessageId={}", adminEnum.getName(), sendResponse.message().messageId());
                ModerateMessage msg = ModerateMessage.builder()
                        .postId(post.getId())
                        .adminId(adminEnum.getId())
                        .telegramMessageId(sendResponse.message().messageId())
                        .build();

                moderateMessageService.save(msg);
            } else {
                log.error("Error send message for: " + adminEnum.getName());
            }
        });
    }

    public boolean sendMessageToChannel(Post post) {
        String chatId = telegramProperties.getCityChatId().get(post.getCity());
        SendMessage request = new SendMessage(chatId, messageFormatter.format(post))
                .parseMode(ParseMode.HTML)
                .disableWebPagePreview(false);

        SendResponse execute = client.execute(request);
        if (!execute.isOk()) {
            log.error("error while send message to chat id={}, postId={}", chatId, post.getId());
        }
        return execute.isOk();
    }

    /**
     * 1. Сообщение с Новостью редактируется для всех админов
     * 2. Убирается клавиатура
     * 3. Вначале сообщения прописывается статус, который был присвоен Новости
     * Если один из админов промодерировал Новость, другие админы это увидят и не смогут промодерировать Новость
     *
     * @param callbackQuery - событие, которое срабатывает при нажатии на клавиатуру
     */
    private void processPressKeyboardInline(CallbackQuery callbackQuery) {
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
        log.info("Edit message {} for adminId = {}", messageId, chatId);
        return new EditMessageText(
                chatId,
                messageId,
                "`" + callbackQuery.data() + " by " + callbackQuery.from().username() + "` \n" + callbackQuery.message().text()
        ).parseMode(ParseMode.Markdown).disableWebPagePreview(true);
    }

}
