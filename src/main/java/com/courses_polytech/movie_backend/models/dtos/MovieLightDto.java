package com.courses_polytech.movie_backend.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MovieLightDto {
    private UUID id;
    private String title;
    private String plot;
    private Integer year;
    private String posterUrl;
    private BigDecimal rating;
    private Integer runtime;
    private List<String> genres;
    private List<String> directors;
    private List<String> writers;
}
