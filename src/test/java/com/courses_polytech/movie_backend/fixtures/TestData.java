package com.courses_polytech.movie_backend.fixtures;

import com.courses_polytech.movie_backend.models.dtos.MovieDto;
import com.courses_polytech.movie_backend.models.entities.Comment;
import com.courses_polytech.movie_backend.models.entities.LibraryEntry;
import com.courses_polytech.movie_backend.models.entities.Movie;
import com.courses_polytech.movie_backend.models.entities.User;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Jeux de données réutilisables dans les tests.
 * Centraliser la création des objets évite de dupliquer des builders verbeux dans chaque test
 * et permet de ne surcharger que les champs qui comptent pour le cas testé (via toBuilder()).
 */
public final class TestData {

    public static final UUID USER_ID = UUID.fromString("00246abd-d425-4cc6-837a-62c16a22b7f7");
    public static final UUID MOVIE_ID = UUID.fromString("7f1c2b0e-3d4a-4b5c-9e8f-1a2b3c4d5e6f");

    private TestData() {
    }

    public static User user() {
        return User.builder()
                .id(USER_ID)
                .username("john.doe@example.com")
                .password("$2a$10$hashedPassword")
                .firstName("John")
                .lastName("Doe")
                .build();
    }

    public static Movie movie() {
        return Movie.builder()
                .id(MOVIE_ID)
                .externalId("tt0133093")
                .title("The Matrix")
                .plot("A hacker discovers the true nature of reality.")
                .year(1999)
                .posterUrl("https://example.com/matrix.jpg")
                .rating(new BigDecimal("8.7"))
                .votes(2_000_000)
                .runtime(136)
                .genres(List.of("Action", "Sci-Fi"))
                .directors(List.of("Lana Wachowski", "Lilly Wachowski"))
                .writers(List.of("Lana Wachowski", "Lilly Wachowski"))
                .build();
    }

    public static MovieDto movieDto() {
        return MovieDto.builder()
                .id(MOVIE_ID)
                .externalId("tt0133093")
                .title("The Matrix")
                .year(1999)
                .build();
    }

    public static Comment comment() {
        Comment comment = Comment.builder()
                .id(UUID.randomUUID())
                .movie(movie())
                .user(user())
                .title("Masterpiece")
                .content("Still holds up today.")
                .build();
        comment.setCreatedAt(Instant.parse("2026-01-15T10:00:00Z"));
        return comment;
    }

    public static LibraryEntry libraryEntry() {
        return LibraryEntry.builder()
                .id(UUID.randomUUID())
                .movie(movie())
                .user(user())
                .watched(true)
                .rating(new BigDecimal("9.0"))
                .build();
    }
}
