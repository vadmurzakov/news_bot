package ru.rnemykin.newsbot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.rnemykin.newsbot.model.Post;
import ru.rnemykin.newsbot.model.enums.PostStatusEnum;
import ru.rnemykin.newsbot.repository.PostRepository;

import java.util.List;

@Service
public class PostService {

	@Autowired
	private PostRepository postRepository;

	public List<Post> getAll() {
		return (List<Post>) postRepository.findAll();
	}

	public List<Post> findAllByOwnerId(long ownerId, Pageable pageable) {
		return postRepository.findAllByOwnerId(ownerId, pageable);
	}

	public void save(List<Post> posts) {
		posts.forEach(this::save);
	}

	public void save(Post post) {
		postRepository.save(post);
	}

	public List<Post> getAllForModeration() {
		return postRepository.findAllByStatus(PostStatusEnum.NEW, new PageRequest(0, 3));
	}

	public Post findByText(String text) {
		return postRepository.findByText(text.getBytes());
	}

	public List<Post> findAllByStatus(PostStatusEnum status, int recordsCount) {
		return postRepository.findAllByStatus(status, new PageRequest(0, recordsCount));
	}

}
