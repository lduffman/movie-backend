package com.courses_polytech.movie_backend.controllers;

import com.courses_polytech.movie_backend.exceptions.BadRequestException;
import com.courses_polytech.movie_backend.models.dtos.LibraryEntryUpsertDto;
import com.courses_polytech.movie_backend.models.dtos.LibraryEntryDto;
import com.courses_polytech.movie_backend.models.dtos.PageDto;
import com.courses_polytech.movie_backend.services.LibraryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "me/library",  produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class LibraryController {

    private static final List<String> ALLOWED_SORT_PROPERTIES = List.of(
            "createdAt",
            "rating",
            "watched",
            "movie.title",
            "movie.year"
    );

    private final LibraryService libraryService;

    @GetMapping
    public ResponseEntity<PageDto<LibraryEntryDto>> getMyLibraryMovies(
            Boolean watched,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        validateSort(pageable.getSort());
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

    private void validateSort(Sort sort) {
        for (Sort.Order order : sort) {
            if (!ALLOWED_SORT_PROPERTIES.contains(order.getProperty())) {
                throw new BadRequestException("sort must be one of: " + String.join(", ", ALLOWED_SORT_PROPERTIES));
            }
        }
    }
}
