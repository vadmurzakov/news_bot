package ru.rnemykin.newsbot.service;

import com.pengrad.telegrambot.model.Update;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.junit.Assert.assertNotNull;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TelegramServiceTest {

	@Autowired private TelegramService service;

	@Test
	public void getUpdates() {
		List<Update> updates = service.getUpdates(0);
		assertNotNull(updates);
	}
}