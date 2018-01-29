package ru.rnemykin.newsbot.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.rnemykin.newsbot.model.PostAttachment;

@Repository
public interface PostAttachmentRepository extends CrudRepository<PostAttachment, Long> {
}
