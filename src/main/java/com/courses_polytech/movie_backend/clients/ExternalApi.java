package com.courses_polytech.movie_backend.clients;

import com.courses_polytech.movie_backend.models.dtos.ExternalMovieDto;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.List;

@HttpExchange
public interface ExternalApi {

    @GetExchange("/movies/search")
    List<ExternalMovieDto> searchMovie(@RequestParam("q") String query);
}
