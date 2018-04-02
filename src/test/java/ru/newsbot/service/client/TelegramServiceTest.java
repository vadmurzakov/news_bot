package ru.newsbot.service.client;

import com.pengrad.telegrambot.response.MessagesResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import ru.newsbot.model.Post;
import ru.newsbot.model.PostAttachment;
import ru.newsbot.model.enums.AdminEnum;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertNotNull;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TelegramServiceTest {
	@Autowired
	private TelegramService telegramService;

	@Test
	public void sendMediaGroup() {
		MessagesResponse response = telegramService.sendMediaGroup(AdminEnum.VADMURZAKOV.id(), getPost());
		assertNotNull(response);
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