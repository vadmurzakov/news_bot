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
}
