package ru.rnemykin.newsbot.service.job;

import com.vk.api.sdk.objects.wall.WallpostFull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.rnemykin.newsbot.model.Post;
import ru.rnemykin.newsbot.model.enums.CityEnum;
import ru.rnemykin.newsbot.model.enums.PostStatusEnum;
import ru.rnemykin.newsbot.model.enums.PublicEnum;
import ru.rnemykin.newsbot.service.PostService;
import ru.rnemykin.newsbot.service.VkService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.Long.valueOf;
import static java.time.Instant.ofEpochMilli;
import static java.time.ZoneId.systemDefault;
import static java.util.stream.Collectors.toList;
import static org.springframework.util.CollectionUtils.isEmpty;

@Slf4j
@Component
public class LoadNewsJob {
    private static final int POSTS_FETCH_SIZE = 10;

    private final VkService vkService;
    private final Map<CityEnum, Set<PublicEnum>> cityNews;
    private final PostService postService;

    @Autowired
    public LoadNewsJob(VkService vkService, Map<CityEnum, Set<PublicEnum>> cityNews, PostService postService) {
        this.vkService = vkService;
        this.cityNews = cityNews;
        this.postService = postService;
    }


    @Scheduled(cron = "${job.loadNews.schedule}")
    public void loadNews() {
        cityNews.get(CityEnum.BELGOROD).forEach(cityPublic -> {
            List<WallpostFull> vkWallPosts = vkService.getWallPosts(cityPublic, POSTS_FETCH_SIZE);
            log.info("retrieve {} vkWallPosts", vkWallPosts.size());
            if(!isEmpty(vkWallPosts)) {
                PageRequest pageRequest = new PageRequest(0, POSTS_FETCH_SIZE);
                List<Post> publicPosts = postService.findAllByOwnerId(cityPublic.id(), pageRequest);
                List<Post> posts = vkWallPosts.stream()
                        .filter(vkPost -> publicPosts.stream().noneMatch(p -> p.getPostId().equals(valueOf(vkPost.getId()))))
                        .map(this::mapToPost).collect(toList());

                postService.save(posts);
            }
        });
    }

    private Post mapToPost(WallpostFull p) {
        Post post = new Post();
        post.setPostId(valueOf(p.getId()));
        post.setOwnerId(-1 * valueOf(p.getOwnerId()));
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
    }
}
