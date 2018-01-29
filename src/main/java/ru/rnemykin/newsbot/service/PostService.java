package ru.rnemykin.newsbot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.rnemykin.newsbot.config.properties.Public;
import ru.rnemykin.newsbot.model.Post;
import ru.rnemykin.newsbot.model.enums.PostStatusEnum;
import ru.rnemykin.newsbot.repository.PostRepository;
import ru.rnemykin.newsbot.service.impl.PostAttachmentService;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.util.CollectionUtils.isEmpty;

@Service
@Transactional
public class PostService {
	private final PostRepository postRepository;
	private final PostAttachmentService attachmentService;


	@Autowired
	public PostService(PostRepository postRepository, PostAttachmentService attachmentService) {
		this.postRepository = postRepository;
        this.attachmentService = attachmentService;
    }

	public List<Post> getAll() {
		Iterable<Post> posts = postRepository.findAll();

		List<Post> result = new ArrayList<>();
		posts.forEach(result::add);
		return result;
	}

	public void save(List<Post> posts) {
		Iterable<Post> saved = postRepository.save(posts);
		saved.forEach(p -> {
			if(!isEmpty(p.getPostAttachments())) {
				p.getPostAttachments().forEach(a -> a.setPostId(p.getId()));
                attachmentService.save(p.getPostAttachments());
            }
		});
	}

	public void save(Post post) {
		postRepository.save(post);
	}

	public List<Post> findAllByStatus(PostStatusEnum status, int recordsCount) {
		return postRepository.findAllByStatusOrderById(status, new PageRequest(0, recordsCount));
	}

	public Post findVkPost(Integer vkPostId, Public postPublic) {
		return postRepository.findByPostIdAndPublicId(vkPostId.longValue(), postPublic.getId());
	}

	public Post findById(long id) {
		return postRepository.findOne(id);
	}

	public Post findByPostId(long id) {
		return postRepository.findByPostId(id);
	}
}
