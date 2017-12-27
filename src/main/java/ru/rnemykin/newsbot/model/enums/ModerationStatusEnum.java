package ru.rnemykin.newsbot.model.enums;

public enum ModerationStatusEnum {
	ACCEPT("принять"),
	REJECT("отклонить"),
	DEFER("отложить");

	private String value;

	ModerationStatusEnum(String value) {
		this.value = value;
	}

	public String value() {
		return value;
	}
}