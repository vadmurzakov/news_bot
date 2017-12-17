package ru.rnemykin.newsbot.service;

import com.vk.api.sdk.objects.groups.GroupFull;
import com.vk.api.sdk.objects.wall.WallpostFull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import ru.rnemykin.newsbot.model.enums.PublicEnum;

import java.util.List;

import static org.junit.Assert.assertNotNull;

@SpringBootTest
@RunWith(SpringRunner.class)
public class VkServiceTest {

	@Autowired private VkService service;

	@Test
	public void getGroup() {
		GroupFull group = service.getGroup(PublicEnum.BEELIVE.id());
		assertNotNull(group);
	}

	@Test
	public void getWallPostsByGroupId() {
		List<WallpostFull> wallPosts = service.getWallPosts(PublicEnum.BEELIVE.id());
		assertNotNull(wallPosts);
	}

	@Test
	public void getWallPostsByGroup() {
		GroupFull group = service.getGroup(PublicEnum.BEELIVE.id());
		List<WallpostFull> wallPosts = service.getWallPosts(group);
		assertNotNull(wallPosts);
	}
}