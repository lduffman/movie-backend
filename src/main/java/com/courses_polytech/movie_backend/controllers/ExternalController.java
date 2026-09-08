package com.courses_polytech.movie_backend.controllers;

import com.courses_polytech.movie_backend.models.dtos.MovieDto;
import com.courses_polytech.movie_backend.services.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.awt.*;
import java.util.List;

@RestController
@RequestMapping(value = "external",  produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ExternalController {

    private final MovieService movieService;

    @GetMapping("/movies")
    public List<MovieDto> searchExternalMovies(
            @RequestParam String query
    ) {
        return movieService.searchMovieByExternalClient(query);
    }
}
