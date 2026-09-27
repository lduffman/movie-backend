package com.courses_polytech.movie_backend.controllers;

import com.courses_polytech.movie_backend.configurations.WebSecurityConfiguration;
import com.courses_polytech.movie_backend.exceptions.NotFoundException;
import com.courses_polytech.movie_backend.fixtures.TestData;
import com.courses_polytech.movie_backend.models.dtos.LibraryEntryDto;
import com.courses_polytech.movie_backend.models.dtos.PageDto;
import com.courses_polytech.movie_backend.services.LibraryService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static com.courses_polytech.movie_backend.fixtures.TestData.MOVIE_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LibraryController.class)
@Import(WebSecurityConfiguration.class)
class LibraryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LibraryService libraryService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    private final LibraryEntryDto entry = LibraryEntryDto.builder()
            .id(UUID.randomUUID())
            .movie(TestData.movieDto())
            .watched(true)
            .rating(new BigDecimal("8.5"))
            .build();

    @Test
    void allEndpoints_shouldReturn401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/me/library")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/me/library/{id}", MOVIE_ID)).andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/me/library/{id}", MOVIE_ID)).andExpect(status().isUnauthorized());

        verifyNoInteractions(libraryService);
    }

    @Test
    void getMyLibraryMovies_shouldPassWatchedFilterToService() throws Exception {
        when(libraryService.getMyLibrary(eq(true), any(Pageable.class)))
                .thenReturn(PageDto.<LibraryEntryDto>builder().items(List.of(entry)).page(0).size(20).total(1).build());

        mockMvc.perform(get("/me/library").param("watched", "true").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].watched").value(true))
                .andExpect(jsonPath("$.items[0].movie.title").value("The Matrix"));
    }

    @Test
    void getMyLibraryMovies_shouldPassNullWatched_whenFilterIsAbsent() throws Exception {
        when(libraryService.getMyLibrary(isNull(), any(Pageable.class)))
                .thenReturn(PageDto.<LibraryEntryDto>builder().items(List.of()).page(0).size(20).total(0).build());

        mockMvc.perform(get("/me/library").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(0));
    }

    @Test
    void getMyLibraryMovies_shouldSortByMostRecentFirst_byDefault() throws Exception {
        when(libraryService.getMyLibrary(isNull(), any(Pageable.class)))
                .thenReturn(PageDto.<LibraryEntryDto>builder().items(List.of()).page(0).size(20).total(0).build());

        mockMvc.perform(get("/me/library").with(jwt()))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(libraryService).getMyLibrary(isNull(), captor.capture());
        assertThat(captor.getValue().getSort().getOrderFor("createdAt")).isEqualTo(Sort.Order.desc("createdAt"));
    }

    @Test
    void getMyLibraryMovies_shouldAcceptSortOnMovieProperty() throws Exception {
        when(libraryService.getMyLibrary(isNull(), any(Pageable.class)))
                .thenReturn(PageDto.<LibraryEntryDto>builder().items(List.of()).page(0).size(20).total(0).build());

        mockMvc.perform(get("/me/library").param("sort", "movie.year,asc").with(jwt()))
                .andExpect(status().isOk());
    }

    @Test
    void getMyLibraryMovies_shouldReturn400_whenSortPropertyIsNotAllowed() throws Exception {
        mockMvc.perform(get("/me/library").param("sort", "user.password,asc").with(jwt()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("sort must be one of: createdAt, rating, watched, movie.title, movie.year"));

        verifyNoInteractions(libraryService);
    }

    @Test
    void getLibraryMovie_shouldReturn404_whenEntryDoesNotExist() throws Exception {
        when(libraryService.getMyLibraryEntry(MOVIE_ID)).thenThrow(new NotFoundException("No library entry found"));

        mockMvc.perform(get("/me/library/{id}", MOVIE_ID).with(jwt()))
                .andExpect(status().isNotFound());
    }

    @Test
    void upsertLibraryEntry_shouldReturn200WithEntry() throws Exception {
        when(libraryService.upsertLibraryEntry(eq(MOVIE_ID), eq(true), any(BigDecimal.class))).thenReturn(entry);

        mockMvc.perform(put("/me/library/{id}", MOVIE_ID)
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"watched": true, "rating": 8.5}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(8.5));

        verify(libraryService).upsertLibraryEntry(MOVIE_ID, true, new BigDecimal("8.5"));
    }

    @Test
    void upsertLibraryEntry_shouldReturn400_whenRatingIsOutOfRange() throws Exception {
        mockMvc.perform(put("/me/library/{id}", MOVIE_ID)
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"watched": true, "rating": 11}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("rating"));

        verifyNoInteractions(libraryService);
    }

    @Test
    void removeMovieFromLibrary_shouldReturn404_whenMovieIsNotInLibrary() throws Exception {
        doThrow(new NotFoundException("No library entry found")).when(libraryService).removeMovieFromMyLibrary(MOVIE_ID);

        mockMvc.perform(delete("/me/library/{id}", MOVIE_ID).with(jwt()))
                .andExpect(status().isNotFound());
    }

    @Test
    void removeMovieFromLibrary_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/me/library/{id}", MOVIE_ID).with(jwt()))
                .andExpect(status().isNoContent());

        verify(libraryService).removeMovieFromMyLibrary(MOVIE_ID);
    }
}
