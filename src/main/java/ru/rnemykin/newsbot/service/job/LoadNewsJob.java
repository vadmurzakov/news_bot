package ru.rnemykin.newsbot.service.job;

import com.vk.api.sdk.objects.wall.WallpostAttachment;
import com.vk.api.sdk.objects.wall.WallpostFull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.rnemykin.newsbot.config.factory.PublicsFactory;
import ru.rnemykin.newsbot.model.Post;
import ru.rnemykin.newsbot.model.PostAttachment;
import ru.rnemykin.newsbot.model.enums.PostStatusEnum;
import ru.rnemykin.newsbot.service.PostService;
import ru.rnemykin.newsbot.service.VkService;

import java.util.ArrayList;
import java.util.List;

import static com.vk.api.sdk.objects.wall.WallpostAttachmentType.PHOTO;
import static java.lang.Long.valueOf;
import static java.time.Instant.ofEpochMilli;
import static java.time.ZoneId.systemDefault;
import static java.util.stream.Collectors.toList;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.springframework.util.CollectionUtils.isEmpty;

@Slf4j
@Component
public class LoadNewsJob {
    private static final int POSTS_FETCH_SIZE = 3;

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
            log.info("retrieve {} vkWallPosts from {}", vkWallPosts.size(), newsPublic.getUrl());
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
				saveAttachments(vkWallPosts);
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

        //post.setPostAttachments(mapToPostAttachments(p.getAttachments()));

        return post;
    }

	/**
	 * этот метод я решил попробовать для того, чтобы вставить атачменты уже после сохранения Post
	 * @param vkWallPosts
	 */
	private void saveAttachments(List<WallpostFull> vkWallPosts) {
		vkWallPosts.forEach(wallpostFull -> {
			List<PostAttachment> postAttachments = wallpostFull.getAttachments().stream()
					.map(this::mapToPostAttachment)
					.collect(toList());
			Post post = postService.findByPostId(wallpostFull.getId());
			assertNotNull(post);
			post.setPostAttachments(postAttachments);
			postService.save(post);
		});
	}

	/**
	 * этот метод я решил попробовать для того, чтобы вставить атачменты уже после сохранения Post
	 * @param wallpostAttachment
	 * @return
	 */
	private PostAttachment mapToPostAttachment(WallpostAttachment wallpostAttachment) {
		if (wallpostAttachment == null) {
			return null;
		}

		PostAttachment postAttachment = new PostAttachment();

		if (PHOTO == wallpostAttachment.getType()){
			postAttachment.setPostId(wallpostAttachment.getPhoto().getPostId());
			postAttachment.setPhoto75Url(wallpostAttachment.getPhoto().getPhoto75());
			postAttachment.setPhoto130Url(wallpostAttachment.getPhoto().getPhoto130());
			postAttachment.setPhoto604Url(wallpostAttachment.getPhoto().getPhoto604());
			postAttachment.setPhoto807Url(wallpostAttachment.getPhoto().getPhoto807());
			postAttachment.setPhoto1280Url(wallpostAttachment.getPhoto().getPhoto1280());
			postAttachment.setPhoto2560Url(wallpostAttachment.getPhoto().getPhoto2560());

			return postAttachment;
		}

		return null;
	}

	/**
	 * этот метод я использовал когда атачменты сеттил до того, как сохранился Post, в 82й стрчоке он использовался
	 * @param wallpostAttacheds
	 * @return
	 */
    private List<PostAttachment> mapToPostAttachments(List<WallpostAttachment> wallpostAttacheds) {
        if (wallpostAttacheds == null) {
            return null;
        }
        List<PostAttachment> postAttachments = new ArrayList<>();
        wallpostAttacheds.forEach(attachment -> {
			if (PHOTO == attachment.getType()){
				PostAttachment postAttachment = new PostAttachment();
				postAttachment.setPostId(attachment.getPhoto().getId());
				postAttachment.setPhoto75Url(attachment.getPhoto().getPhoto75());
				postAttachment.setPhoto130Url(attachment.getPhoto().getPhoto130());
				postAttachment.setPhoto604Url(attachment.getPhoto().getPhoto604());
				postAttachment.setPhoto807Url(attachment.getPhoto().getPhoto807());
				postAttachment.setPhoto1280Url(attachment.getPhoto().getPhoto1280());
				postAttachment.setPhoto2560Url(attachment.getPhoto().getPhoto2560());
				postAttachments.add(postAttachment);
			}
		});
        return postAttachments;
	}
}
