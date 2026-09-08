package com.courses_polytech.movie_backend.models.dtos;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LibraryEntryDto {
    private UUID id;
    private MovieDto movie;
    private boolean watched;
    private BigDecimal rating;
}
