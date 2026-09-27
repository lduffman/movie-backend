package com.courses_polytech.movie_backend.services;

import com.courses_polytech.movie_backend.exceptions.NotFoundException;
import com.courses_polytech.movie_backend.fixtures.TestData;
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
import org.junit.jupiter.api.BeforeEach;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static com.courses_polytech.movie_backend.fixtures.TestData.MOVIE_ID;
import static com.courses_polytech.movie_backend.fixtures.TestData.USER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LibraryServiceTest {

    @Mock
    private CurrentUser currentUser;
    @Mock
    private MovieRepository movieRepository;
    @Mock
    private LibraryEntryRepository libraryEntryRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private LibraryEntryConverter libraryEntryConverter;

    @InjectMocks
    private LibraryService libraryService;

    @BeforeEach
    void setUp() {
        // Toutes les méthodes du service lisent l'utilisateur courant
        when(currentUser.id()).thenReturn(USER_ID);
    }

    @Test
    void getMyLibrary_shouldReturnEntriesOfCurrentUser() {
        Pageable pageable = PageRequest.of(0, 10);
        LibraryEntry entry = TestData.libraryEntry();
        LibraryEntryDto dto = LibraryEntryDto.builder().id(entry.getId()).watched(true).build();
        when(libraryEntryRepository.findByUserIdAndWatched(USER_ID, true, pageable))
                .thenReturn(new PageImpl<>(List.of(entry), pageable, 1));
        when(libraryEntryConverter.mapToDto(entry)).thenReturn(dto);

        PageDto<LibraryEntryDto> result = libraryService.getMyLibrary(true, pageable);

        assertThat(result.getItems()).containsExactly(dto);
        assertThat(result.getTotal()).isEqualTo(1);
    }

    @Nested
    class GetMyLibraryEntry {

        @Test
        void shouldReturnEntry_whenMovieIsInLibrary() {
            LibraryEntry entry = TestData.libraryEntry();
            LibraryEntryDto dto = LibraryEntryDto.builder().id(entry.getId()).build();
            when(libraryEntryRepository.findByUserIdAndMovieId(USER_ID, MOVIE_ID)).thenReturn(Optional.of(entry));
            when(libraryEntryConverter.mapToDto(entry)).thenReturn(dto);

            assertThat(libraryService.getMyLibraryEntry(MOVIE_ID)).isEqualTo(dto);
        }

        @Test
        void shouldThrowNotFound_whenMovieIsNotInLibrary() {
            when(libraryEntryRepository.findByUserIdAndMovieId(USER_ID, MOVIE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> libraryService.getMyLibraryEntry(MOVIE_ID))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(MOVIE_ID.toString());
        }
    }

    @Nested
    class UpsertLibraryEntry {

        private final Movie movie = TestData.movie();
        private final User user = TestData.user();
        private final BigDecimal rating = new BigDecimal("7.5");

        @Test
        void shouldCreateEntry_whenMovieIsNotYetInLibrary() {
            // Given
            LibraryEntryDto expected = LibraryEntryDto.builder().watched(true).rating(rating).build();
            when(movieRepository.findById(MOVIE_ID)).thenReturn(Optional.of(movie));
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(libraryEntryRepository.findByUserIdAndMovieId(USER_ID, MOVIE_ID)).thenReturn(Optional.empty());
            when(libraryEntryRepository.save(any(LibraryEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(libraryEntryConverter.mapToDto(any(LibraryEntry.class))).thenReturn(expected);

            // When
            LibraryEntryDto result = libraryService.upsertLibraryEntry(MOVIE_ID, true, rating);

            // Then
            assertThat(result).isEqualTo(expected);
            ArgumentCaptor<LibraryEntry> captor = ArgumentCaptor.forClass(LibraryEntry.class);
            verify(libraryEntryRepository).save(captor.capture());
            LibraryEntry saved = captor.getValue();
            assertThat(saved.getId()).as("nouvelle entrée, l'id sera généré par JPA").isNull();
            assertThat(saved.getMovie()).isSameAs(movie);
            assertThat(saved.getUser()).isSameAs(user);
            assertThat(saved.getWatched()).isTrue();
            assertThat(saved.getRating()).isEqualByComparingTo("7.5");
        }

        @Test
        void shouldUpdateExistingEntry_whenMovieIsAlreadyInLibrary() {
            // Given
            LibraryEntry existing = TestData.libraryEntry().toBuilder().watched(false).rating(null).build();
            when(movieRepository.findById(MOVIE_ID)).thenReturn(Optional.of(movie));
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(libraryEntryRepository.findByUserIdAndMovieId(USER_ID, MOVIE_ID)).thenReturn(Optional.of(existing));
            when(libraryEntryRepository.save(existing)).thenReturn(existing);

            // When
            libraryService.upsertLibraryEntry(MOVIE_ID, true, rating);

            // Then : c'est bien l'entrée existante (même id) qui est modifiée puis sauvegardée
            verify(libraryEntryRepository).save(existing);
            assertThat(existing.getWatched()).isTrue();
            assertThat(existing.getRating()).isEqualByComparingTo("7.5");
        }

        @Test
        void shouldThrowNotFound_whenMovieDoesNotExist() {
            when(movieRepository.findById(MOVIE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> libraryService.upsertLibraryEntry(MOVIE_ID, true, rating))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Movie not found");

            verify(libraryEntryRepository, never()).save(any());
        }

        @Test
        void shouldThrowNotFound_whenUserDoesNotExist() {
            when(movieRepository.findById(MOVIE_ID)).thenReturn(Optional.of(movie));
            when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> libraryService.upsertLibraryEntry(MOVIE_ID, true, rating))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("User not found");

            verify(libraryEntryRepository, never()).save(any());
        }
    }

    @Nested
    class RemoveMovieFromMyLibrary {

        @Test
        void shouldDeleteEntryOfCurrentUser() {
            LibraryEntry entry = TestData.libraryEntry();
            when(libraryEntryRepository.findByUserIdAndMovieId(USER_ID, MOVIE_ID)).thenReturn(Optional.of(entry));

            libraryService.removeMovieFromMyLibrary(MOVIE_ID);

            verify(libraryEntryRepository).delete(entry);
        }

        @Test
        void shouldThrowNotFound_whenMovieIsNotInLibrary() {
            when(libraryEntryRepository.findByUserIdAndMovieId(USER_ID, MOVIE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> libraryService.removeMovieFromMyLibrary(MOVIE_ID))
                    .isInstanceOf(NotFoundException.class);

            verify(libraryEntryRepository, never()).delete(any());
        }
    }
}
