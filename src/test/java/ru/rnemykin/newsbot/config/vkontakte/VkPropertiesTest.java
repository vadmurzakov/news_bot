package ru.rnemykin.newsbot.config.vkontakte;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

@SpringBootTest
@RunWith(SpringRunner.class)
public class VkPropertiesTest {

	@Autowired private VkProperties properties;

	@Test
	public void getAppId() {
		assertNotNull(properties.getAppId());
		assertFalse(properties.getAppId().equals(0)); //is default value in application.yml
	}

	@Test
	public void getSecretKey() {
		assertNotNull(properties.getSecretKey());
		assertFalse(properties.getSecretKey().equals("key")); //is default value in application.yml
	}

	@Test
	public void getServiceKeyAccess() {
		assertNotNull(properties.getServiceKeyAccess());
		assertFalse(properties.getServiceKeyAccess().equals("accessKey")); //is default value in application.yml
	}
}