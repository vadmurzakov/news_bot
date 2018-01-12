package ru.rnemykin.newsbot.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import ru.rnemykin.newsbot.model.Post;
import ru.rnemykin.newsbot.model.enums.PostStatusEnum;

import javax.transaction.Transactional;
import java.util.List;

import static org.junit.Assert.assertNotNull;

@SpringBootTest
@RunWith(SpringRunner.class)
@Transactional
@Rollback
public class PostServiceTest {

	@Autowired
	private PostService postService;

	@Test
	public void getAll() {
		List<Post> posts = postService.getAll();
		assertNotNull(posts);
	}

//	@Test
//	public void getAllModeration() {
//		List<Post> posts = postService.getAllForModeration();
//		assertNotNull(posts);
//	}

	@Test
	public void save() {
		Post post = new Post();
		post.setId(0L);
		post.setStatus(PostStatusEnum.NEW);
		postService.save(post);
	}
}