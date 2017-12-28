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

	public static ModerationStatusEnum from(String sourct) {
		//todo[vmurzakov]: stub
		return null;
	}
}
