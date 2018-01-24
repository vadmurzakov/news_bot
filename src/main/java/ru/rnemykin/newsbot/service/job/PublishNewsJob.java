package ru.rnemykin.newsbot.service.job;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.rnemykin.newsbot.config.telegram.TelegramProperties;
import ru.rnemykin.newsbot.model.Post;
import ru.rnemykin.newsbot.model.enums.PostStatusEnum;
import ru.rnemykin.newsbot.service.PostService;
import ru.rnemykin.newsbot.service.TelegramService;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class PublishNewsJob {
    private final PostService postService;
    private final TelegramService telegramService;
    private final TelegramProperties telegramProperties;

    @Autowired
    public PublishNewsJob(PostService postService, TelegramService telegramService, TelegramProperties telegramProperties) {
        this.postService = postService;
        this.telegramService = telegramService;
        this.telegramProperties = telegramProperties;
    }


    @Scheduled(cron = "${job.publishNews.schedule}")
    public void publishNews() {
        List<Post> posts = postService.findAllByStatus(PostStatusEnum.MODERATED, 100);
        posts.forEach(p -> {
            Integer chatId = Integer.valueOf(telegramProperties.getCityChatId().get(p.getCity()));
            if (telegramService.sendMessage(p, chatId)) {
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
