package ru.rnemykin.newsbot.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import ru.rnemykin.newsbot.model.Post;
import ru.rnemykin.newsbot.model.enums.PostStatusEnum;

import java.util.List;

@Repository
public interface PostRepository extends PagingAndSortingRepository<Post, Long> {
    List<Post> findAllByOwnerId(long ownerId, Pageable pageable);
    List<Post> findAllByStatus(PostStatusEnum status);
    Post findByText(byte[] text);
    List<Post> findAllByStatus(PostStatusEnum status, PageRequest pageRequest);
}
