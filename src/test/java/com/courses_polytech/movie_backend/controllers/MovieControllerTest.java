package com.courses_polytech.movie_backend.controllers;

import com.courses_polytech.movie_backend.configurations.WebSecurityConfiguration;
import com.courses_polytech.movie_backend.exceptions.NotFoundException;
import com.courses_polytech.movie_backend.fixtures.TestData;
import com.courses_polytech.movie_backend.models.dtos.CommentDto;
import com.courses_polytech.movie_backend.models.dtos.CommentUpsertDto;
import com.courses_polytech.movie_backend.models.dtos.MovieDto;
import com.courses_polytech.movie_backend.models.dtos.PageDto;
import com.courses_polytech.movie_backend.services.MovieService;
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

import java.util.List;
import java.util.UUID;

import static com.courses_polytech.movie_backend.fixtures.TestData.MOVIE_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @WebMvcTest ne démarre que la couche web (controllers, advice, filtres de sécurité) :
 * pas de base de données, les services sont remplacés par des mocks (@MockitoBean).
 * On teste ainsi le contrat HTTP : routes, codes de retour, validation, sérialisation JSON et sécurité.
 */
@WebMvcTest(MovieController.class)
@Import(WebSecurityConfiguration.class)
class MovieControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MovieService movieService;

    // Requis par la configuration oauth2ResourceServer ; jamais appelé car jwt() court-circuite le décodage
    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void getMovieById_shouldReturn200WithMovie() throws Exception {
        when(movieService.getMovieById(MOVIE_ID)).thenReturn(TestData.movieDto());

        mockMvc.perform(get("/movies/{id}", MOVIE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(MOVIE_ID.toString()))
                .andExpect(jsonPath("$.title").value("The Matrix"));
    }

    @Test
    void getMovieById_shouldReturn404_whenServiceThrowsNotFound() throws Exception {
        when(movieService.getMovieById(MOVIE_ID)).thenThrow(new NotFoundException("Movie not found"));

        mockMvc.perform(get("/movies/{id}", MOVIE_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Movie not found"));
    }

    @Test
    void getMovieById_shouldReturn400_whenIdIsNotAUuid() throws Exception {
        mockMvc.perform(get("/movies/{id}", "not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid value for parameter 'id'"));

        verifyNoInteractions(movieService);
    }

    @Test
    void addComment_shouldReturn400_whenBodyIsMalformedJson() throws Exception {
        mockMvc.perform(post("/movies/{id}/comments", MOVIE_ID)
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Malformed request body"));

        verifyNoInteractions(movieService);
    }

    @Test
    void searchMovies_shouldPassQueryGenreAndPaginationToService() throws Exception {
        PageDto<MovieDto> page = PageDto.<MovieDto>builder()
                .items(List.of(TestData.movieDto())).page(1).size(5).total(6).build();
        when(movieService.searchMovies(eq("matrix"), eq("Action"), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/movies/search")
                        .param("q", "matrix")
                        .param("genre", "Action")
                        .param("page", "1")
                        .param("size", "5")
                        .param("sort", "year,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].title").value("The Matrix"))
                .andExpect(jsonPath("$.total").value(6));

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(movieService).searchMovies(eq("matrix"), eq("Action"), captor.capture());
        Pageable pageable = captor.getValue();
        assertThat(pageable.getPageNumber()).isEqualTo(1);
        assertThat(pageable.getPageSize()).isEqualTo(5);
        assertThat(pageable.getSort().getOrderFor("year")).isEqualTo(Sort.Order.desc("year"));
    }

    @Test
    void searchMovies_shouldReturn400_whenSortPropertyIsNotAllowed() throws Exception {
        mockMvc.perform(get("/movies/search").param("sort", "plot,asc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("sort must be one of: title, year, rating, runtime, votes"));

        verifyNoInteractions(movieService);
    }

    @Test
    void getComments_shouldReturn200WithPage() throws Exception {
        CommentDto comment = CommentDto.builder().id(UUID.randomUUID()).author("John Doe").title("Great").build();
        when(movieService.retrieveMovieComments(eq(MOVIE_ID), any(Pageable.class)))
                .thenReturn(PageDto.<CommentDto>builder().items(List.of(comment)).page(0).size(20).total(1).build());

        mockMvc.perform(get("/movies/{id}/comments", MOVIE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].author").value("John Doe"));
    }

    @Test
    void addComment_shouldReturn201_whenAuthenticatedAndBodyIsValid() throws Exception {
        CommentDto created = CommentDto.builder().id(UUID.randomUUID()).movieId(MOVIE_ID).title("Great").content("Loved it").build();
        when(movieService.addMovieComment(eq(MOVIE_ID), any(CommentUpsertDto.class))).thenReturn(created);

        mockMvc.perform(post("/movies/{id}/comments", MOVIE_ID)
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "Great", "content": "Loved it"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Great"));

        ArgumentCaptor<CommentUpsertDto> captor = ArgumentCaptor.forClass(CommentUpsertDto.class);
        verify(movieService).addMovieComment(eq(MOVIE_ID), captor.capture());
        assertThat(captor.getValue().getTitle()).isEqualTo("Great");
        assertThat(captor.getValue().getContent()).isEqualTo("Loved it");
    }

    @Test
    void addComment_shouldReturn400WithFieldErrors_whenBodyIsInvalid() throws Exception {
        mockMvc.perform(post("/movies/{id}/comments", MOVIE_ID)
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "", "content": ""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid request"))
                .andExpect(jsonPath("$.errors.length()").value(2));

        verifyNoInteractions(movieService);
    }

    @Test
    void addComment_shouldReturn401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(post("/movies/{id}/comments", MOVIE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "Great", "content": "Loved it"}
                                """))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(movieService);
    }

    @Test
    void getGenres_shouldReturnGenreList() throws Exception {
        when(movieService.getGenres()).thenReturn(List.of("Action", "Drama"));

        mockMvc.perform(get("/movies/genres"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("Action"))
                .andExpect(jsonPath("$[1]").value("Drama"));
    }
}
