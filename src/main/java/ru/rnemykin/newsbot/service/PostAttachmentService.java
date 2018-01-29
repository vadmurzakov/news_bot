package ru.rnemykin.newsbot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.rnemykin.newsbot.model.PostAttachment;
import ru.rnemykin.newsbot.repository.PostAttachmentRepository;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class PostAttachmentService {
    @Autowired
    private PostAttachmentRepository attachmentRepository;

    public List<PostAttachment> save(List<PostAttachment> postAttachments) {
        Iterable<PostAttachment> saved = attachmentRepository.save(postAttachments);

        List<PostAttachment> result = new ArrayList<>();
        saved.forEach(result::add);
        return result;
    }
}
