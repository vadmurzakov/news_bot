package ru.newsbot.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import ru.newsbot.model.Model;
import ru.newsbot.service.EntityService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AbstractEntityService<ID extends Serializable, T extends Model<ID>, R extends CrudRepository<T, ID>>
        implements EntityService<ID, T, R> {

    @Autowired
    protected R entityRepository;

    @Override
    public T findById(ID entityId) {
        return entityRepository.findOne(entityId);
    }

    @Override
    public List<T> findAll() {
        return convertToList(entityRepository.findAll());
    }

    @Override
    public T save(T entity) {
        return entityRepository.save(entity);
    }

    @Override
    public List<T> save(List<T> entities) {
        return convertToList(entityRepository.save(entities));
    }

    protected List<T> convertToList(Iterable<T> source) {
        List<T> result = new ArrayList<>();
        source.forEach(result::add);
        return result;
    }

    @Override
    public void delete(T entity) {
        entityRepository.delete(entity);
    }
}
