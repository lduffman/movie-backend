package com.courses_polytech.movie_backend.repositories;

import com.courses_polytech.movie_backend.models.entities.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MovieRepository extends JpaRepository<Movie, UUID> {

    Optional<Movie> findByExternalId(String externalId);

    @Query(
            value = """
                    SELECT m.*
                    FROM movies m
                    WHERE (COALESCE(:title, '') = '' OR m.title ILIKE CONCAT('%', :title, '%'))
                      AND (
                            COALESCE(:genre, '') = ''
                            OR EXISTS (
                                SELECT 1
                                FROM unnest(m.genres) genre
                                WHERE LOWER(genre) = LOWER(:genre)
                            )
                      )
                    """,
            countQuery = """
                    SELECT COUNT(*)
                    FROM movies m
                    WHERE (COALESCE(:title, '') = '' OR m.title ILIKE CONCAT(:title, '%'))
                      AND (
                            COALESCE(:genre, '') = ''
                            OR EXISTS (
                                SELECT 1
                                FROM unnest(m.genres) genre
                                WHERE LOWER(genre) = LOWER(:genre)
                            )
                      )
                    """,
            nativeQuery = true
    )
    Page<Movie> searchMovies(
            @Param("title") String title,
            @Param("genre") String genre,
            Pageable pageable
    );

    @Query(value = """
            SELECT DISTINCT unnest(m.genres) AS genre
                FROM movies m
                ORDER BY genre
            """, nativeQuery = true)
    List<String> findAllDistinctMovieGenres();
}
