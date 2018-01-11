package ru.rnemykin.newsbot.model;

import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;

import static ru.rnemykin.newsbot.model.enums.ModerationStatusEnum.*;

/**
 * Created by vadmurzakov on 11.01.18.
 */
public class Keyboard {
    public final static InlineKeyboardMarkup DEFAULT = new InlineKeyboardMarkup(
            new InlineKeyboardButton[]{
                    new InlineKeyboardButton(ACCEPT.value()).callbackData(ACCEPT.name()),
                    new InlineKeyboardButton(REJECT.value()).callbackData(REJECT.name()),
                    new InlineKeyboardButton(DEFER.value()).callbackData(DEFER.name())
            });
}
