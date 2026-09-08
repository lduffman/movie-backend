package com.courses_polytech.movie_backend.exceptions;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends ApiException {
    public ForbiddenException(String message) { super(HttpStatus.FORBIDDEN, message); }
}
