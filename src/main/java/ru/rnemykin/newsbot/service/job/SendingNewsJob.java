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
	@Autowired private PostService postService;
	@Autowired private TelegramService telegramService;

	@Scheduled(cron = "${job.sendingNews.schedule}")
	public void sendingNews() {
		List<Post> allForModeration = postService.getAllForModeration();
		allForModeration.forEach(post -> {
			telegramService.sendMessageToGroupAdmins(post.getTextAsString());
			post.setStatus(PostStatusEnum.MODERATION);
			postService.save(post);
		});
	}
}
