package com.courses_polytech.movie_backend.repositories;

import com.courses_polytech.movie_backend.models.entities.LibraryEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface LibraryEntryRepository extends JpaRepository<LibraryEntry, UUID> {

    @Query("SELECT le FROM LibraryEntry le " +
            "WHERE le.user.id = :userId " +
            "AND (:watched IS NULL OR le.watched = :watched)")
    Page<LibraryEntry> findByUserIdAndWatched(
            @Param("userId") UUID userId,
            @Param("watched") Boolean watched,
            Pageable pageable);
    Optional<LibraryEntry> findByUserIdAndMovieId(UUID userId, UUID movieId);
}
