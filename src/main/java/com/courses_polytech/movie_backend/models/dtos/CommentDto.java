package com.courses_polytech.movie_backend.models.dtos;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class CommentDto {
    private UUID id;
    private UUID movieId;
    private String author;
    private String title;
    private String content;
    private Instant createdAt;
}
