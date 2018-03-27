package ru.newsbot.config.telegram;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StringUtils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TelegramPropertiesTest {

	@Autowired private TelegramProperties properties;

	@Test
	public void getToken() {
		assertTrue(!StringUtils.isEmpty(properties.getToken()));
		assertFalse(properties.getToken().equals("<token>")); //is default value in application.yml
	}
}