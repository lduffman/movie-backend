package com.courses_polytech.movie_backend.services;

import com.courses_polytech.movie_backend.clients.ExternalApiClient;
import com.courses_polytech.movie_backend.exceptions.NotFoundException;
import com.courses_polytech.movie_backend.fixtures.TestData;
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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static com.courses_polytech.movie_backend.fixtures.TestData.MOVIE_ID;
import static com.courses_polytech.movie_backend.fixtures.TestData.USER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MovieServiceTest {

    @Mock
    private ExternalApiClient externalApiClient;
    @Mock
    private MovieRepository movieRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private MovieConverter movieConverter;
    @Mock
    private CommentConverter commentConverter;
    @Mock
    private CurrentUser currentUser;

    @InjectMocks
    private MovieService movieService;

    private final Pageable pageable = PageRequest.of(0, 20);

    @Nested
    class GetMovieById {

        @Test
        void shouldReturnMappedMovie_whenMovieExists() {
            Movie movie = TestData.movie();
            MovieDto dto = TestData.movieDto();
            when(movieRepository.findById(MOVIE_ID)).thenReturn(Optional.of(movie));
            when(movieConverter.mapToDto(movie)).thenReturn(dto);

            assertThat(movieService.getMovieById(MOVIE_ID)).isEqualTo(dto);
        }

        @Test
        void shouldThrowNotFound_whenMovieDoesNotExist() {
            when(movieRepository.findById(MOVIE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> movieService.getMovieById(MOVIE_ID))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Movie not found");
        }
    }

    @Test
    void searchMovies_shouldReturnMappedPage() {
        Movie movie = TestData.movie();
        MovieDto dto = TestData.movieDto();
        when(movieRepository.searchMovies("matrix", "Action", pageable))
                .thenReturn(new PageImpl<>(List.of(movie), pageable, 1));
        when(movieConverter.mapToDto(movie)).thenReturn(dto);

        PageDto<MovieDto> result = movieService.searchMovies("matrix", "Action", pageable);

        assertThat(result.getItems()).containsExactly(dto);
        assertThat(result.getPage()).isZero();
        assertThat(result.getSize()).isEqualTo(20);
        assertThat(result.getTotal()).isEqualTo(1);
    }

    @Test
    void retrieveMovieComments_shouldReturnMappedPage() {
        when(movieRepository.existsById(MOVIE_ID)).thenReturn(true);
        Comment comment = TestData.comment();
        CommentDto dto = CommentDto.builder().id(comment.getId()).title("Masterpiece").build();
        when(commentRepository.findAllByMovieIdOrderByCreatedAtDesc(MOVIE_ID, pageable))
                .thenReturn(new PageImpl<>(List.of(comment), pageable, 1));
        when(commentConverter.mapToDto(comment)).thenReturn(dto);

        PageDto<CommentDto> result = movieService.retrieveMovieComments(MOVIE_ID, pageable);

        assertThat(result.getItems()).containsExactly(dto);
        assertThat(result.getTotal()).isEqualTo(1);
    }

    @Test
    void retrieveMovieComments_shouldThrowNotFound_whenMovieDoesNotExist() {
        when(movieRepository.existsById(MOVIE_ID)).thenReturn(false);

        assertThatThrownBy(() -> movieService.retrieveMovieComments(MOVIE_ID, pageable))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Movie not found");

        verifyNoInteractions(commentRepository);
    }

    @Nested
    class AddMovieComment {

        private final CommentUpsertDto request = CommentUpsertDto.builder()
                .title("Great")
                .content("Loved it")
                .build();

        @Test
        void shouldSaveCommentLinkedToMovieAndCurrentUser() {
            // Given
            Movie movie = TestData.movie();
            User user = TestData.user();
            CommentDto expected = CommentDto.builder().title("Great").build();
            when(currentUser.id()).thenReturn(USER_ID);
            when(movieRepository.findById(MOVIE_ID)).thenReturn(Optional.of(movie));
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(commentConverter.mapToDto(any(Comment.class))).thenReturn(expected);

            // When
            CommentDto result = movieService.addMovieComment(MOVIE_ID, request);

            // Then
            assertThat(result).isEqualTo(expected);
            ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
            verify(commentRepository).save(captor.capture());
            Comment saved = captor.getValue();
            assertThat(saved.getTitle()).isEqualTo("Great");
            assertThat(saved.getContent()).isEqualTo("Loved it");
            assertThat(saved.getMovie()).isSameAs(movie);
            assertThat(saved.getUser()).isSameAs(user);
        }

        @Test
        void shouldThrowNotFound_whenMovieDoesNotExist() {
            when(movieRepository.findById(MOVIE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> movieService.addMovieComment(MOVIE_ID, request))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Movie not found");

            verify(commentRepository, never()).save(any());
        }

        @Test
        void shouldThrowNotFound_whenCurrentUserDoesNotExist() {
            when(currentUser.id()).thenReturn(USER_ID);
            when(movieRepository.findById(MOVIE_ID)).thenReturn(Optional.of(TestData.movie()));
            when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> movieService.addMovieComment(MOVIE_ID, request))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("User not found");

            verify(commentRepository, never()).save(any());
        }
    }

    @Test
    void searchMovieByExternalClient_shouldMapEveryExternalMovie() {
        ExternalMovieDto first = externalMovie("tt1");
        ExternalMovieDto second = externalMovie("tt2");
        MovieDto firstDto = MovieDto.builder().externalId("tt1").build();
        MovieDto secondDto = MovieDto.builder().externalId("tt2").build();
        when(externalApiClient.searchMovie("matrix")).thenReturn(List.of(first, second));
        when(movieConverter.mapExternalToDto(first)).thenReturn(firstDto);
        when(movieConverter.mapExternalToDto(second)).thenReturn(secondDto);

        List<MovieDto> result = movieService.searchMovieByExternalClient("matrix");

        assertThat(result).containsExactly(firstDto, secondDto);
        verifyNoInteractions(movieRepository);
    }

    @Test
    void getGenres_shouldReturnGenresFromRepository() {
        when(movieRepository.findAllDistinctMovieGenres()).thenReturn(List.of("Action", "Drama"));

        assertThat(movieService.getGenres()).containsExactly("Action", "Drama");
    }

    private static ExternalMovieDto externalMovie(String externalId) {
        return new ExternalMovieDto(externalId, "Title " + externalId, null, 2000, null,
                null, null, null, List.of(), List.of(), List.of());
    }
}
