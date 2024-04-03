package com.kleanarchapi.infrastructure.persistence.repository.spring;

import com.kleanarchapi.domain.exception.EntityNotFoundException;
import com.kleanarchapi.domain.exception.IllegalUpdateException;
import com.kleanarchapi.infrastructure.persistence.criteria.*;
import com.kleanarchapi.infrastructure.persistence.repository.RepositoryInterface;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.persistence.EntityManager;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.stream.Collectors;

public class SpringRepository<E> implements RepositoryInterface<E> {
    @Autowired
    private EntityManager entityManager;

    private Class<E> entityClass;

    private int equalsConditionsStrings = 0;

    @Override
    public void setEntity(Class<E> entityClass) {
        this.entityClass = entityClass;
    }

    @Override
    @Transactional
    public E getById(int id) {
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
    public E update(int id, E entity) {
        E foundEntity = this.getById(id);

        Field[] fields = entity.getClass().getDeclaredFields();

        for (Field field : fields) {
            if (!field.canAccess(entity) || Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers())) {
                continue;
            }

            field.setAccessible(true);

            try {
                Object newValue = field.get(entity);
                field.set(foundEntity, newValue);
            } catch (IllegalAccessException e) {
                throw new IllegalUpdateException("Trying to update illegal field " + field.getName() + " from " + this.entityClass.getSimpleName() + ".");
            }
        }

        return foundEntity;
    }

    @Override
    @Transactional
    public boolean delete(int id) {
        E entity = this.getById(id);
        this.entityManager.remove(entity);

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

    public String translateCriteriaConditionToJPQL(ConditionInterface<?> condition) {
        switch (condition.getType()) {
            case ConditionType.EQUALS:
                return this.translateEquals((SimplesCondition<?>) condition);
            case ConditionType.LIKE:
                return this.translateLike((SimplesCondition<?>) condition);
            case ConditionType.ORDER:
                return this.translateOrder((SimplesCondition<OrderDirections>) condition);
        }

        return "";
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

    private String translateEquals(SimplesCondition<?> condition) {
        String statementInitial = "WHERE";

        if (this.equalsConditionsStrings != 0) {
            statementInitial = "AND";
        }

        this.equalsConditionsStrings++;
        return statementInitial + " " + condition.getField() + " = :" + condition.getField();
    }

    private String translateLike(SimplesCondition<?> condition) {
        String statementInitial = "WHERE";

        if (this.equalsConditionsStrings != 0) {
            statementInitial = "AND";
        }

        this.equalsConditionsStrings++;
        return statementInitial + " " + condition.getField() + " LIKE :" + condition.getField();
    }

    private String translateOrder(SimplesCondition<OrderDirections> condition) {
        String statementInitial = "ORDER BY";

        return statementInitial + " " + condition.getField() + " " + condition.getValue().name();
    }
}
