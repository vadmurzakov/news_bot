package ru.rnemykin.newsbot.model.enums;

/**
 * offset - используется для глобального редактирования/отправления/удаления сообщений в боте
 * т.е. если один из админов модерирует новость, все остальные админы увидят этот результат
 */
public enum AdminEnum {
	VADMURZAKOV(186736203L, 0),
	RNEMYKIN(228618478L, 1);

	private Long id;
	private Integer offset;

	AdminEnum(Long id, Integer offset) {
		this.id = id;
		this.offset = offset;
	}

	public Long id() {
		return id;
	}

	public Integer offset() {
		return offset;
	}
}
