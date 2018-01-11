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
import com.pengrad.telegrambot.response.BaseResponse;
import com.pengrad.telegrambot.response.GetUpdatesResponse;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.rnemykin.newsbot.config.telegram.TelegramProperties;
import ru.rnemykin.newsbot.model.Keyboard;
import ru.rnemykin.newsbot.model.Post;
import ru.rnemykin.newsbot.model.enums.AdminEnum;
import ru.rnemykin.newsbot.model.enums.ModerationStatusEnum;
import ru.rnemykin.newsbot.model.enums.PostStatusEnum;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static ru.rnemykin.newsbot.model.enums.ModerationStatusEnum.ACCEPT;
import static ru.rnemykin.newsbot.model.enums.ModerationStatusEnum.REJECT;

@Service
@Slf4j
public class TelegramService {
    private int offset = 0;

    private MessageFormatter messageFormatter;
    private TelegramProperties telegramProperties;
    private PostService postService;
    private TelegramBot client;

    @Autowired
    public TelegramService(MessageFormatter msgFormatter, TelegramProperties tgrmProperties, PostService postService, TelegramBot client) {
        this.messageFormatter = msgFormatter;
        this.telegramProperties = tgrmProperties;
        this.postService = postService;
        this.client = client;

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

    public void sendMessageToGroupAdmins(String text) {
        Arrays.stream(AdminEnum.values()).forEach(adminEnum -> {
            SendMessage request = new SendMessage(adminEnum.id(), text)
                    .parseMode(ParseMode.HTML)
                    .replyMarkup(Keyboard.DEFAULT)
                    .disableWebPagePreview(false);

            SendResponse sendResponse = client.execute(request);
            if (!sendResponse.isOk()) {
                log.error("Error send message for: " + adminEnum.name());
            } else {
                log.info("Send news in chat for {}, telegramMessageId={}", adminEnum.name(), sendResponse.message().messageId());
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

    private void processPressKeyboardInline(CallbackQuery callbackQuery) {
        Post post = postService.findByText(callbackQuery.message().text());
        ModerationStatusEnum moderationStatus = ModerationStatusEnum.from(callbackQuery.data());
        if (moderationStatus == ACCEPT) {
            post.setStatus(PostStatusEnum.MODERATED);
        } else if (moderationStatus == REJECT) {
            post.setStatus(PostStatusEnum.CANCELED);
        } else {
            post.setStatus(PostStatusEnum.MODERATION);
        }
        postService.save(post);
        editMessageForAdmins(callbackQuery);
        log.info("{} moderated postId={} with status {}", callbackQuery.from().username(), post.getId(), callbackQuery.data());
    }

    @Deprecated
    private void editMessageForAdmins(CallbackQuery callbackQuery) {
        if (callbackQuery.from().id().equals(AdminEnum.VADMURZAKOV.id())) {
            client.execute(makeEditMessage(callbackQuery, AdminEnum.VADMURZAKOV.id(), callbackQuery.message().messageId()));
            client.execute(makeEditMessage(callbackQuery, AdminEnum.RNEMYKIN.id(), callbackQuery.message().messageId() + 1));
        } else if (callbackQuery.from().id().equals(AdminEnum.RNEMYKIN.id())) {
            client.execute(makeEditMessage(callbackQuery, AdminEnum.VADMURZAKOV.id(), callbackQuery.message().messageId() - 1));
            client.execute(makeEditMessage(callbackQuery, AdminEnum.RNEMYKIN.id(), callbackQuery.message().messageId()));
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
//    private void editMessageForAdmins(CallbackQuery callbackQuery) {
//        int offset = 0;
//        int position = -1;
//        for(AdminEnum adminEnum : AdminEnum.values()) {
//            client.execute(makeEditMessage(callbackQuery, adminEnum.id(),callbackQuery.message().messageId() + offset));
//            if (callbackQuery.from().id().equals(adminEnum.id())) {
//                position *= -1;
//                offset = 0;
//            }
//            offset = (offset * position + 1) * position;
//        }
//    }

    private EditMessageText makeEditMessage(CallbackQuery callbackQuery, Integer chatId, Integer messageId) {
        log.info("Edit message {} for adminId = {}", messageId, chatId);
        return new EditMessageText(
                chatId,
                messageId,
                "`" + callbackQuery.data() + " by " + callbackQuery.from().username() + "` \n" + callbackQuery.message().text()
        ).parseMode(ParseMode.Markdown).disableWebPagePreview(true);
    }

}
