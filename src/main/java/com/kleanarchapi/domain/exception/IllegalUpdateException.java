package com.kleanarchapi.domain.exception;

public class IllegalUpdateException extends RuntimeException{
    public IllegalUpdateException(String message) {
        super(message);
    }

    public IllegalUpdateException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalUpdateException(Throwable cause) {
        super(cause);
    }
}
