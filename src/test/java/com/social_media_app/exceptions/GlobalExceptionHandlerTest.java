package com.social_media_app.exceptions;

import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    @Test
    void handleNotFound() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        NotFoundException ex = new NotFoundException("Resource not found");

        var pd = handler.handleNotFound(ex);

        assertEquals(404, pd.getStatus());
        assertEquals("Resource not found", pd.getDetail());

        assert pd.getTitle().equals("Not Found");
    }

    @Test
    void handleConflict() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        ConflictException ex = new ConflictException("Conflict occurred");

        var pd = handler.handleConflict(ex);

        assertEquals(409, pd.getStatus());
        assertEquals("Conflict occurred", pd.getDetail());

        assert Objects.equals(pd.getTitle(), "Conflict");
    }
}