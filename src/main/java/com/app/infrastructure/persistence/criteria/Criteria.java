package com.app.infrastructure.persistence.criteria;

import java.util.ArrayList;
import java.util.List;

public class Criteria {
    private final List<ConditionInterface<?>> conditions;

    public List<ConditionInterface<?>> getConditions() {
        return this.conditions;
    }

    public Criteria() {
        this.conditions = new ArrayList<>();
    }

    public <T> Criteria equals(String field, T value) {
        SimpleCondition<T> equalsCondition = new SimpleCondition<T>(ConditionType.EQUALS);
        equalsCondition.setField(field);
        equalsCondition.setValue(value);
        this.conditions.add(equalsCondition);

        return this;
    }

    public <T> Criteria notEquals(String field, T value) {
        SimpleCondition<T> notEqualsCondition = new SimpleCondition<T>(ConditionType.NOT_EQUALS);
        notEqualsCondition.setField(field);
        notEqualsCondition.setValue(value);
        this.conditions.add(notEqualsCondition);

        return this;
    }

    public <T> Criteria like(String field, T value) {
        SimpleCondition<T> likeCondition = new SimpleCondition<T>(ConditionType.LIKE);
        likeCondition.setField(field);
        likeCondition.setValue(value);
        this.conditions.add(likeCondition);

        return this;
    }

    public Criteria order(String field, OrderDirections direction) {
        SimpleCondition<OrderDirections> orderCondition = new SimpleCondition<OrderDirections>(ConditionType.ORDER);
        orderCondition.setField(field);
        orderCondition.setValue(direction);
        this.conditions.add(orderCondition);

        return this;
    }
}
