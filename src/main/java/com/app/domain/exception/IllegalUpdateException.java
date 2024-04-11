package com.app.domain.exception;

public class IllegalUpdateException extends RuntimeException{
    public IllegalUpdateException(String message) {
        super(message);
    }
}
