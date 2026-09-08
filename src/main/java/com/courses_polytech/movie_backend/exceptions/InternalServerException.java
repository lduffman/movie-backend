package com.courses_polytech.movie_backend.exceptions;

import org.springframework.http.HttpStatus;

public class InternalServerException extends ApiException {
    public InternalServerException(String message) { super(HttpStatus.INTERNAL_SERVER_ERROR, message); }
}
