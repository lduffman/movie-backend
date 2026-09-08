package com.courses_polytech.movie_backend.repositories;

import com.courses_polytech.movie_backend.models.entities.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {

    Page<Comment> findAllByMovieIdOrderByCreatedAtDesc(UUID movieId, Pageable pageable);
}
