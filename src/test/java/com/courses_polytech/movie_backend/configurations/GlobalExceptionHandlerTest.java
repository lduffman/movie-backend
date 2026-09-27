package com.courses_polytech.movie_backend.configurations;

import com.courses_polytech.movie_backend.exceptions.ConflictException;
import com.courses_polytech.movie_backend.exceptions.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Le handler peut être testé comme une simple classe Java.
 * Le cas MethodArgumentNotValidException est couvert par les tests de controllers (@WebMvcTest),
 * où la validation Bean Validation est réellement déclenchée.
 */
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleApi_shouldUseStatusAndMessageOfException() {
        ResponseEntity<GlobalExceptionHandler.ApiError> response = handler.handleApi(new NotFoundException("Movie not found"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(404);
        assertThat(response.getBody().message()).isEqualTo("Movie not found");
        assertThat(response.getBody().timestamp()).isNotNull();
    }

    @Test
    void handleApi_shouldWorkForAnyApiExceptionSubclass() {
        ResponseEntity<GlobalExceptionHandler.ApiError> response = handler.handleApi(new ConflictException("User already exists"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().status()).isEqualTo(409);
    }

    @Test
    void handleIntegrity_shouldReturnConflictWithoutLeakingDatabaseDetails() {
        ResponseEntity<GlobalExceptionHandler.ApiError> response =
                handler.handleIntegrity(new DataIntegrityViolationException("duplicate key value violates UX_movies_externalId"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().message()).isEqualTo("Data integrity conflict");
    }

    @Test
    void handleUnexpected_shouldReturnGenericInternalError() {
        ResponseEntity<GlobalExceptionHandler.ApiError> response = handler.handleUnexpected(new IllegalStateException("boom"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().message()).isEqualTo("Internal error");
    }
}
