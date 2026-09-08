package com.courses_polytech.movie_backend.services;

import com.courses_polytech.movie_backend.exceptions.NotFoundException;
import com.courses_polytech.movie_backend.clients.ExternalApiClient;
import com.courses_polytech.movie_backend.models.CurrentUser;
import com.courses_polytech.movie_backend.models.converters.CommentConverter;
import com.courses_polytech.movie_backend.models.converters.MovieConverter;
import com.courses_polytech.movie_backend.models.dtos.CommentDto;
import com.courses_polytech.movie_backend.models.dtos.CommentUpsertDto;
import com.courses_polytech.movie_backend.models.dtos.ExternalMovieDto;
import com.courses_polytech.movie_backend.models.dtos.MovieDto;
import com.courses_polytech.movie_backend.models.dtos.PageDto;
import com.courses_polytech.movie_backend.models.entities.Comment;
import com.courses_polytech.movie_backend.models.entities.Movie;
import com.courses_polytech.movie_backend.models.entities.User;
import com.courses_polytech.movie_backend.repositories.CommentRepository;
import com.courses_polytech.movie_backend.repositories.MovieRepository;
import com.courses_polytech.movie_backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class MovieService {

    private final ExternalApiClient externalApiClient;
    private final MovieRepository movieRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final MovieConverter movieConverter;
    private final CommentConverter commentConverter;
    private final CurrentUser currentUser;

    @Transactional(readOnly = true)
    public MovieDto getMovieById(UUID id) {
        log.info("Getting movie by id {}", id);
        return movieRepository.findById(id)
                .map(movieConverter::mapToDto)
                .orElseThrow(() -> new NotFoundException("Movie not found"));
    }

    @Transactional(readOnly = true)
    public PageDto<MovieDto> searchMovies(String query, String genres, Pageable pageable) {
        log.info("Searching movies");

        return PageDto.from(movieRepository.searchMovies(query, genres, pageable)
                .map(movieConverter::mapToDto));
    }

    @Transactional(readOnly = true)
    public PageDto<CommentDto> retrieveMovieComments(UUID movieId, Pageable pageable) {
        log.info("Retrieving comments for movie {}", movieId);

        return PageDto.from(commentRepository.findAllByMovieIdOrderByCreatedAtDesc(movieId, pageable)
                .map(commentConverter::mapToDto));

    }

    @Transactional
    public CommentDto addMovieComment(UUID movieId, CommentUpsertDto dto) {
        log.info("Adding comment for movie {}", movieId);

        Movie movie = getMovieOrThrow(movieId);
        User user = userRepository.findById(currentUser.id())
                .orElseThrow(() -> new NotFoundException("User not found"));

        Comment comment = Comment.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .movie(movie)
                .user(user)
                .build();

        return commentConverter.mapToDto(commentRepository.save(comment));
    }

    public List<MovieDto> searchMovieByExternalClient(String query) {
        log.info("Searching movie via external client with query {}", query);
        return  externalApiClient.searchMovie(query)
                .stream().map(movieConverter::mapExternalToDto)
                .toList();
    }

    @Cacheable(value = "movie-genres")
    public List<String> getGenres() {
        log.info("Getting movie genres");
        return movieRepository.findAllDistinctMovieGenres();
    }

    private Movie getMovieOrThrow(UUID id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Movie not found"));
    }
}
