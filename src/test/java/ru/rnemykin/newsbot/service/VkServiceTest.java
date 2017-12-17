package ru.rnemykin.newsbot.service;

import com.vk.api.sdk.objects.groups.GroupFull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import ru.rnemykin.newsbot.model.enums.PublicEnum;

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
}