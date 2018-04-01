package ru.newsbot.service.impl;

import org.springframework.stereotype.Service;
import ru.newsbot.model.ModerateMessage;
import ru.newsbot.repository.ModerateMessageRepository;

import javax.transaction.Transactional;

@Service
@Transactional
public class ModerateMessageService extends AbstractEntityService<Long, ModerateMessage, ModerateMessageRepository> {

	public ModerateMessage findByTlgrmIdAndAdminId(Integer telegramMessageId, Integer adminId) {
		return entityRepository.findByTelegramMessageIdAndAdminId(telegramMessageId, adminId);
	}

	public ModerateMessage findByPostIdAndAdminId(Long postId, Integer adminId) {
		return entityRepository.findByPostIdAndAdminId(postId, adminId);
	}
}
