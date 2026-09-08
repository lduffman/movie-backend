package com.courses_polytech.movie_backend.models.converters;

import com.courses_polytech.movie_backend.models.dtos.UserResponse;
import com.courses_polytech.movie_backend.models.entities.User;
import org.springframework.stereotype.Component;

@Component
public class UserConverter {

    public UserResponse mapToUserResponse (User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();
    }

}
