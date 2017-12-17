package ru.rnemykin.newsbot.service;

import com.google.common.collect.Iterables;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.ServiceActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.groups.GroupFull;
import com.vk.api.sdk.objects.wall.WallpostFull;
import com.vk.api.sdk.objects.wall.responses.GetResponse;
import com.vk.api.sdk.queries.wall.WallGetFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.rnemykin.newsbot.config.vkontakte.VkConfiguration;
import ru.rnemykin.newsbot.model.enums.PublicEnum;

import javax.annotation.PostConstruct;
import java.text.MessageFormat;
import java.util.List;

@Service
@Slf4j
public class VkService {

	@Autowired private VkConfiguration configuration;

	private final static Integer COUNT_WALLPOST = 5;
	private final static Integer OFFSET_WALLPOST = 0;

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

	public GroupFull getGroup(Long id) {
		try {
			List<GroupFull> groupFulls = vk.groups().getById(actor).groupId(id.toString()).execute();
			return Iterables.getFirst(groupFulls, null);
		} catch (ApiException | ClientException e) {
			log.error(MessageFormat.format("Error get group {0}, {1}", PublicEnum.fromId(id), e.getMessage()));
			return null;
		}
	}

	public List<WallpostFull> getWallpost(Long groupId) {
		return getWallpost(calcGroupId(groupId), COUNT_WALLPOST, OFFSET_WALLPOST);
	}

	public List<WallpostFull> getWallpost(GroupFull group) {
		return getWallpost(calcGroupId(group.getId()), COUNT_WALLPOST, OFFSET_WALLPOST);
	}

	public List<WallpostFull> getWallpost(Integer groupId, Integer count, Integer offset) {
		try {
			GetResponse response = vk.wall().get(actor).ownerId(calcGroupId(groupId)).filter(WallGetFilter.OWNER).count(count).offset(offset).execute();
			return response.getItems();
		} catch (ApiException | ClientException e) {
			log.error(MessageFormat.format("Error get wall posts for group {0}, {1}", PublicEnum.fromId(groupId.longValue()), e.getMessage()));
			return null;
		}
	}

	/**
	 * @return group id must be negative
	 */
	private Integer calcGroupId(Object groupId) {
		return new Integer(String.valueOf(groupId)) * -1;
	}
}
