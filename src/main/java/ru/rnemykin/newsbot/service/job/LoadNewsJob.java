package ru.rnemykin.newsbot.service.job;

import com.vk.api.sdk.objects.wall.WallpostFull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.rnemykin.newsbot.model.Post;
import ru.rnemykin.newsbot.model.enums.CityEnum;
import ru.rnemykin.newsbot.model.enums.PostStatusEnum;
import ru.rnemykin.newsbot.model.enums.PublicEnum;
import ru.rnemykin.newsbot.repository.PostRepository;
import ru.rnemykin.newsbot.service.VkService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.time.Instant.ofEpochMilli;
import static java.time.ZoneId.systemDefault;
import static java.util.stream.Collectors.toList;

@Slf4j
@Component
public class LoadNewsJob {
    private final VkService vkService;
    private final Map<CityEnum, Set<PublicEnum>> cityNews;
    private final PostRepository postRepository;

    @Autowired
    public LoadNewsJob(VkService vkService, Map<CityEnum, Set<PublicEnum>> cityNews, PostRepository postRepository) {
        this.vkService = vkService;
        this.cityNews = cityNews;
        this.postRepository = postRepository;
    }


    @Scheduled(cron = "${job.loadNews.schedule}")
    public void loadNews() {
        cityNews.get(CityEnum.BELGOROD).forEach(cityPublic -> {
            List<WallpostFull> vkWallPosts = vkService.getWallPosts(cityPublic.id());
            log.info("retrieve {} vkWallPosts", vkWallPosts.size());
            // todo filter existing
            List<Post> posts = vkWallPosts.stream().map(p -> {
                Post post = new Post();
                post.setPostId(Long.valueOf(p.getId()));
                post.setOwnerId(Long.valueOf(p.getOwnerId()));
                post.setType(p.getPostType().getValue());
                post.setPostDate(ofEpochMilli(p.getDate() * 1000L).atZone(systemDefault()).toLocalDateTime());
                post.setText(p.getText().getBytes());
                post.setLikesCount(p.getLikes().getCount() == null ? p.getLikes().getCount() : 0);
                post.setViewsCount(p.getViews().getCount() == null ? p.getViews().getCount() : 0);
                post.setRepostsCount(p.getReposts().getCount() == null ? p.getReposts().getCount() : 0);
                post.setCommentsCount(p.getComments().getCount() == null ? p.getComments().getCount() : 0);
                post.setIsPinned(Integer.valueOf(1).equals(p.getIsPinned()));
                post.setCreateDate(LocalDateTime.now());
                post.setStatus(PostStatusEnum.NEW);
                return post;
            }).collect(toList());

            postRepository.save(posts);
        });
    }
}
