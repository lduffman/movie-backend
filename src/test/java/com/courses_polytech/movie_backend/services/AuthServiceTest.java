package com.courses_polytech.movie_backend.services;

import com.courses_polytech.movie_backend.exceptions.UnauthorizedException;
import com.courses_polytech.movie_backend.fixtures.TestData;
import com.courses_polytech.movie_backend.models.dtos.TokenResponse;
import com.courses_polytech.movie_backend.models.entities.User;
import com.courses_polytech.movie_backend.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private TokenService tokenService;

    @InjectMocks
    private AuthService authService;

    @Test
    void login_shouldReturnToken_whenCredentialsAreValid() {
        // Given
        User user = TestData.user();
        TokenResponse expected = TokenResponse.builder().accessToken("jwt").tokenType("Bearer").expiresIn(3600).build();
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Secret1!", user.getPassword())).thenReturn(true);
        when(tokenService.issueToken(user)).thenReturn(expected);

        // When
        TokenResponse result = authService.login(user.getUsername(), "Secret1!");

        // Then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void login_shouldThrowUnauthorized_whenUserDoesNotExist() {
        when(userRepository.findByUsername("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login("unknown@example.com", "Secret1!"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Invalid username or password");

        verify(tokenService, never()).issueToken(any());
    }

    @Test
    void login_shouldThrowUnauthorized_whenPasswordIsWrong() {
        User user = TestData.user();
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", user.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(user.getUsername(), "wrong"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Invalid username or password");

        verify(tokenService, never()).issueToken(any());
    }
}
