package com.app.infrastructure.persistence.exceptions;

public class IllegalCriteriaTypeException extends RuntimeException {
    public IllegalCriteriaTypeException(String message) {
        super(message);
    }
}
