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
        SimplesCondition<T> equalsCondition = new SimplesCondition<T>(ConditionType.EQUALS);
        equalsCondition.setField(field);
        equalsCondition.setValue(value);
        this.conditions.add(equalsCondition);

        return this;
    }

    public <T> Criteria like(String field, T value) {
        SimplesCondition<T> likeCondition = new SimplesCondition<T>(ConditionType.LIKE);
        likeCondition.setField(field);
        likeCondition.setValue(value);
        this.conditions.add(likeCondition);

        return this;
    }

    public Criteria order(String field, OrderDirections direction) {
        SimplesCondition<OrderDirections> orderCondition = new SimplesCondition<OrderDirections>(ConditionType.ORDER);
        orderCondition.setField(field);
        orderCondition.setValue(direction);
        this.conditions.add(orderCondition);

        return this;
    }
}
