package com.app.infrastructure.persistence.exceptions;

public class IllegalUpdateException extends RuntimeException{
    public IllegalUpdateException(String message) {
        super(message);
    }
}
