package ru.rnemykin.newsbot.service;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.ServiceActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.groups.GroupFull;
import com.vk.api.sdk.objects.wall.WallpostFull;
import com.vk.api.sdk.queries.wall.WallGetFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.rnemykin.newsbot.config.properties.Public;
import ru.rnemykin.newsbot.config.vkontakte.VkConfig;
import ru.rnemykin.newsbot.model.enums.PublicEnum;

import javax.annotation.PostConstruct;
import java.util.List;

import static java.util.Collections.emptyList;

@Service
@Slf4j
public class VkService {
	@Autowired
    private VkConfig configuration;

	private ServiceActor actor;
	private VkApiClient vk;

	@PostConstruct
	private void init() {
		Integer appId = configuration.getProperties().getAppId();
		String secretKey = configuration.getProperties().getSecretKey();
		String serviceKeyAccess = configuration.getProperties().getServiceKeyAccess();

		actor = new ServiceActor(appId, secretKey, serviceKeyAccess);
		vk = configuration.getClient();
	}

	public GroupFull getGroup(Integer id) {
		try {
			List<GroupFull> groupFulls = vk.groups().getById(actor).groupId(id.toString()).execute();
			return Iterables.getFirst(groupFulls, null);
		} catch (ApiException | ClientException e) {
			log.error("Error get group {}, {}", PublicEnum.from(id), e.getMessage());
			return null;
		}
	}

	public List<WallpostFull> getWallPosts(Public group, int postsCount) {
		try {
			return Lists.reverse(
					vk.wall().get(actor)
							.ownerId(-group.getId())
							.filter(WallGetFilter.OWNER)
							.count(postsCount)
							.execute()
							.getItems()
			);
		} catch (ApiException | ClientException e) {
			log.error("Error get wall posts for group {}, {}", group, e.getMessage());
			return emptyList();
		}
	}

}
