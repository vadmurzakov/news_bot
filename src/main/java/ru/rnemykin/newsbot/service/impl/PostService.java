package ru.rnemykin.newsbot.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.rnemykin.newsbot.config.properties.Public;
import ru.rnemykin.newsbot.model.Post;
import ru.rnemykin.newsbot.model.enums.PostStatusEnum;
import ru.rnemykin.newsbot.repository.PostRepository;

import javax.transaction.Transactional;
import java.util.List;

import static org.springframework.util.CollectionUtils.isEmpty;

@Service
@Transactional
public class PostService extends AbstractEntityService<Long, Post, PostRepository> {
	private final PostAttachmentService attachmentService;

	@Autowired
	public PostService(PostAttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

	@Override
    public List<Post> save(List<Post> posts) {
        List<Post> saved = super.save(posts);
        saved.forEach(p -> {
			if(!isEmpty(p.getPostAttachments())) {
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
	 * Можем ли мы отправить новость вместе с картинкой (на данный момент смотрим посты только с одной картинкой)
	 * @param post - новость, если в новости есть url на внешний источник, предпочтение отдаему ему, а не картинке
	 */
	public boolean isPostWithPhoto(Post post) {
		boolean isPostWithUrl = post.getTextAsString().contains("http");
		return post.getPostAttachments().size() == 1 && !isPostWithUrl;
	}

	/**
	 * Можно ли отправить новость как картинку
	 *  - максимальная длина описания фотографии 200 символов
	 *  - пока обрабатываем те новости, где одна картинка
	 */
	public boolean isPostAsPhoto(Post post) {
		return isPostWithPhoto(post) && post.getTextAsString().length() <= 200;
	}
}
