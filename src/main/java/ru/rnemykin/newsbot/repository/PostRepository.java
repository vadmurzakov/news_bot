package ru.rnemykin.newsbot.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.rnemykin.newsbot.model.Post;

@Repository
public interface PostRepository extends CrudRepository<Post, Long> {
}
