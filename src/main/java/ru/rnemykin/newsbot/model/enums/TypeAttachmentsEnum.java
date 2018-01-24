package ru.rnemykin.newsbot.model.enums;

public enum TypeAttachmentsEnum {
	VIDEO,
	PHOTO;

	private static TypeAttachmentsEnum from(String source) {
		for (TypeAttachmentsEnum attachmentsEnum : TypeAttachmentsEnum.values()) {
			if (source.equals(attachmentsEnum.name())) {
				return attachmentsEnum;
			}
		}
		throw new RuntimeException("Not found TypeAttachmentsEnum value of " + source);
	}
}
