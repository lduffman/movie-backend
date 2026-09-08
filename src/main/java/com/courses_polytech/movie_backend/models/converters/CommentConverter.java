package com.courses_polytech.movie_backend.models.converters;

import com.courses_polytech.movie_backend.models.dtos.CommentDto;
import com.courses_polytech.movie_backend.models.entities.Comment;
import org.springframework.stereotype.Component;

@Component
public class CommentConverter {

    public CommentDto mapToDto(Comment comment) {
        String author = comment.getUser().getFirstName() + " " + comment.getUser().getLastName();
        return CommentDto.builder()
                .id(comment.getId())
                .movieId(comment.getMovie().getId())
                .author(author)
                .title(comment.getTitle())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
