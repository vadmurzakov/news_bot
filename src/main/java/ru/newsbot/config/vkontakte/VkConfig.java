package ru.newsbot.config.vkontakte;

import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.ServiceActor;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Data
@Component
@Configuration
@RequiredArgsConstructor
public class VkConfig {
	private final VkProperties properties;
	private ServiceActor actor;

	@PostConstruct
	private void init() {
		Integer appId = properties.getAppId();
		String secretKey = properties.getSecretKey();
		String serviceKeyAccess = properties.getServiceKeyAccess();

		actor = new ServiceActor(appId, secretKey, serviceKeyAccess);
	}

	@Bean(name = "vkClient")
	@SneakyThrows
	public VkApiClient getClient() {
		TransportClient transportClient = new HttpTransportClient();
		return new VkApiClient(transportClient);
	}

}
