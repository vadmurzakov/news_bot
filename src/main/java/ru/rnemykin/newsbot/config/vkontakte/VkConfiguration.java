package ru.rnemykin.newsbot.config.vkontakte;

import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import lombok.Data;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Data
@Component
@Configuration
public class VkConfiguration {

	private final VkProperties properties;

	@Autowired
	public VkConfiguration(VkProperties properties) {
		this.properties = properties;
	}

	@Bean
	@SneakyThrows
	public VkApiClient getClient() {
		TransportClient transportClient = new HttpTransportClient();
		return new VkApiClient(transportClient);
	}

}
