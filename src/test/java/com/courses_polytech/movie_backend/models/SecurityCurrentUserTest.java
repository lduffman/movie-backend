package com.courses_polytech.movie_backend.models;

import com.courses_polytech.movie_backend.exceptions.UnauthorizedException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SecurityCurrentUserTest {

    private final SecurityCurrentUser currentUser = new SecurityCurrentUser();

    @AfterEach
    void clearSecurityContext() {
        // Le SecurityContext est stocké dans un ThreadLocal : il faut le nettoyer pour ne pas polluer les autres tests
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldReadIdAndUsernameFromJwt() {
        UUID userId = UUID.randomUUID();
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .subject(userId.toString())
                .claim("username", "john.doe@example.com")
                .build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));

        assertThat(currentUser.id()).isEqualTo(userId);
        assertThat(currentUser.username()).isEqualTo("john.doe@example.com");
    }

    @Test
    void shouldThrowUnauthorized_whenNoAuthentication() {
        assertThatThrownBy(currentUser::id)
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("No authenticated user");
    }

    @Test
    void shouldThrowUnauthorized_whenPrincipalIsNotAJwt() {
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken("john", "password"));

        assertThatThrownBy(currentUser::username)
                .isInstanceOf(UnauthorizedException.class);
    }
}
