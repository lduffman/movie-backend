package com.courses_polytech.movie_backend.models.dtos;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class LibraryEntryUpsertDto {
    private boolean watched;
    @Max(10)
    @Min(1)
    private BigDecimal rating;
}
