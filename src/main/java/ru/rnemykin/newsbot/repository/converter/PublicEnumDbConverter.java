package ru.rnemykin.newsbot.repository.converter;

import ru.rnemykin.newsbot.model.enums.PublicEnum;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Arrays;

@Converter(autoApply = true)
public class PublicEnumDbConverter implements AttributeConverter<PublicEnum, Integer> {

    @Override
    public Integer convertToDatabaseColumn(PublicEnum source) {
        return source.id();
    }

    @Override
    public PublicEnum convertToEntityAttribute(Integer source) {
        return Arrays.stream(PublicEnum.values())
                .filter(e -> e.id().equals(source))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("not found public with id" + source));
    }
}
