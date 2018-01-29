package ru.rnemykin.newsbot.service.impl;

import org.springframework.stereotype.Service;
import ru.rnemykin.newsbot.model.PostAttachment;
import ru.rnemykin.newsbot.repository.PostAttachmentRepository;

import javax.transaction.Transactional;

@Service
@Transactional
public class PostAttachmentService extends AbstractEntityService<Long, PostAttachment, PostAttachmentRepository> {
}
