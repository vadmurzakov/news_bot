package ru.newsbot.service.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.newsbot.config.factory.ChatAdminsFactory;
import ru.newsbot.config.properties.ChatAdmin;
import ru.newsbot.model.Keyboard;
import ru.newsbot.model.Post;
import ru.newsbot.model.enums.PostStatusEnum;
import ru.newsbot.service.client.TelegramService;
import ru.newsbot.service.impl.PostService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class SendingNewsOnModerationJob {
	@Value("${job.sendingNewsOnModeration.count}")
	private int POSTS_FETCH_SIZE;

	private final PostService postService;
	private final TelegramService telegramService;
	private final ChatAdminsFactory chatAdminsFactory;

	@Autowired
	public SendingNewsOnModerationJob(PostService postService, TelegramService telegramService, ChatAdminsFactory chatAdminsFactory) {
		this.postService = postService;
		this.telegramService = telegramService;
		this.chatAdminsFactory = chatAdminsFactory;
	}

	@Scheduled(cron = "${job.sendingNewsOnModeration.schedule}")
	public void sendingNewsOnModeration() {
		List<Post> allForModeration = postService.findAllByStatus(PostStatusEnum.NEW, POSTS_FETCH_SIZE);
		allForModeration.forEach(post -> {

			List<ChatAdmin> cityAdmins = chatAdminsFactory.findAll(post.getCity());
			cityAdmins.forEach(adminEnum -> {
				telegramService.sendMessageOnModeration(post, adminEnum.getId(), Keyboard.DEFAULT);
			});

			post.setSentDate(LocalDateTime.now());
			post.setStatus(PostStatusEnum.MODERATION);
			postService.save(post);
		});
	}
}