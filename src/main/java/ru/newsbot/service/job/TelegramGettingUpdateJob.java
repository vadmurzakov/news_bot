package ru.newsbot.service.job;

import com.google.common.collect.Iterables;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.GetUpdates;
import com.pengrad.telegrambot.response.GetUpdatesResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.newsbot.service.client.TelegramService;

import java.util.List;

import static java.util.Collections.emptyList;

@Slf4j
@Component
public class TelegramGettingUpdateJob {
	@Value("${job.telegramGettingUpdate.enable}")
	private boolean isEnable;
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

	@Scheduled(cron = "${job.telegramGettingUpdate.interval}")
	private void telegramGettingUpdate() {
		if (isEnable) {
			getUpdates(offset).forEach(update -> {
				if (update.callbackQuery() != null) {
					telegramService.processPressKeyboardInline(update.callbackQuery());
				}
			});
		}
	}

	private List<Update> getUpdates(int offset) {
		GetUpdates getUpdates = new GetUpdates().offset(offset + 1);
		GetUpdatesResponse response = client.execute(getUpdates);

		if (!response.isOk()) {
			log.error("Error get updates: " + response.toString());
			return emptyList();
		}

		List<Update> updates = response.updates();
		setOffset(updates);

		return updates.isEmpty() ? emptyList() : updates;
	}

	private void setOffset(List<Update> updates) {
		Update last = Iterables.getLast(updates, null);
		this.offset = last == null ? 0 : last.updateId();
	}

}
