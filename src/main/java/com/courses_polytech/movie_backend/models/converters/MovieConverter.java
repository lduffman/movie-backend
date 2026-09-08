package com.courses_polytech.movie_backend.models.converters;

import com.courses_polytech.movie_backend.models.dtos.ExternalMovieDto;
import com.courses_polytech.movie_backend.models.dtos.MovieDto;
import com.courses_polytech.movie_backend.models.entities.Movie;
import org.springframework.stereotype.Component;

@Component
public class MovieConverter {

    public MovieDto mapToDto(Movie movie) {
        return MovieDto.builder()
                .id(movie.getId())
                .externalId(movie.getExternalId())
                .title(movie.getTitle())
                .plot(movie.getPlot())
                .year(movie.getYear())
                .posterUrl(movie.getPosterUrl())
                .rating(movie.getRating())
                .votes(movie.getVotes())
                .runtime(movie.getRuntime())
                .genres(movie.getGenres())
                .directors(movie.getDirectors())
                .writers(movie.getWriters())
                .build();
    }

    public MovieDto mapExternalToDto(ExternalMovieDto externalMovieDto) {
        return MovieDto.builder()
                .externalId(externalMovieDto.external_id())
                .title(externalMovieDto.title())
                .plot(externalMovieDto.plot())
                .year(externalMovieDto.year())
                .posterUrl(externalMovieDto.poster_url())
                .rating(externalMovieDto.rating())
                .votes(externalMovieDto.votes())
                .runtime(externalMovieDto.runtime())
                .genres(externalMovieDto.genres())
                .directors(externalMovieDto.directors())
                .writers(externalMovieDto.writers())
                .build();
    }
}
