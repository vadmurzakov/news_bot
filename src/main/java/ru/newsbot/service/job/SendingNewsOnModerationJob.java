package ru.newsbot.service.job;

import com.pengrad.telegrambot.response.SendResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.newsbot.config.factory.ChatAdminsFactory;
import ru.newsbot.config.properties.ChatAdmin;
import ru.newsbot.model.Keyboard;
import ru.newsbot.model.ModerateMessage;
import ru.newsbot.model.Post;
import ru.newsbot.model.enums.PostStatusEnum;
import ru.newsbot.service.client.TelegramService;
import ru.newsbot.service.impl.ModerateMessageService;
import ru.newsbot.service.impl.PostService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SendingNewsOnModerationJob {
	@Value("${job.sendingNewsOnModeration.enable}")
	private boolean isEnable;
	@Value("${job.sendingNewsOnModeration.count}")
	private int POSTS_FETCH_SIZE;

	private final PostService postService;
	private final TelegramService telegramService;
	private final ChatAdminsFactory chatAdminsFactory;
	private final ModerateMessageService moderateMessageService;

	@Scheduled(cron = "${job.sendingNewsOnModeration.schedule}")
	public void sendingNewsOnModeration() {
		if (isEnable) {
			List<Post> allForModeration = postService.findAllByStatus(PostStatusEnum.NEW, POSTS_FETCH_SIZE);
			allForModeration.forEach(post -> {

				List<ChatAdmin> cityAdmins = chatAdminsFactory.findAll(post.getCity());
				cityAdmins.forEach(adminEnum -> {
					SendResponse response = telegramService.sendMessage(post, adminEnum.getId(), Keyboard.MODERATION);
					if (response.isOk()) {
						storeModerateMessage(post.getId(), adminEnum.getId(), response.message().messageId());
					}
				});

				post.setSentDate(LocalDateTime.now());
				post.setStatus(PostStatusEnum.MODERATION);
				postService.save(post);
			});
		}
	}

	private void storeModerateMessage(Long postId, Integer chatId, Integer messageId) {
		ModerateMessage msg = ModerateMessage.builder()
				.postId(postId)
				.adminId(chatId)
				.telegramMessageId(messageId)
				.build();

		moderateMessageService.save(msg);
	}
}
