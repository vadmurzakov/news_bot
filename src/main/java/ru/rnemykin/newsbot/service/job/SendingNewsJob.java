package ru.rnemykin.newsbot.service.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.rnemykin.newsbot.model.Post;
import ru.rnemykin.newsbot.model.enums.PostStatusEnum;
import ru.rnemykin.newsbot.service.PostService;
import ru.rnemykin.newsbot.service.TelegramService;

import java.util.List;

@Slf4j
@Component
public class SendingNewsJob {
	private final PostService postService;
	private final TelegramService telegramService;

	@Autowired
	public SendingNewsJob(PostService postService, TelegramService telegramService) {
		this.postService = postService;
		this.telegramService = telegramService;
	}

	@Scheduled(cron = "${job.sendingNews.schedule}")
	public void sendingNews() {
		List<Post> allForModeration = postService.findAllByStatus(PostStatusEnum.NEW, 3);
		allForModeration.forEach(post -> {
			telegramService.sendMessageToGroupAdmins(post);
			post.setStatus(PostStatusEnum.MODERATION);
			postService.save(post);
		});
	}
}
