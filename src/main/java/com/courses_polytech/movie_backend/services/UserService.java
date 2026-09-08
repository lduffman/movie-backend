package com.courses_polytech.movie_backend.services;

import com.courses_polytech.movie_backend.exceptions.ConflictException;
import com.courses_polytech.movie_backend.models.converters.UserConverter;
import com.courses_polytech.movie_backend.models.dtos.RegisterRequest;
import com.courses_polytech.movie_backend.models.dtos.UserResponse;
import com.courses_polytech.movie_backend.models.entities.User;
import com.courses_polytech.movie_backend.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserConverter userConverter;
    private final PasswordEncoder encoder;


    @Transactional
    public UserResponse registerUser(RegisterRequest registerRequest) {
        log.info("Registering new user");

        userRepository.findByUsername(registerRequest.getUsername())
                .ifPresent(user -> {
                    throw new ConflictException("User already exists");
                });

        String passwordHash = encoder.encode(registerRequest.getPassword());

        User newUser = User.builder()
                .firstName(registerRequest.getFirstName())
                .lastName(registerRequest.getLastName())
                .password(passwordHash)
                .username(registerRequest.getUsername())
                .build();

        return userConverter.mapToUserResponse(userRepository.save(newUser));
    }
}
