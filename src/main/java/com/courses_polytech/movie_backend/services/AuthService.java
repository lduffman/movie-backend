package com.courses_polytech.movie_backend.services;

import com.courses_polytech.movie_backend.exceptions.UnauthorizedException;
import com.courses_polytech.movie_backend.models.converters.UserConverter;
import com.courses_polytech.movie_backend.models.dtos.UserResponse;
import com.courses_polytech.movie_backend.models.entities.User;
import com.courses_polytech.movie_backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserConverter userConverter;

    @Transactional
    public UserResponse login(String username, String password) {
        log.info("Login request for username {} ", username);
        Optional<User> maybeUser = userRepository.findByUsername(username);

        if (maybeUser.isEmpty()) {
            throw new UnauthorizedException("Invalid username or password");
        }

        User user = maybeUser.get();

        if (!passwordEncoder.matches(password,user.getPassword())) {
            throw new UnauthorizedException("Invalid username or password");
        }

        return userConverter.mapToUserResponse(user);
    }
}
