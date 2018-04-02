package ru.newsbot.model;

import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;

import static ru.newsbot.model.enums.ModerationStatusEnum.ACCEPT;
import static ru.newsbot.model.enums.ModerationStatusEnum.REJECT;

public class Keyboard {
	public final static InlineKeyboardMarkup MODERATION = new InlineKeyboardMarkup(
			new InlineKeyboardButton[]{
					new InlineKeyboardButton(ACCEPT.value()).callbackData(ACCEPT.name()),
					new InlineKeyboardButton(REJECT.value()).callbackData(REJECT.name())
			});

	public final static InlineKeyboardMarkup DEFAULT = new InlineKeyboardMarkup();
}
