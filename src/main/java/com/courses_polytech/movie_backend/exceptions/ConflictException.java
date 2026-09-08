package com.courses_polytech.movie_backend.exceptions;

import org.springframework.http.HttpStatus;

public class ConflictException extends ApiException {
    public ConflictException(String message) { super(HttpStatus.CONFLICT, message); }
}
