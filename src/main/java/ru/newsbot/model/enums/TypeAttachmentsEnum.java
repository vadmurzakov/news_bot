package ru.newsbot.model.enums;

import java.util.Arrays;

public enum TypeAttachmentsEnum {
	VIDEO,
	DOC,
	PHOTO;

	public static TypeAttachmentsEnum from(String source) {
		return Arrays.stream(values())
				.filter(e -> e.name().equalsIgnoreCase(source.toUpperCase()))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("can't find enum value for " + source));
	}
}
