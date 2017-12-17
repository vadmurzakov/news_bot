package ru.rnemykin.newsbot.config;

import com.vk.api.sdk.client.VkApiClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import ru.rnemykin.newsbot.config.vkontakte.VkConfiguration;
import ru.rnemykin.newsbot.config.vkontakte.VkProperties;

import static org.junit.Assert.assertNotNull;

@SpringBootTest
@RunWith(SpringRunner.class)
public class VkConfigurationTest {

	@Autowired private VkConfiguration configuration;

	@Test
	public void vkApiClient() {
		VkApiClient vk = configuration.getClient();
		assertNotNull(vk);
	}

	@Test
	public void getVkProperties() {
		VkProperties properties = configuration.getProperties();
		assertNotNull(properties);
	}
}