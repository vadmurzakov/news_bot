package ru.newsbot.config.vkontakte;

import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Data
@Component
@Configuration
@AllArgsConstructor
public class VkConfig {
	private final VkProperties properties;

	@Bean(name = "vkClient")
	@SneakyThrows
	public VkApiClient getClient() {
		TransportClient transportClient = new HttpTransportClient();
		return new VkApiClient(transportClient);
	}

}
