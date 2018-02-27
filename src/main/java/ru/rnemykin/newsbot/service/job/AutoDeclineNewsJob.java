package ru.rnemykin.newsbot.service.job;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.rnemykin.newsbot.config.factory.ChatAdminsFactory;
import ru.rnemykin.newsbot.config.properties.ChatAdmin;
import ru.rnemykin.newsbot.model.ModerateMessage;
import ru.rnemykin.newsbot.model.Post;
import ru.rnemykin.newsbot.model.enums.ModerationStatusEnum;
import ru.rnemykin.newsbot.model.enums.PostStatusEnum;
import ru.rnemykin.newsbot.service.client.TelegramService;
import ru.rnemykin.newsbot.service.impl.ModerateMessageService;
import ru.rnemykin.newsbot.service.impl.PostService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@AllArgsConstructor
public class AutoDeclineNewsJob {
    private final ModerateMessageService moderateMessageService;
    private final TelegramService telegramService;
    private final PostService postService;
    private final ChatAdminsFactory chatAdminsFactory;

    @Scheduled(cron = "${job.autoDeclineNews.interval}")
    public void declineOldNews() {
        List<Post> postsOnModeration = postService.findAllByStatus(PostStatusEnum.MODERATION, 200);
        List<Post> oldPosts = postsOnModeration.stream()
                .filter(p -> p.getCreateDate().plusHours(4).isBefore(LocalDateTime.now()))
                .collect(Collectors.toList());

        for (Post post : oldPosts) {
            List<ChatAdmin> cityAdmins = chatAdminsFactory.findAll(post.getCity());
            cityAdmins.forEach(a -> {
                ModerateMessage message = moderateMessageService.findByPostIdAndAdminId(post.getId(), a.getId());
                if (message != null) {
                    telegramService.deleteMessage(message.getAdminId(), message.getTelegramMessageId());
                    message.setProcessedStatus(ModerationStatusEnum.REJECT);
                    moderateMessageService.save(message);
                }
            });

            post.setStatus(PostStatusEnum.CANCELED);
        }

        postService.save(oldPosts);
    }
}
