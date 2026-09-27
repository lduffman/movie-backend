package com.courses_polytech.movie_backend.controllers;

import com.courses_polytech.movie_backend.configurations.WebSecurityConfiguration;
import com.courses_polytech.movie_backend.models.dtos.MovieDto;
import com.courses_polytech.movie_backend.services.MovieService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExternalController.class)
@Import(WebSecurityConfiguration.class)
class ExternalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MovieService movieService;
    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void searchExternalMovies_shouldBePublicAndReturnMovies() throws Exception {
        when(movieService.searchMovieByExternalClient("matrix"))
                .thenReturn(List.of(MovieDto.builder().externalId("tt0133093").title("The Matrix").build()));

        mockMvc.perform(get("/external/movies").param("query", "matrix"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].externalId").value("tt0133093"))
                .andExpect(jsonPath("$[0].id").doesNotExist());
    }

    @Test
    void searchExternalMovies_shouldReturn400_whenQueryIsMissing() throws Exception {
        mockMvc.perform(get("/external/movies"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Missing required parameter 'query'"));

        verifyNoInteractions(movieService);
    }
}
