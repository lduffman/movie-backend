package com.courses_polytech.movie_backend.configurations;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
        String issuer,
        long expirationSeconds,
        String secret
) {
}
