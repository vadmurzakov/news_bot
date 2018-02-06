package ru.rnemykin.newsbot;

import com.google.common.collect.Sets;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;
import ru.rnemykin.newsbot.model.enums.CityEnum;
import ru.rnemykin.newsbot.model.enums.PublicEnum;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.unmodifiableMap;
import static ru.rnemykin.newsbot.model.enums.PublicEnum.BEELIVE;
import static ru.rnemykin.newsbot.model.enums.PublicEnum.BELGOROD1;
import static ru.rnemykin.newsbot.model.enums.PublicEnum.BEL_INTER;

@EnableRetry
@EnableScheduling
@SpringBootApplication
public class NewsBotApplication {

	public static void main(String[] args) {
		SpringApplication.run(NewsBotApplication.class, args);
	}

	@Bean
	public Map<CityEnum, Set<PublicEnum>> cityNews() {
		return unmodifiableMap(new HashMap<CityEnum, Set<PublicEnum>>() {{
			put(CityEnum.BELGOROD, Sets.newHashSet(BEELIVE, BEL_INTER, BELGOROD1));
		}});
	}

}
