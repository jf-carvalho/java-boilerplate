package com.app.application.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ErrorResponseTest {
    @Test
    public void shouldCreateNewErrorResponseInstance() {
        ErrorResponse errorResponse = new ErrorResponse("error message");

        assertNotNull(errorResponse);
    }
}