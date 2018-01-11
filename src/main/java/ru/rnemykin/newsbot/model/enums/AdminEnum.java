package ru.rnemykin.newsbot.model.enums;

/**
 * offset - используется для глобального редактирования/отправления/удаления сообщений в боте
 * т.е. если один из админов модерирует новость, все остальные админы увидят этот результат
 */
public enum AdminEnum {
	VADMURZAKOV(186736203),
	RNEMYKIN(228618478);

	private Integer id;

	AdminEnum(Integer id) {
		this.id = id;
	}

	public Integer id() {
		return id;
	}
}
