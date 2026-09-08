package com.courses_polytech.movie_backend.models;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DevCurrentUser implements CurrentUser {
    private static final UUID DEV_CURRENT_USER_ID = UUID.fromString("00246abd-d425-4cc6-837a-62c16a22b7f7");

    public UUID id() {
        return DEV_CURRENT_USER_ID;
    }

    public String username() {
        return "john.doe@example.com";
    }
}
