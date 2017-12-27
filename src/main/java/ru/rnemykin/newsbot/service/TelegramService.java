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
import ru.rnemykin.newsbot.config.telegram.TelegramConfig;
import ru.rnemykin.newsbot.model.enums.AdminEnum;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static ru.rnemykin.newsbot.model.enums.ModerationStatusEnum.*;

@Service
@Slf4j
public class TelegramService {

	@Autowired
	private TelegramConfig telegramConfig;

	private int offset = 0;
	private TelegramBot client;

	private static class Keyboard {
		public final static InlineKeyboardMarkup DEFAULT = new InlineKeyboardMarkup(
				new InlineKeyboardButton[]{
						new InlineKeyboardButton(ACCEPT.value()).callbackData(ACCEPT.name()),
						new InlineKeyboardButton(REJECT.value()).callbackData(REJECT.name()),
						new InlineKeyboardButton(DEFER.value()).callbackData(DEFER.name())
				});
	}

	@PostConstruct
	private void init() {
		client = telegramConfig.getClient();
		List<Update> updates = getUpdates(offset);
		setOffset(updates);
	}

	@Scheduled(fixedDelay = 5000)
	private void getUpdates() {
		exec(getUpdates(offset));
	}

	public List<Update> getUpdates(int offset) {
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

	private void exec(List<Update> updates) {
		for (Update update : updates) {
			if (update.message() != null) {
				Message message = update.message();
				log.info(message.toString());
				sendMessage("hello world"); //todo[vmurzakov]: сюда передавать текст новости
			} else if (update.callbackQuery() != null) {
				log.info(update.callbackQuery().toString());
				processPressKeyboardInline(update.callbackQuery());
			}

		}
	}

	/**
	 * Обрабатывает нажатие inlineKeyboard:
	 * 1. Сообщение с Новостью редактируется для всех админов
	 * 2. Убирается клавиатура
	 * 3. Вначале сообщения прописывается статус, который был присвоен Новости
	 * Если один из админов промодерировал Новость, другие админы это увидят и не смогут промодерировать Новость
	 *
	 * @param callbackQuery - событие, которое срабатывает при нажатии на клавиатуру
	 */
	public void processPressKeyboardInline(CallbackQuery callbackQuery) {
		Arrays.stream(AdminEnum.values()).forEach(adminEnum -> {
			Long chatId = adminEnum.id();
			Integer messageId = callbackQuery.message().messageId();
			EditMessageText editMessageText = new EditMessageText(chatId, messageId + adminEnum.offset(), "`" + callbackQuery.data() + "` \n" + callbackQuery.message().text())
					.parseMode(ParseMode.Markdown)
					.disableWebPagePreview(true);
			BaseResponse response = client.execute(editMessageText);
			if (response.isOk()) {
				log.info("{} moderated post with status {}", adminEnum.name(), callbackQuery.data());
			} else {
				log.error("Error press inlineKeyboard: " + response.toString());
			}
		});
	}

	/**
	 * Отправляет сообщение всем админам (например Новость на модерацию)
	 *
	 * @param text текст сообщения
	 */
	public void sendMessage(String text) {
		Arrays.stream(AdminEnum.values()).forEach(adminEnum -> {
			SendMessage request = new SendMessage(adminEnum.id(), text)
					.parseMode(ParseMode.HTML)
					.replyMarkup(Keyboard.DEFAULT);

			SendResponse sendResponse = client.execute(request);
			if (!sendResponse.isOk()) {
				log.error("Error send message: " + sendResponse.toString());
			}
		});
	}

}
