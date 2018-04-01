package ru.newsbot.config.telegram;

import com.pengrad.telegrambot.TelegramBot;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AllArgsConstructor
public class TelegramConfig {
	private final TelegramProperties properties;

	@Bean(name = "telegramClient")
	public TelegramBot telegramClient() {
		return new TelegramBot(properties.getToken());
	}
}