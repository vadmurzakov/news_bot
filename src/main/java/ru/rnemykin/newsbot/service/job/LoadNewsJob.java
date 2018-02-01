package ru.rnemykin.newsbot.service.job;

import com.vk.api.sdk.objects.wall.WallpostAttachment;
import com.vk.api.sdk.objects.wall.WallpostFull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.rnemykin.newsbot.config.factory.PublicsFactory;
import ru.rnemykin.newsbot.model.Post;
import ru.rnemykin.newsbot.model.PostAttachment;
import ru.rnemykin.newsbot.model.enums.PostStatusEnum;
import ru.rnemykin.newsbot.service.client.VkService;
import ru.rnemykin.newsbot.service.impl.PostService;

import java.util.List;
import java.util.Optional;

import static com.vk.api.sdk.objects.wall.WallpostAttachmentType.PHOTO;
import static java.lang.Long.valueOf;
import static java.time.Instant.ofEpochMilli;
import static java.time.ZoneId.systemDefault;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.springframework.util.CollectionUtils.isEmpty;

@Slf4j
@Component
public class LoadNewsJob {
	@Value("${job.loadNews.count}")
    private int POSTS_FETCH_SIZE;

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
            if (!isEmpty(vkWallPosts)) {
                log.info("retrieve {} vkWallPosts from {}", vkWallPosts.size(), newsPublic.getUrl());

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
        int count = 0;
        post.setPostId(valueOf(p.getId()));
        post.setOwnerId(-1 * valueOf(p.getOwnerId()));
        post.setType(p.getPostType().getValue());
        post.setPostDate(ofEpochMilli(p.getDate() * 1000L).atZone(systemDefault()).toLocalDateTime());
        post.setText(p.getText().getBytes());
        post.setLikesCount(p.getLikes().getCount() != null ? p.getLikes().getCount() : 0);
        if (p.getViews() != null && p.getViews().getCount() != null) {
            count = p.getViews().getCount();
        }
        post.setViewsCount(count);
        post.setRepostsCount(p.getReposts().getCount() != null ? p.getReposts().getCount() : 0);
        post.setCommentsCount(p.getComments().getCount() != null ? p.getComments().getCount() : 0);
        post.setIsPinned(Integer.valueOf(1).equals(p.getIsPinned()));
        post.setStatus(PostStatusEnum.NEW);
        post.setPostAttachments(mapToPostAttachments(p.getAttachments()));
        return post;
    }

    private List<PostAttachment> mapToPostAttachments(List<WallpostAttachment> wallpostAttacheds) {
		return Optional.ofNullable(wallpostAttacheds)
				.orElse(emptyList())
				.stream()
				.filter(a -> a != null && PHOTO == a.getType())
				.map(this::mapAttachment)
				.collect(toList());
	}

	private PostAttachment mapAttachment(WallpostAttachment wallpostAttachment) {
		PostAttachment postAttachment = new PostAttachment();
		postAttachment.setPhoto75Url(wallpostAttachment.getPhoto().getPhoto75());
		postAttachment.setPhoto130Url(wallpostAttachment.getPhoto().getPhoto130());
		postAttachment.setPhoto604Url(wallpostAttachment.getPhoto().getPhoto604());
		postAttachment.setPhoto807Url(wallpostAttachment.getPhoto().getPhoto807());
		postAttachment.setPhoto1280Url(wallpostAttachment.getPhoto().getPhoto1280());
		postAttachment.setPhoto2560Url(wallpostAttachment.getPhoto().getPhoto2560());
		return postAttachment;
	}
}
