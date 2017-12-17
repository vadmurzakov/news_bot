package ru.rnemykin.newsbot.service;

import com.google.common.collect.Iterables;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.GetUpdates;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.GetUpdatesResponse;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.rnemykin.newsbot.config.telegram.TelegramConfig;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class TelegramService {

	@Autowired private TelegramConfig telegramConfig;

	private int offset = 0;
	private TelegramBot client;

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
			Message message = update.message();
			log.info(message.toString());
			//todo[vmurzakov]: arguments
			sendMessage(message.chat().id(), "hello world");
		}
	}

	public void sendMessage(Long chatId, String text) {
		SendMessage request = new SendMessage(chatId, text)
				.parseMode(ParseMode.HTML);

		SendResponse sendResponse = client.execute(request);
		if (!sendResponse.isOk()) {
			log.error("Error send message: " + sendResponse.toString());
		}
	}
}
