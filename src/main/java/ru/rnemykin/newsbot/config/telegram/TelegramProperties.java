package ru.rnemykin.newsbot.config.telegram;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import ru.rnemykin.newsbot.model.enums.CityEnum;

import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "telegram")
public class TelegramProperties {
	private String token;
	private Map<CityEnum, String> cityChatId;
}
