package com.courses_polytech.movie_backend.models.dtos;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class LibraryEntryUpsertDto {
    private boolean watched;
    @DecimalMax("10.0")
    @DecimalMin("0.0")
    private BigDecimal rating;
}
