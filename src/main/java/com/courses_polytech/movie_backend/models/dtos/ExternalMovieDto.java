package com.courses_polytech.movie_backend.models.dtos;

import java.math.BigDecimal;
import java.util.List;

public record ExternalMovieDto(
        String external_id, String title, String plot, Integer year, String poster_url,
        BigDecimal rating, Integer votes, Integer runtime, List<String> genres,
        List<String> directors,  List<String> writers
        ) {}
