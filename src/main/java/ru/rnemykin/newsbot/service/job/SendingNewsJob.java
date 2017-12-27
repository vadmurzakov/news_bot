package ru.rnemykin.newsbot.service.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.rnemykin.newsbot.model.Post;
import ru.rnemykin.newsbot.model.enums.PostStatusEnum;
import ru.rnemykin.newsbot.service.PostService;
import ru.rnemykin.newsbot.service.TelegramService;

import java.io.UnsupportedEncodingException;
import java.util.List;

@Slf4j
@Component
public class SendingNewsJob {
	@Autowired private PostService postService;
	@Autowired private TelegramService telegramService;

	@Scheduled(fixedDelay = 10000)
	public void sendingNews() {
		List<Post> allForModeration = postService.getAllForModeration();
		allForModeration.forEach(post -> {
			try {
				telegramService.sendMessage(new String(post.getText(), "UTF-8"));
				post.setStatus(PostStatusEnum.MODERATION);
				postService.save(post);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		});
	}
}
