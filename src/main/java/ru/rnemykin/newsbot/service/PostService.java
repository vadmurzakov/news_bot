package ru.rnemykin.newsbot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.rnemykin.newsbot.model.Post;
import ru.rnemykin.newsbot.model.enums.PostStatusEnum;
import ru.rnemykin.newsbot.model.enums.PublicEnum;
import ru.rnemykin.newsbot.repository.PostRepository;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class PostService {
	private final PostRepository postRepository;

	@Autowired
	public PostService(PostRepository postRepository) {
		this.postRepository = postRepository;
	}

	public List<Post> getAll() {
		Iterable<Post> posts = postRepository.findAll();

		List<Post> result = new ArrayList<>();
		posts.forEach(result::add);
		return result;
	}

	public void save(List<Post> posts) {
		postRepository.save(posts);
	}

	public void save(Post post) {
		postRepository.save(post);
	}

	public Post findByText(String text) {
		return postRepository.findByText(text.getBytes());
	}

	public List<Post> findAllByStatus(PostStatusEnum status, int recordsCount) {
		return postRepository.findAllByStatusOrderById(status, new PageRequest(0, recordsCount));
	}

	public Post findVkPost(Integer vkPostId, PublicEnum postPublic) {
		return postRepository.findByPostIdAndPostPublic(vkPostId.longValue(), postPublic);
	}
}
