package ru.newsbot.service.client;

import com.pengrad.telegrambot.response.MessagesResponse;
import com.pengrad.telegrambot.response.SendResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import ru.newsbot.model.Keyboard;
import ru.newsbot.model.Post;
import ru.newsbot.model.PostAttachment;
import ru.newsbot.model.enums.AdminEnum;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TelegramServiceTest {
	@Autowired
	private TelegramService telegramService;

	private final Integer chatId = AdminEnum.VADMURZAKOV.id();
	private final String caption = "Тестовой описание для файлов мультимедия";

	@Test
	public void sendMediaGroup() {
		MessagesResponse response = telegramService.sendMediaGroup(chatId, getPost());
		assertTrue(response.isOk());
	}

	@Test
	public void sendDocument() {
		String url = "https://vk.com/doc13342202_463504699?hash=aa4ab77a2399c714dc&dl=GA:1522680861:921a9bdd202962fc0e&api=1&mp4=1";
		SendResponse response = telegramService.sendDocument(chatId, url, caption, Keyboard.MODERATION);
		assertTrue(response.isOk());
	}

	@Test
	public void sendPhoto() {
		String url = "https://pp.userapi.com/c639617/v639617986/50da6/JwujfyqWhjI.jpg";
		SendResponse response = telegramService.sendPhoto(chatId, url, caption, Keyboard.MODERATION);
		assertTrue(response.isOk());
	}

	private Post getPost() {
		PostAttachment one = new PostAttachment();
		PostAttachment two = new PostAttachment();
		PostAttachment three = new PostAttachment();
		one.setPhoto1280Url("https://pp.userapi.com/c639617/v639617986/50da6/JwujfyqWhjI.jpg");
		two.setPhoto1280Url("https://pp.userapi.com/c837132/v837132986/5bd46/FrsryNedLgI.jpg");
		three.setPhoto1280Url("https://pp.userapi.com/c841335/v841335986/1d0fe/VjHwLk83Exk.jpg");
		List<PostAttachment> postAttachments = new ArrayList<>(Arrays.asList(one, two, three));
		Post post = new Post();
		post.setPostAttachments(postAttachments);
		return post;
	}
}