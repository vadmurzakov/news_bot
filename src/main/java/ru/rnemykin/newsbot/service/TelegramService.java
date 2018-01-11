package ru.rnemykin.newsbot.service;

import com.google.common.collect.Iterables;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
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
import ru.rnemykin.newsbot.model.Post;
import ru.rnemykin.newsbot.model.enums.AdminEnum;
import ru.rnemykin.newsbot.model.enums.ModerationStatusEnum;
import ru.rnemykin.newsbot.model.enums.PostStatusEnum;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static ru.rnemykin.newsbot.model.enums.ModerationStatusEnum.ACCEPT;
import static ru.rnemykin.newsbot.model.enums.ModerationStatusEnum.DEFER;
import static ru.rnemykin.newsbot.model.enums.ModerationStatusEnum.REJECT;

@Service
@Slf4j
public class TelegramService {
    private int offset = 0;
    private Keyboard keyboard = new Keyboard();

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
	private void processNewsModeration () {
        List<Update> updates = getUpdates(offset);
        for (Update update : updates) {
            if (update.message() != null) {
                Message message = update.message();
                log.info(message.toString());
                sendMessageToGroupAdmins("hello world"); //todo[vmurzakov]: сюда передавать текст новости
            } else if (update.callbackQuery() != null) {
                log.info(update.callbackQuery().toString());
                keyboard.processPressKeyboardInline(update.callbackQuery());
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
					.replyMarkup(keyboard.DEFAULT);

			SendResponse sendResponse = client.execute(request);
			if (!sendResponse.isOk()) {
				log.error("Error send message: " + sendResponse.toString());
			}
		});
	}

    public boolean sendMessageToChannel(Post post) {
        String chatId = telegramProperties.getCityChatId().get(post.getCity());
        SendMessage request = new SendMessage(chatId, messageFormatter.format(post))
                .parseMode(ParseMode.HTML)
                .disableWebPagePreview(true);

        SendResponse execute = client.execute(request);
        if(!execute.isOk()) {
            log.error("error while send message to chat id={}, postId={}", chatId, post.getId());
        }
        return execute.isOk();
    }


    private class Keyboard {
        final InlineKeyboardMarkup DEFAULT = new InlineKeyboardMarkup(
                new InlineKeyboardButton[]{
                        new InlineKeyboardButton(ACCEPT.value()).callbackData(ACCEPT.name()),
                        new InlineKeyboardButton(REJECT.value()).callbackData(REJECT.name()),
                        new InlineKeyboardButton(DEFER.value()).callbackData(DEFER.name())
                });

        /**
         * Обрабатывает нажатие inlineKeyboard:
         * 1. Сообщение с Новостью редактируется для всех админов
         * 2. Убирается клавиатура
         * 3. Вначале сообщения прописывается статус, который был присвоен Новости
         * Если один из админов промодерировал Новость, другие админы это увидят и не смогут промодерировать Новость
         *
         * @param callbackQuery - событие, которое срабатывает при нажатии на клавиатуру
         */
        private void processPressKeyboardInline(CallbackQuery callbackQuery) {
            Arrays.stream(AdminEnum.values()).forEach(adminEnum -> {
                BaseResponse response = client.execute(makeEditMessage(callbackQuery, adminEnum));
                if(!response.isOk()) {
                    log.error("Error press inlineKeyboard: " + response.toString());
                    return;
                }

                log.info("{} moderated post with status {}", adminEnum.name(), callbackQuery.data());

                Post post = postService.findByText(callbackQuery.message().text());
                ModerationStatusEnum moderationStatus = ModerationStatusEnum.from(callbackQuery.data());
                if(moderationStatus == ACCEPT) {
                    post.setStatus(PostStatusEnum.MODERATED);
                } else if(moderationStatus == REJECT) {
                    post.setStatus(PostStatusEnum.CANCELED);
                }
                postService.save(post);
            });
        }

        private EditMessageText makeEditMessage(CallbackQuery callbackQuery, AdminEnum adminEnum) {
            EditMessageText editMessageText = new EditMessageText(
                    adminEnum.id(),
                    callbackQuery.message().messageId() + adminEnum.offset(),
                    "`" + callbackQuery.data() + "` \n" + callbackQuery.message().text()
            );

            return editMessageText
                    .parseMode(ParseMode.Markdown)
                    .disableWebPagePreview(true);
        }
    }
}
