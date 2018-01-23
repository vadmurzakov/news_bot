package ru.rnemykin.newsbot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.rnemykin.newsbot.model.ModerateMessage;
import ru.rnemykin.newsbot.repository.ModerateMessageRepository;

@Service
public class ModerateMessageService {
    private final ModerateMessageRepository moderateMessageRepository;

    @Autowired
    public ModerateMessageService(ModerateMessageRepository moderateMessageRepository) {
        this.moderateMessageRepository = moderateMessageRepository;
    }


    public ModerateMessage findByTlgrmIdAndAdminId(Integer telegramMessageId, Integer adminId) {
        return moderateMessageRepository.findByTelegramMessageIdAndAdminId(telegramMessageId, adminId);
    }

    public ModerateMessage findByPostIdAndAdminId(Long postId, Integer adminId) {
        return moderateMessageRepository.findByPostIdAndAdminId(postId, adminId);
    }

    public ModerateMessage save(ModerateMessage moderateMessage) {
        return moderateMessageRepository.save(moderateMessage);
    }
}
