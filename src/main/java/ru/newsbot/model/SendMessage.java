package ru.newsbot.model;

import com.pengrad.telegrambot.model.request.Keyboard;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.AbstractSendRequest;

public class SendMessage extends AbstractSendRequest<SendMessage> {
	public SendMessage(Object chatId, String text) {
		super(chatId);
		this.add("text", text);
	}

	public SendMessage parseMode(ParseMode parseMode) {
		return this.add("parse_mode", parseMode.name());
	}

	public SendMessage disableWebPagePreview(boolean disableWebPagePreview) {
		return this.add("disable_web_page_preview", disableWebPagePreview);
	}

	@Override
	public SendMessage replyMarkup(Keyboard replyMarkup) {
		if (replyMarkup == null) {
			return this;
		}
		return super.replyMarkup(replyMarkup);
	}

	public SendMessage withPostId(Long id) {
		return this.add("post_id", id);
	}
}