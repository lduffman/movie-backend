package com.courses_polytech.movie_backend.controllers;

import com.courses_polytech.movie_backend.exceptions.BadRequestException;
import com.courses_polytech.movie_backend.models.dtos.CommentDto;
import com.courses_polytech.movie_backend.models.dtos.CommentUpsertDto;
import com.courses_polytech.movie_backend.models.dtos.MovieDto;
import com.courses_polytech.movie_backend.models.dtos.PageDto;
import com.courses_polytech.movie_backend.services.MovieService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "movies", produces = MediaType.APPLICATION_JSON_VALUE)
public class MovieController {

    private static final List<String> ALLOWED_SEARCH_SORT_PROPERTIES = List.of(
            "title",
            "year",
            "rating",
            "runtime",
            "votes"
    );

    private final MovieService movieService;

    @GetMapping("/search")
    public ResponseEntity<PageDto<MovieDto>> searchMovies(
            @RequestParam(name = "q", required = false) String query,
            @RequestParam(name = "genre", required = false) String queryGenre,
            Pageable pageable
    ) {
        validateSearchSort(pageable.getSort());
        return ResponseEntity.ok(movieService.searchMovies(query, queryGenre, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovieDto> getMovieById(
            @PathVariable UUID id) {
        return ResponseEntity.ok(movieService.getMovieById(id));
    }

    @GetMapping("{movieId}/comments")
    public ResponseEntity<PageDto<CommentDto>> getComments(
            @PathVariable UUID movieId,
            Pageable pageable
    ) {
        return ResponseEntity.ok(movieService.retrieveMovieComments(movieId, pageable));
    }

    @PostMapping("{movieId}/comments")
    public ResponseEntity<CommentDto> addComment(
            @PathVariable UUID movieId,
            @Valid @RequestBody CommentUpsertDto dto
            ) {
        return ResponseEntity.status(201).body(movieService.addMovieComment(movieId, dto));
    }

    @GetMapping("/genres")
    public ResponseEntity<List<String>> getGenres() {
        return ResponseEntity.ok(movieService.getGenres());
    }

    private void validateSearchSort(Sort sort) {
        for (Sort.Order order : sort) {
            if (!ALLOWED_SEARCH_SORT_PROPERTIES.contains(order.getProperty())) {
                throw new BadRequestException("sort must be one of: " + String.join(", ", ALLOWED_SEARCH_SORT_PROPERTIES));
            }
        }
    }
}
