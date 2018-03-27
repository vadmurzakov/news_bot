package ru.newsbot.model;

import com.pengrad.telegrambot.model.request.Keyboard;
import com.pengrad.telegrambot.request.AbstractMultipartRequest;

import java.io.File;

public class SendPhoto extends AbstractMultipartRequest<SendPhoto> {
	public SendPhoto(Object chatId, String photo) {
		super(chatId, photo);
	}

	public SendPhoto(Object chatId, File photo) {
		super(chatId, photo);
	}

	public SendPhoto(Object chatId, byte[] photo) {
		super(chatId, photo);
	}

	public SendPhoto caption(String caption) {
		return this.add("caption", caption);
	}

	@Override
	public SendPhoto replyMarkup(Keyboard replyMarkup) {
		if (replyMarkup == null) {
			return this;
		}
		return super.replyMarkup(replyMarkup);
	}

	protected String getFileParamName() {
		return "photo";
	}

	public String getDefaultFileName() {
		return "file.jpg";
	}

	public String getContentType() {
		return "image/jpeg";
	}
}
