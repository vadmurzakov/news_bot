package ru.newsbot.config.factory;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import ru.newsbot.config.properties.ChatAdmin;
import ru.newsbot.model.enums.CityEnum;

import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;

@Component
@EnableConfigurationProperties(ChatAdminsFactory.ChatAdminsProperties.class)
public class ChatAdminsFactory {

	@Getter
	@Setter
	@ConfigurationProperties("news")
	class ChatAdminsProperties {
		private Map<CityEnum, List<ChatAdmin>> chatAdmins;
	}

	@Autowired
	private ChatAdminsProperties chatAdminsProperties;

	public ChatAdmin findById(int adminId) {
		return chatAdminsProperties.chatAdmins.values().stream()
				.flatMap(List::stream)
				.filter(a -> adminId == a.getId())
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Not found chatAdmin with id " + adminId));
	}

	public List<ChatAdmin> findAll(CityEnum city) {
		return unmodifiableList(chatAdminsProperties.chatAdmins.getOrDefault(city, emptyList()));
	}

}
