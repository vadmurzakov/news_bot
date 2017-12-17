package ru.rnemykin.newsbot.service;

import com.google.common.collect.Iterables;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.ServiceActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.groups.GroupFull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.rnemykin.newsbot.config.vkontakte.VkConfiguration;
import ru.rnemykin.newsbot.model.enums.PublicEnum;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
@Slf4j
public class VkService {

	@Autowired private VkConfiguration configuration;

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
			log.error("Error get group " + PublicEnum.fromId(id));
			return null;
		}
	}
}
