package ru.rnemykin.newsbot.config.telegram;

import com.pengrad.telegrambot.TelegramBot;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Data
@Component
@Configuration
public class TelegramConfig {

	private final TelegramProperties properties;

	@Autowired
	public TelegramConfig(TelegramProperties properties) {
		this.properties = properties;
	}

	@Bean(name = "telegramClient")
	public TelegramBot getClient() {
		return new TelegramBot(properties.getToken());
	}
}