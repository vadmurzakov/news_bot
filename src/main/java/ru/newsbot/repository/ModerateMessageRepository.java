package ru.newsbot.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.newsbot.model.ModerateMessage;

@Repository
public interface ModerateMessageRepository extends CrudRepository<ModerateMessage, Long> {
    ModerateMessage findByTelegramMessageIdAndAdminId(Integer telegramMessageId, Integer adminId);

    ModerateMessage findByPostIdAndAdminId(Long postId, Integer adminId);
}
