package com.courses_polytech.movie_backend.exceptions;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends ApiException {
    public UnauthorizedException(String message) { super(HttpStatus.UNAUTHORIZED, message); }
}