package ru.rnemykin.newsbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import ru.rnemykin.newsbot.model.enums.CityEnum;
import ru.rnemykin.newsbot.model.enums.PublicEnum;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.singleton;
import static java.util.Collections.unmodifiableMap;

@SpringBootApplication
public class NewsBotApplication {

	public static void main(String[] args) {
		SpringApplication.run(NewsBotApplication.class, args);
	}

	@Bean
	public Map<CityEnum, Set<PublicEnum>> cityNews() {
		return unmodifiableMap(new HashMap<CityEnum, Set<PublicEnum>>() {{
			put(CityEnum.BELGOROD, singleton(PublicEnum.BEELIVE));
		}});
	}

}
