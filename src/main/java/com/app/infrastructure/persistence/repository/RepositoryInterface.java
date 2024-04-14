package com.app.infrastructure.persistence.repository;

import com.app.infrastructure.persistence.criteria.Criteria;
import com.app.infrastructure.persistence.exceptions.EntityNotFoundException;

import java.util.List;

public interface RepositoryInterface<E> {
    void setEntity(Class<E> entityClass);
    E getById(Long id) throws EntityNotFoundException;

    List<E> getAll();

    E create(E newEntity);

    E update(Long id, E entity) throws EntityNotFoundException;

    boolean delete(Long id);

    List<E> getByFilter(Criteria criteria);
}

