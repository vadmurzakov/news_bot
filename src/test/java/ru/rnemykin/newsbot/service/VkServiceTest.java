package ru.rnemykin.newsbot.service;

import com.vk.api.sdk.objects.groups.GroupFull;
import com.vk.api.sdk.objects.wall.WallpostFull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import ru.rnemykin.newsbot.config.factory.PublicsFactory;
import ru.rnemykin.newsbot.model.enums.CityEnum;
import ru.rnemykin.newsbot.model.enums.PublicEnum;
import ru.rnemykin.newsbot.service.client.VkService;

import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@SpringBootTest
@RunWith(SpringRunner.class)
public class VkServiceTest {

	@Autowired private VkService service;
	@Autowired private PublicsFactory publicsFactory;

	@Test
	public void getGroup() {
		GroupFull group = service.getGroup(PublicEnum.BEELIVE.id());
		assertNotNull(group);
	}

	@Test
	public void getWallPostsByGroup() {
		List<WallpostFull> wallPosts = service.getWallPosts(publicsFactory.findAll().get(CityEnum.BELGOROD).get(0), 5);
		assertNotNull(wallPosts);
		assertTrue(!wallPosts.isEmpty());
	}
}