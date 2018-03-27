package ru.newsbot.model.enums;

public enum AdminEnum {
	VADMURZAKOV(186736203);

	private Integer id;

	AdminEnum(Integer id) {
		this.id = id;
	}

	public Integer id() {
		return id;
	}
}
