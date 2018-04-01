package ru.newsbot.config.vkontakte;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

@SpringBootTest
@RunWith(SpringRunner.class)
public class VkPropertiesTest {
	@Autowired
	private VkProperties properties;

	@Test
	public void getAppId() {
		assertNotNull(properties.getAppId());
		assertNotEquals(0, (int) properties.getAppId()); //is default value in application.yml
	}

	@Test
	public void getSecretKey() {
		assertNotNull(properties.getSecretKey());
		assertNotEquals("key", properties.getSecretKey()); //is default value in application.yml
	}

	@Test
	public void getServiceKeyAccess() {
		assertNotNull(properties.getServiceKeyAccess());
		assertNotEquals("accessKey", properties.getServiceKeyAccess()); //is default value in application.yml
	}
}