package com.courses_polytech.movie_backend.services;

import com.courses_polytech.movie_backend.exceptions.NotFoundException;
import com.courses_polytech.movie_backend.models.CurrentUser;
import com.courses_polytech.movie_backend.models.converters.LibraryEntryConverter;
import com.courses_polytech.movie_backend.models.dtos.LibraryEntryDto;
import com.courses_polytech.movie_backend.models.dtos.PageDto;
import com.courses_polytech.movie_backend.models.entities.LibraryEntry;
import com.courses_polytech.movie_backend.models.entities.Movie;
import com.courses_polytech.movie_backend.models.entities.User;
import com.courses_polytech.movie_backend.repositories.LibraryEntryRepository;
import com.courses_polytech.movie_backend.repositories.MovieRepository;
import com.courses_polytech.movie_backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LibraryService {

    private final CurrentUser currentUser;
    private final MovieRepository movieRepository;
    private final LibraryEntryRepository libraryEntryRepository;
    private final UserRepository userRepository;
    private final LibraryEntryConverter libraryEntryConverter;

    @Transactional(readOnly = true)
    public PageDto<LibraryEntryDto> getMyLibrary(Boolean watched, Pageable pageable) {
        log.info("Get library entries for user {}", currentUser.id());
        return PageDto.from(libraryEntryRepository.findByUserIdAndWatched(currentUser.id(), watched, pageable)
                .map(libraryEntryConverter::mapToDto));
    }

    @Transactional(readOnly = true)
    public LibraryEntryDto getMyLibraryEntry(UUID movieId) {
        log.info("Get library entry for user {} and movie {}", currentUser.id(), movieId);
        return libraryEntryRepository.findByUserIdAndMovieId(currentUser.id(), movieId)
                .map(libraryEntryConverter::mapToDto)
                .orElseThrow(() -> new NotFoundException("No library entry found for user " + currentUser.id() + " and movie " + movieId));
    }

    @Transactional
    public LibraryEntryDto upsertLibraryEntry(UUID movieId, Boolean watched, BigDecimal rating) {
        log.info("Upsert movie {} to user {} library", movieId, currentUser.id());

        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new NotFoundException("Movie not found"));

        User user = userRepository.findById(currentUser.id())
                .orElseThrow(() -> new NotFoundException("User not found"));

        LibraryEntry le = libraryEntryRepository.findByUserIdAndMovieId(user.getId(), movieId)
                .map(libraryEntry -> {
                    libraryEntry.setWatched(watched);
                    libraryEntry.setRating(rating);
                    return libraryEntry;
                }).orElse(LibraryEntry.builder()
                        .movie(movie)
                        .user(user)
                        .watched(watched)
                        .rating(rating)
                        .build()
                );

        return libraryEntryConverter.mapToDto(libraryEntryRepository.save(le));
    }

    @Transactional
    public void removeMovieFromMyLibrary(UUID movieId) {
        log.info("Remove movie {} from library of user {}", movieId, currentUser.id());

        LibraryEntry entry = libraryEntryRepository.findByUserIdAndMovieId(currentUser.id(), movieId)
                .orElseThrow(() -> new NotFoundException("No library entry found for user " + currentUser.id() + " and movie " + movieId));

        libraryEntryRepository.delete(entry);
    }

}
