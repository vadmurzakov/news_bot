package ru.newsbot.service.job;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.newsbot.model.Post;
import ru.newsbot.model.enums.PostStatusEnum;
import ru.newsbot.service.client.TelegramService;
import ru.newsbot.service.impl.PostService;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PublishNewsJob {
	@Value("${job.publishNews.enable}")
	private boolean isEnable;
	private final PostService postService;
	private final TelegramService telegramService;

	@Scheduled(cron = "${job.publishNews.schedule}")
	public void publishNews() {
		if (isEnable) {
			List<Post> posts = postService.findAllByStatus(PostStatusEnum.MODERATED, 100);
			posts.forEach(p -> {
				if (telegramService.sendMessageToChannel(p)) {
					p.setPublishDate(LocalDateTime.now());
					p.setStatus(PostStatusEnum.PUBLISHED);
				} else {
					p.setSentAttemptsCount(p.getSentAttemptsCount() + 1);
					if (p.getSentAttemptsCount() > 3) {
						p.setStatus(PostStatusEnum.ERROR);
					}
				}

				postService.save(p);
			});
		}
	}
}
