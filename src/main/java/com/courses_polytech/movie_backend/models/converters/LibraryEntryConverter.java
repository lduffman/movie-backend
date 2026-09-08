package com.courses_polytech.movie_backend.models.converters;

import com.courses_polytech.movie_backend.models.dtos.LibraryEntryDto;
import com.courses_polytech.movie_backend.models.dtos.MovieDto;
import com.courses_polytech.movie_backend.models.entities.LibraryEntry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class LibraryEntryConverter {

    private final MovieConverter movieConverter;

    public LibraryEntryDto mapToDto(LibraryEntry le) {
        MovieDto movieDto = movieConverter.mapToDto(le.getMovie());
        return LibraryEntryDto.builder()
                .id(le.getId())
                .movie(movieDto)
                .rating(le.getRating())
                .watched(le.getWatched())
                .build();
    }
}
