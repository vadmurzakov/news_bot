package ru.newsbot.service.client;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.groups.GroupFull;
import com.vk.api.sdk.objects.wall.WallpostFull;
import com.vk.api.sdk.queries.wall.WallGetFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.newsbot.config.properties.Public;
import ru.newsbot.config.vkontakte.VkConfig;
import ru.newsbot.model.enums.PublicEnum;

import java.util.List;

import static java.util.Collections.emptyList;

@Service
@Slf4j
@RequiredArgsConstructor
public class VkService {
	private final VkConfig configuration;
	private final VkApiClient client;

	public GroupFull getGroup(Integer id) {
		try {
			List<GroupFull> groupFulls = client.groups().getById(configuration.getActor()).groupId(id.toString()).execute();
			return Iterables.getFirst(groupFulls, null);
		} catch (ApiException | ClientException e) {
			log.error("Error get group {}, {}", PublicEnum.from(id), e.getMessage());
			return null;
		}
	}

	public List<WallpostFull> getWallPosts(Public group, int postsCount) {
		try {
			return Lists.reverse(
					client.wall().get(configuration.getActor())
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
