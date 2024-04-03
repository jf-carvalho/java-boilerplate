package com.kleanarchapi.infrastructure.persistence.criteria;

public class SimplesCondition<T> implements ConditionInterface<T> {
    private String field;
    private T value;
    private final ConditionType type;

    public SimplesCondition(ConditionType type) {
        this.type = type;
    }

    @Override
    public ConditionType getType() {
        return this.type;
    }

    @Override
    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    @Override
    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }
}
