package ru.rnemykin.newsbot.service.job;

import com.google.common.collect.Iterables;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.GetUpdates;
import com.pengrad.telegrambot.response.GetUpdatesResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.rnemykin.newsbot.service.TelegramService;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class TelegramGettingUpdateJob {

	private int offset = 0;

	private TelegramService telegramService;
	private TelegramBot client;

	@Autowired
	public TelegramGettingUpdateJob(TelegramService telegramService, TelegramBot client) {
		this.telegramService = telegramService;
		this.client = client;

		List<Update> updates = getUpdates(offset);
		setOffset(updates);
	}

	@Scheduled(fixedDelayString = "${job.telegramGettingUpdate.interval}")
	private void telegramGettingUpdate() {
		List<Update> updates = getUpdates(offset);
		for (Update update : updates) {
			if (update.message() != null) {
				Message message = update.message();
				log.info(message.toString());
			} else if (update.callbackQuery() != null) {
				log.info(update.callbackQuery().toString());
				telegramService.processPressKeyboardInline(update.callbackQuery());
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

}