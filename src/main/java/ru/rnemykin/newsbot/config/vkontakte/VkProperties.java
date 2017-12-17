package ru.rnemykin.newsbot.config.vkontakte;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "vk")
public class VkProperties {
	private Integer appId;
	private String secretKey;
	private String serviceKeyAccess;
}
