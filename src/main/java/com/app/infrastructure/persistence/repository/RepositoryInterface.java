package com.app.infrastructure.persistence.repository;

import com.app.infrastructure.persistence.criteria.Criteria;

import java.util.List;

public interface RepositoryInterface<E> {
    void setEntity(Class<E> entityClass);
    E getById(int id);

    List<E> getAll();

    E create(E newEntity);

    E update(int id, E entity);

    boolean delete(int id);

    List<E> getByFilter(Criteria criteria);
}

