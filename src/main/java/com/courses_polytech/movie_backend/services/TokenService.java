package com.courses_polytech.movie_backend.services;

import com.courses_polytech.movie_backend.configurations.JwtProperties;
import com.courses_polytech.movie_backend.models.dtos.TokenResponse;
import com.courses_polytech.movie_backend.models.entities.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class TokenService {

    private static final String TOKEN_TYPE = "Bearer";

    private final JwtEncoder jwtEncoder;
    private final JwtProperties jwtProperties;

    public TokenResponse issueToken(User user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(jwtProperties.expirationSeconds(), ChronoUnit.SECONDS);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(jwtProperties.issuer())
                .issuedAt(now)
                .expiresAt(expiresAt)
                .subject(user.getId().toString())
                .claim("username", user.getUsername())
                .build();

        String token = jwtEncoder
                .encode(JwtEncoderParameters.from(JwsHeader.with(() -> "HS256").build(), claims))
                .getTokenValue();

        return TokenResponse.builder()
                .accessToken(token)
                .tokenType(TOKEN_TYPE)
                .expiresIn(jwtProperties.expirationSeconds())
                .build();
    }
}
