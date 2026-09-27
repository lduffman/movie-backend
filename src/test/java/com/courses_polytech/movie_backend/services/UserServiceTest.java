package com.courses_polytech.movie_backend.services;

import com.courses_polytech.movie_backend.exceptions.ConflictException;
import com.courses_polytech.movie_backend.fixtures.TestData;
import com.courses_polytech.movie_backend.models.converters.UserConverter;
import com.courses_polytech.movie_backend.models.dtos.RegisterRequest;
import com.courses_polytech.movie_backend.models.dtos.UserResponse;
import com.courses_polytech.movie_backend.models.entities.User;
import com.courses_polytech.movie_backend.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserConverter userConverter;
    @Mock
    private PasswordEncoder encoder;

    @InjectMocks
    private UserService userService;

    // Un ArgumentCaptor permet de récupérer l'objet réellement passé à un mock pour l'inspecter
    @Captor
    private ArgumentCaptor<User> userCaptor;

    private final RegisterRequest request = RegisterRequest.builder()
            .firstName("Jane")
            .lastName("Doe")
            .username("jane.doe@example.com")
            .password("Secret1!")
            .build();

    @Test
    void registerUser_shouldSaveUserWithHashedPassword() {
        // Given
        User saved = TestData.user();
        UserResponse expected = UserResponse.builder().id(saved.getId()).username(request.getUsername()).build();
        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.empty());
        when(encoder.encode("Secret1!")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenReturn(saved);
        when(userConverter.mapToUserResponse(saved)).thenReturn(expected);

        // When
        UserResponse result = userService.registerUser(request);

        // Then
        assertThat(result).isEqualTo(expected);
        verify(userRepository).save(userCaptor.capture());
        User toSave = userCaptor.getValue();
        assertThat(toSave.getUsername()).isEqualTo("jane.doe@example.com");
        assertThat(toSave.getFirstName()).isEqualTo("Jane");
        assertThat(toSave.getLastName()).isEqualTo("Doe");
        assertThat(toSave.getPassword())
                .as("le mot de passe ne doit jamais être stocké en clair")
                .isEqualTo("hashed");
    }

    @Test
    void registerUser_shouldThrowConflict_whenUsernameAlreadyExists() {
        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.of(TestData.user()));

        assertThatThrownBy(() -> userService.registerUser(request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("User already exists");

        verify(userRepository, never()).save(any());
        verify(encoder, never()).encode(any());
    }
}
