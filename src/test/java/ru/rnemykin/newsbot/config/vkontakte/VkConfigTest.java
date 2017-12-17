package ru.rnemykin.newsbot.config.vkontakte;

import com.vk.api.sdk.client.VkApiClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertNotNull;

@SpringBootTest
@RunWith(SpringRunner.class)
public class VkConfigTest {

	@Autowired private VkConfig configuration;

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