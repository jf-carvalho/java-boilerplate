package com.app.infrastructure.persistence.repository.spring;

import com.app.infrastructure.persistence.exceptions.EntityNotFoundException;
import com.app.infrastructure.persistence.exceptions.IllegalUpdateException;
import com.app.infrastructure.persistence.criteria.*;
import com.app.infrastructure.persistence.exceptions.IllegalCriteriaTypeException;
import com.app.infrastructure.persistence.repository.RepositoryInterface;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import jakarta.persistence.EntityManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

public class SpringRepository<E> implements RepositoryInterface<E> {

    private final EntityManager entityManager;

    private Class<E> entityClass;

    private int equalsConditionsStrings = 0;

    public SpringRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public void setEntity(Class<E> entityClass) {
        this.entityClass = entityClass;
    }

    @Override
    @Transactional
    public E getById(Long id) {
        E entity = this.entityManager.find(this.entityClass, id);

        if (entity == null) {
            throw new EntityNotFoundException(this.entityClass.getSimpleName() + " with id " + id + " not found.");
        }

        return entity;
    }

    @Override
    @Transactional
    public List<E> getAll() {
        String jpql = "SELECT e FROM " + this.entityClass.getSimpleName() + " e";
        return this.entityManager.createQuery(jpql, this.entityClass).getResultList();
    }

    @Override
    @Transactional
    public E create(E newEntity) {
        this.entityManager.persist(newEntity);
        return newEntity;
    }

    @Override
    @Transactional
    public E update(Long id, E entity) {
        this.getById(id);

        try {
            Method method = this.entityClass.getMethod("setId", Long.class);
            method.invoke(entity, id);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalUpdateException("Cannot update " + this.entityClass.getSimpleName() + " because method setId does not exist or is not accessible");
        }

        entityManager.merge(entity);

        return entity;
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        E entity = this.getById(id);
        entityManager.remove(entity);

        return true;
    }

    @Override
    @Transactional
    public List<E> getByFilter(Criteria criteria) {
        String jpql = "SELECT entity FROM " + this.entityClass.getSimpleName() + " entity ";

        List<ConditionInterface<?>> conditions = criteria.getConditions();

        List<String> jpqlList = conditions.stream()
                .map(this::translateCriteriaConditionToJPQL)
                .collect(Collectors.toList());

        jpql += String.join(" ", jpqlList);

        Query query = this.entityManager.createQuery(jpql, this.entityClass);

        for (ConditionInterface<?> condition : conditions) {
            query = this.addConditionToQuery(query, condition);
        }

        this.equalsConditionsStrings = 0;

        return (List<E>) query.getResultList();
    }

    private String translateCriteriaConditionToJPQL(ConditionInterface<?> condition) {
        switch (condition.getType()) {
            case ConditionType.EQUALS:
                return this.translateEquals((SimpleCondition<?>) condition);
            case ConditionType.LIKE:
                return this.translateLike((SimpleCondition<?>) condition);
            case ConditionType.ORDER:
                return this.translateOrder((SimpleCondition<OrderDirections>) condition);
        }

        throw new IllegalCriteriaTypeException("Criteria " + condition.getType().name() + " not found");
    }

    private Query addConditionToQuery(Query query, ConditionInterface<?> condition) {

        switch (condition.getType()) {
            case ConditionType.EQUALS:
                query = query.setParameter(condition.getField(), condition.getValue());
                break;
            case ConditionType.LIKE:
                query = query.setParameter(condition.getField(), "%" + condition.getValue() + "%");
                break;
        }

        return query;
    }

    private String translateEquals(SimpleCondition<?> condition) {
        String statementInitial = "WHERE";

        if (this.equalsConditionsStrings != 0) {
            statementInitial = "AND";
        }

        this.equalsConditionsStrings++;
        return statementInitial + " " + condition.getField() + " = :" + condition.getField();
    }

    private String translateLike(SimpleCondition<?> condition) {
        String statementInitial = "WHERE";

        if (this.equalsConditionsStrings != 0) {
            statementInitial = "AND";
        }

        this.equalsConditionsStrings++;
        return statementInitial + " " + condition.getField() + " LIKE :" + condition.getField();
    }

    private String translateOrder(SimpleCondition<OrderDirections> condition) {
        String statementInitial = "ORDER BY";

        return statementInitial + " " + condition.getField() + " " + condition.getValue().name();
    }
}
