package com.courses_polytech.movie_backend.controllers;

import com.courses_polytech.movie_backend.models.dtos.LibraryEntryUpsertDto;
import com.courses_polytech.movie_backend.models.dtos.LibraryEntryDto;
import com.courses_polytech.movie_backend.models.dtos.PageDto;
import com.courses_polytech.movie_backend.services.LibraryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(value = "me/library",  produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class LibraryController {

    private final LibraryService libraryService;

    @GetMapping
    public ResponseEntity<PageDto<LibraryEntryDto>> getMyLibraryMovies(Boolean watched, Pageable pageable) {
        return ResponseEntity.ok(libraryService.getMyLibrary(watched, pageable));
    }

    @GetMapping("{movieId}")
    public ResponseEntity<LibraryEntryDto> getLibraryMovie(@PathVariable UUID movieId) {
        return ResponseEntity.ok(libraryService.getMyLibraryEntry(movieId));
    }

    @PutMapping("{movieId}")
    public ResponseEntity<LibraryEntryDto> upsertLibraryEntry(@PathVariable UUID movieId, @Valid @RequestBody LibraryEntryUpsertDto dto) {
        return ResponseEntity
                .status(200)
                .body(libraryService.upsertLibraryEntry(movieId, dto.isWatched(), dto.getRating()));
    }

    @DeleteMapping("{movieId}")
    public ResponseEntity<Void> removeMovieFromLibrary(@PathVariable UUID movieId) {
        libraryService.removeMovieFromMyLibrary(movieId);
        return ResponseEntity.noContent().build();
    }
}
