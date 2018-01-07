package ru.rnemykin.newsbot.config.telegram;

import com.pengrad.telegrambot.TelegramBot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TelegramConfig {

	private final TelegramProperties properties;

	@Autowired
	public TelegramConfig(TelegramProperties properties) {
		this.properties = properties;
	}

	@Bean
	public TelegramBot telegramClient() {
		return new TelegramBot(properties.getToken());
	}
}