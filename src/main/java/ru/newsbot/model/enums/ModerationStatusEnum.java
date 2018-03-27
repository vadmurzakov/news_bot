package ru.newsbot.model.enums;

import java.util.Arrays;

public enum ModerationStatusEnum {
	ACCEPT("принять"),
	REJECT("отклонить");

	private String value;

	ModerationStatusEnum(String value) {
		this.value = value;
	}

	public String value() {
		return value;
	}

	public static ModerationStatusEnum from(String source) {
		return Arrays.stream(values())
				.filter(e -> e.name().equalsIgnoreCase(source))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("can't find enum value for " + source));
	}
}
