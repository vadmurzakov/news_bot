package ru.newsbot.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.newsbot.config.properties.Public;
import ru.newsbot.model.Post;
import ru.newsbot.model.PostAttachment;
import ru.newsbot.model.enums.PostStatusEnum;
import ru.newsbot.model.enums.TypeAttachmentsEnum;
import ru.newsbot.repository.PostRepository;

import javax.transaction.Transactional;
import java.util.List;

import static org.springframework.util.CollectionUtils.isEmpty;

@Service
@Transactional
@AllArgsConstructor
public class PostService extends AbstractEntityService<Long, Post, PostRepository> {
	private final PostAttachmentService attachmentService;
	private final Integer MAX_LENGTH_MESSAGE = 200;

	@Override
	public List<Post> save(List<Post> posts) {
		List<Post> saved = super.save(posts);
		saved.forEach(p -> {
			if (!isEmpty(p.getPostAttachments())) {
				p.getPostAttachments().forEach(a -> a.setPostId(p.getId()));
				attachmentService.save(p.getPostAttachments());
			}
		});

		return saved;
	}

	public List<Post> findAllByStatus(PostStatusEnum status, int recordsCount) {
		return entityRepository.findAllByStatusOrderById(status, new PageRequest(0, recordsCount));
	}

	public Post findVkPost(Integer vkPostId, Public postPublic) {
		return entityRepository.findByPostIdAndPublicId(vkPostId.longValue(), postPublic.getId());
	}

	/**
	 * Можем ли мы отправить новость вместе с картинкой
	 *
	 * @param post - новость, если в новости есть url на внешний источник, предпочтение отдаему ему, а не картинке
	 */
	public boolean isPostWithPhoto(Post post) {
		boolean isPostWithUrl = post.getTextAsString().contains("http");
		return post.getPostAttachments().size() == 1 && !isPostWithUrl;
	}

	/**
	 * Можно ли отправить новость как картинку
	 * - максимальная длина описания фотографии 200 символов
	 */
	public boolean isPostAsPhoto(Post post) {
		return isPostWithPhoto(post) && post.getTextAsString().length() <= MAX_LENGTH_MESSAGE;
	}

	public boolean isPostAsPhotoAlbum(Post post) {
		boolean isPostAsPhotoAlbum = post.getPostAttachments().size() > 1;
		for (PostAttachment postAttachment : post.getPostAttachments()) {
			isPostAsPhotoAlbum &= postAttachment.getType() == TypeAttachmentsEnum.PHOTO;
		}
		return isPostAsPhotoAlbum;
	}

	public boolean isPostAsGif(Post post) {
		boolean isPostAsGif = post.getPostAttachments().size() == 1;
		try {
			isPostAsGif &= post.getPostAttachments().get(0).getType() == TypeAttachmentsEnum.DOC;
		} catch (IndexOutOfBoundsException exp) {
			isPostAsGif = false;
		}

		return isPostAsGif;
	}
}
