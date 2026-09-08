package com.courses_polytech.movie_backend.models;

import java.util.UUID;

public interface CurrentUser {
    UUID id();
    String username();
}
