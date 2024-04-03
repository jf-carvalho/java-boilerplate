package com.kleanarchapi.infrastructure.persistence.criteria;

public interface ConditionInterface<T> {
    ConditionType getType();
    String getField();
    void setField(String field);
    T getValue();
    void setValue(T value);
}
