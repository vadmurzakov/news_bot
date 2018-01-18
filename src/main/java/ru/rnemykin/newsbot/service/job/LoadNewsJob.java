package ru.rnemykin.newsbot.service.job;

import com.vk.api.sdk.objects.wall.WallpostFull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.rnemykin.newsbot.config.factory.PublicsFactory;
import ru.rnemykin.newsbot.model.Post;
import ru.rnemykin.newsbot.model.enums.PostStatusEnum;
import ru.rnemykin.newsbot.service.PostService;
import ru.rnemykin.newsbot.service.VkService;

import java.util.List;

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
    private final PublicsFactory publicsFactory;
    private final PostService postService;

    @Autowired
    public LoadNewsJob(VkService vkService, PublicsFactory publicsFactory, PostService postService) {
        this.vkService = vkService;
        this.publicsFactory = publicsFactory;
        this.postService = postService;
    }


    @Scheduled(cron = "${job.loadNews.schedule}")
    public void loadNews() {
        publicsFactory.findAll().forEach((key, value) -> value.forEach(newsPublic -> {
            List<WallpostFull> vkWallPosts = vkService.getWallPosts(newsPublic, POSTS_FETCH_SIZE);
            log.info("retrieve {} vkWallPosts", vkWallPosts.size());
            if (!isEmpty(vkWallPosts)) {
                List<Post> posts = vkWallPosts.stream()
                        .filter(vkPost -> postService.findVkPost(vkPost.getId(), newsPublic) == null)
                        .map(this::mapToPost)
                        .peek(p -> {
                            p.setCity(key);
                            p.setPublicId(newsPublic.getId());
                        })
                        .collect(toList());

                postService.save(posts);
            }
        }));
    }

    private Post mapToPost(WallpostFull p) {
        Post post = new Post();
        post.setPostId(valueOf(p.getId()));
        post.setOwnerId(-1 * valueOf(p.getOwnerId()));
        post.setType(p.getPostType().getValue());
        post.setPostDate(ofEpochMilli(p.getDate() * 1000L).atZone(systemDefault()).toLocalDateTime());
        post.setText(p.getText().getBytes());
        post.setLikesCount(p.getLikes().getCount() != null ? p.getLikes().getCount() : 0);
        post.setViewsCount(p.getViews().getCount() != null ? p.getViews().getCount() : 0);
        post.setRepostsCount(p.getReposts().getCount() != null ? p.getReposts().getCount() : 0);
        post.setCommentsCount(p.getComments().getCount() != null ? p.getComments().getCount() : 0);
        post.setIsPinned(Integer.valueOf(1).equals(p.getIsPinned()));
        post.setStatus(PostStatusEnum.NEW);
        return post;
    }
}
