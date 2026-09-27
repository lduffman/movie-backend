package com.courses_polytech.movie_backend.services;

import com.courses_polytech.movie_backend.configurations.JwtProperties;
import com.courses_polytech.movie_backend.fixtures.TestData;
import com.courses_polytech.movie_backend.models.dtos.TokenResponse;
import com.courses_polytech.movie_backend.models.entities.User;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Ici on n'utilise pas de mock : l'encodeur JWT est un objet pur (pas d'I/O),
 * on peut donc utiliser la vraie implémentation et vérifier le token en le décodant.
 */
class TokenServiceTest {

    private static final SecretKey KEY = new SecretKeySpec(
            "a-test-secret-that-is-at-least-256-bits-long!!".getBytes(StandardCharsets.UTF_8), "HmacSHA256");

    private final JwtProperties jwtProperties = new JwtProperties("movie-backend-test", 3600, "unused");
    private final JwtDecoder jwtDecoder = NimbusJwtDecoder.withSecretKey(KEY).build();

    private TokenService tokenService;

    @BeforeEach
    void setUp() {
        tokenService = new TokenService(new NimbusJwtEncoder(new ImmutableSecret<>(KEY)), jwtProperties);
    }

    @Test
    void issueToken_shouldReturnBearerTokenWithConfiguredExpiration() {
        TokenResponse response = tokenService.issueToken(TestData.user());

        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getExpiresIn()).isEqualTo(3600);
        assertThat(response.getAccessToken()).isNotBlank();
    }

    @Test
    void issueToken_shouldContainUserClaims() {
        User user = TestData.user();

        Jwt jwt = jwtDecoder.decode(tokenService.issueToken(user).getAccessToken());

        assertThat(jwt.getSubject()).isEqualTo(user.getId().toString());
        assertThat(jwt.getClaimAsString("username")).isEqualTo(user.getUsername());
        assertThat(jwt.getClaimAsString("iss")).isEqualTo("movie-backend-test");
        assertThat(Duration.between(jwt.getIssuedAt(), jwt.getExpiresAt())).hasSeconds(3600);
    }
}
