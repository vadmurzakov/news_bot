package ru.newsbot.service.impl;

import org.springframework.stereotype.Service;
import ru.newsbot.model.PostAttachment;
import ru.newsbot.repository.PostAttachmentRepository;

import javax.transaction.Transactional;

@Service
@Transactional
public class PostAttachmentService extends AbstractEntityService<Long, PostAttachment, PostAttachmentRepository> {
}
