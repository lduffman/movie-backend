package com.courses_polytech.movie_backend.models.dtos;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentUpsertDto {
    @NotEmpty
    @Size(max = 255)
    private String title;
    @NotEmpty
    private String content;
}
