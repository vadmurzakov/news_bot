package ru.rnemykin.newsbot.config.factory;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import ru.rnemykin.newsbot.config.properties.Public;
import ru.rnemykin.newsbot.model.enums.CityEnum;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
@EnableConfigurationProperties(PublicsFactory.PublicsProperties.class)
public class PublicsFactory {

    @Getter
    @Setter
    @ConfigurationProperties("news")
    class PublicsProperties {
        private Map<CityEnum, List<Public>> publics;
    }

    @Autowired
    private PublicsProperties publicsProperties;


    public Public findById(int publicId) {
        return publicsProperties.publics.values().stream()
                .flatMap(List::stream)
                .filter(p -> publicId == p.getId())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Not found public with id = " + publicId));
    }

    public Map<CityEnum, List<Public>> findAll() {
        return Collections.unmodifiableMap(publicsProperties.publics);
    }

}
