package ru.rnemykin.newsbot.service;

import org.springframework.data.repository.CrudRepository;
import ru.rnemykin.newsbot.model.Model;

import java.io.Serializable;
import java.util.List;

public interface EntityService<ID extends Serializable, T extends Model<ID>, R extends CrudRepository<T, ID>> {
    T findById(ID entityId);

    List<T> findAll();

    T save(T entity);

    List<T> save(List<T> entities);

    void delete(T entity);
}
