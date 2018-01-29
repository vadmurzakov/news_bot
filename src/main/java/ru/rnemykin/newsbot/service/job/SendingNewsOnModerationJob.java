package ru.rnemykin.newsbot.service.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.rnemykin.newsbot.config.factory.ChatAdminsFactory;
import ru.rnemykin.newsbot.config.properties.ChatAdmin;
import ru.rnemykin.newsbot.model.Keyboard;
import ru.rnemykin.newsbot.model.Post;
import ru.rnemykin.newsbot.model.enums.PostStatusEnum;
import ru.rnemykin.newsbot.service.TelegramService;
import ru.rnemykin.newsbot.service.impl.PostService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class SendingNewsOnModerationJob {
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
		List<Post> allForModeration = postService.findAllByStatus(PostStatusEnum.NEW, 3);
		allForModeration.forEach(post -> {

			List<ChatAdmin> cityAdmins = chatAdminsFactory.findAll(post.getCity());
			cityAdmins.forEach(adminEnum -> {
				telegramService.sendMessage(post, adminEnum.getId(), Keyboard.DEFAULT);
			});

			post.setSentDate(LocalDateTime.now());
			post.setStatus(PostStatusEnum.MODERATION);
			postService.save(post);
		});
	}
}
