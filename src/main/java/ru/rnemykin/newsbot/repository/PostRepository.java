package ru.rnemykin.newsbot.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import ru.rnemykin.newsbot.model.Post;
import ru.rnemykin.newsbot.model.enums.PostStatusEnum;

import java.util.List;

@Repository
public interface PostRepository extends PagingAndSortingRepository<Post, Long> {
    List<Post> findAllByOwnerIdOrderByIdDesc(long ownerId, Pageable pageable);

    List<Post> findAllByStatusOrderById(PostStatusEnum status, Pageable pageRequest);

    Post findByPostIdAndPublicId(Long postId, Integer publicId);
}
