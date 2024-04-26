package com.app.infrastructure.persistence.repository.spring;

import com.app.infrastructure.persistence.exceptions.EntityNotFoundException;
import com.app.infrastructure.persistence.exceptions.IllegalUpdateException;
import com.app.infrastructure.persistence.criteria.*;
import com.app.infrastructure.persistence.exceptions.IllegalCriteriaTypeException;
import com.app.infrastructure.persistence.repository.RepositoryInterface;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

public class SpringRepository<E> implements RepositoryInterface<E> {
    @Autowired
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
        E existingEntity  = this.getById(id);

        entityManager.merge(existingEntity);

        updateEntityValues(existingEntity, entity);

        return existingEntity;
    }

    private void updateEntityValues(E existingEntity, E updatedEntity) {
        Field[] fields = existingEntity.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.getName().equalsIgnoreCase("id")) {
                continue;
            }

            try {
                field.setAccessible(true);
                Object updatedValue = field.get(updatedEntity);
                field.set(existingEntity, updatedValue);
            } catch (IllegalAccessException e) {
                throw new IllegalUpdateException(e.getMessage());
            }
        }
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
            case ConditionType.NOT_EQUALS:
                return this.translateNotEquals((SimpleCondition<?>) condition);
            case ConditionType.LIKE:
                return this.translateLike((SimpleCondition<?>) condition);
            case ConditionType.ORDER:
                return this.translateOrder((SimpleCondition<OrderDirections>) condition);
        }

        throw new IllegalCriteriaTypeException("Criteria " + condition.getType().name() + " not found");
    }

    private Query addConditionToQuery(Query query, ConditionInterface<?> condition) {

        switch (condition.getType()) {
            case ConditionType.EQUALS,
                 ConditionType.NOT_EQUALS:
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

    private String translateNotEquals(SimpleCondition<?> condition) {
        String statementInitial = "WHERE";

        if (this.equalsConditionsStrings != 0) {
            statementInitial = "AND";
        }

        this.equalsConditionsStrings++;
        return statementInitial + " " + condition.getField() + " <> :" + condition.getField();
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
