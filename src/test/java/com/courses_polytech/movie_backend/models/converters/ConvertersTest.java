package com.courses_polytech.movie_backend.models.converters;

import com.courses_polytech.movie_backend.fixtures.TestData;
import com.courses_polytech.movie_backend.models.dtos.CommentDto;
import com.courses_polytech.movie_backend.models.dtos.ExternalMovieDto;
import com.courses_polytech.movie_backend.models.dtos.LibraryEntryDto;
import com.courses_polytech.movie_backend.models.dtos.MovieDto;
import com.courses_polytech.movie_backend.models.dtos.UserResponse;
import com.courses_polytech.movie_backend.models.entities.Comment;
import com.courses_polytech.movie_backend.models.entities.LibraryEntry;
import com.courses_polytech.movie_backend.models.entities.Movie;
import com.courses_polytech.movie_backend.models.entities.User;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Les converters sont des fonctions pures : pas besoin de Spring ni de mock,
 * on les instancie avec new et on vérifie le mapping champ par champ.
 */
class ConvertersTest {

    private final MovieConverter movieConverter = new MovieConverter();

    @Nested
    class MovieConverterTest {

        @Test
        void mapToDto_shouldCopyAllFields() {
            Movie movie = TestData.movie();

            MovieDto dto = movieConverter.mapToDto(movie);

            assertThat(dto.getId()).isEqualTo(movie.getId());
            assertThat(dto.getExternalId()).isEqualTo("tt0133093");
            assertThat(dto.getTitle()).isEqualTo("The Matrix");
            assertThat(dto.getPlot()).isEqualTo(movie.getPlot());
            assertThat(dto.getYear()).isEqualTo(1999);
            assertThat(dto.getPosterUrl()).isEqualTo(movie.getPosterUrl());
            assertThat(dto.getRating()).isEqualByComparingTo("8.7");
            assertThat(dto.getVotes()).isEqualTo(2_000_000);
            assertThat(dto.getRuntime()).isEqualTo(136);
            assertThat(dto.getGenres()).containsExactly("Action", "Sci-Fi");
            assertThat(dto.getDirectors()).containsExactly("Lana Wachowski", "Lilly Wachowski");
            assertThat(dto.getWriters()).containsExactly("Lana Wachowski", "Lilly Wachowski");
        }

        @Test
        void mapExternalToDto_shouldCopyAllFieldsWithoutInternalId() {
            ExternalMovieDto external = new ExternalMovieDto(
                    "tt0111161", "The Shawshank Redemption", "Two imprisoned men bond.", 1994,
                    "https://example.com/poster.jpg", new BigDecimal("9.3"), 2_800_000, 142,
                    List.of("Drama"), List.of("Frank Darabont"), List.of("Stephen King", "Frank Darabont"));

            MovieDto dto = movieConverter.mapExternalToDto(external);

            assertThat(dto.getId()).as("un film externe n'a pas encore d'id en base").isNull();
            assertThat(dto.getExternalId()).isEqualTo("tt0111161");
            assertThat(dto.getTitle()).isEqualTo("The Shawshank Redemption");
            assertThat(dto.getPlot()).isEqualTo("Two imprisoned men bond.");
            assertThat(dto.getYear()).isEqualTo(1994);
            assertThat(dto.getPosterUrl()).isEqualTo("https://example.com/poster.jpg");
            assertThat(dto.getRating()).isEqualByComparingTo("9.3");
            assertThat(dto.getVotes()).isEqualTo(2_800_000);
            assertThat(dto.getRuntime()).isEqualTo(142);
            assertThat(dto.getGenres()).containsExactly("Drama");
            assertThat(dto.getDirectors()).containsExactly("Frank Darabont");
            assertThat(dto.getWriters()).containsExactly("Stephen King", "Frank Darabont");
        }
    }

    @Test
    void commentConverter_shouldBuildAuthorFromUserFirstAndLastName() {
        Comment comment = TestData.comment();

        CommentDto dto = new CommentConverter().mapToDto(comment);

        assertThat(dto.getId()).isEqualTo(comment.getId());
        assertThat(dto.getMovieId()).isEqualTo(TestData.MOVIE_ID);
        assertThat(dto.getAuthor()).isEqualTo("John Doe");
        assertThat(dto.getTitle()).isEqualTo("Masterpiece");
        assertThat(dto.getContent()).isEqualTo("Still holds up today.");
        assertThat(dto.getCreatedAt()).isEqualTo(comment.getCreatedAt());
    }

    @Test
    void libraryEntryConverter_shouldMapEntryAndNestedMovie() {
        LibraryEntry entry = TestData.libraryEntry();

        LibraryEntryDto dto = new LibraryEntryConverter(movieConverter).mapToDto(entry);

        assertThat(dto.getId()).isEqualTo(entry.getId());
        assertThat(dto.isWatched()).isTrue();
        assertThat(dto.getRating()).isEqualByComparingTo("9.0");
        assertThat(dto.getMovie().getId()).isEqualTo(TestData.MOVIE_ID);
        assertThat(dto.getMovie().getTitle()).isEqualTo("The Matrix");
    }

    @Test
    void userConverter_shouldNotExposePassword() {
        User user = TestData.user();

        UserResponse response = new UserConverter().mapToUserResponse(user);

        assertThat(response.getId()).isEqualTo(user.getId());
        assertThat(response.getUsername()).isEqualTo("john.doe@example.com");
        assertThat(response.getFirstName()).isEqualTo("John");
        assertThat(response.getLastName()).isEqualTo("Doe");
        // UserResponse n'a pas de champ password : on s'assure qu'il ne fuite pas dans la sérialisation
        assertThat(response.toString()).doesNotContain(user.getPassword());
    }
}
