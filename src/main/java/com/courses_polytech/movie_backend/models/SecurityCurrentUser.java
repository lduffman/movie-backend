package com.courses_polytech.movie_backend.models;

import com.courses_polytech.movie_backend.exceptions.UnauthorizedException;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Primary
@Component
public class SecurityCurrentUser implements CurrentUser {

    @Override
    public UUID id() {
        return UUID.fromString(jwt().getSubject());
    }

    @Override
    public String username() {
        return jwt().getClaimAsString("username");
    }

    private Jwt jwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new UnauthorizedException("No authenticated user");
        }

        return jwt;
    }
}
