package com.courses_polytech.movie_backend.clients;

import com.courses_polytech.movie_backend.models.dtos.ExternalMovieDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExternalApiClient {

    private final ExternalApi externalApi;

    public List<ExternalMovieDto> searchMovie(String query) {
        return externalApi.searchMovie(query);
    }
}
