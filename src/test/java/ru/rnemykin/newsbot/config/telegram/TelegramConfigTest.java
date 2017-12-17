package ru.rnemykin.newsbot.config.telegram;


import com.pengrad.telegrambot.TelegramBot;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertNotNull;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TelegramConfigTest {

	@Autowired
	private TelegramConfig config;

	@Test
	public void getClient() {
		TelegramBot client = config.getClient();
		assertNotNull(client);
	}

	@Test
	public void getProperties() {
		assertNotNull(config.getProperties());
	}
}