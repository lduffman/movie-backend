package com.courses_polytech.movie_backend.controllers;

import com.courses_polytech.movie_backend.configurations.WebSecurityConfiguration;
import com.courses_polytech.movie_backend.exceptions.ConflictException;
import com.courses_polytech.movie_backend.exceptions.UnauthorizedException;
import com.courses_polytech.movie_backend.fixtures.TestData;
import com.courses_polytech.movie_backend.models.dtos.RegisterRequest;
import com.courses_polytech.movie_backend.models.dtos.TokenResponse;
import com.courses_polytech.movie_backend.models.dtos.UserResponse;
import com.courses_polytech.movie_backend.services.AuthService;
import com.courses_polytech.movie_backend.services.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(WebSecurityConfiguration.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;
    @MockitoBean
    private AuthService authService;
    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void register_shouldReturn201WithoutPassword_whenRequestIsValid() throws Exception {
        UserResponse created = UserResponse.builder()
                .id(TestData.USER_ID).username("jane.doe@example.com").firstName("Jane").lastName("Doe").build();
        when(userService.registerUser(any(RegisterRequest.class))).thenReturn(created);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName": "Jane", "lastName": "Doe",
                                 "username": "jane.doe@example.com", "password": "Secret1!"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("jane.doe@example.com"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void register_shouldReturn400_whenEmailAndPasswordAreInvalid() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName": "Jane", "lastName": "Doe",
                                 "username": "not-an-email", "password": "weak"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field", hasItem("username")))
                .andExpect(jsonPath("$.errors[*].field", hasItem("password")));

        verifyNoInteractions(userService);
    }

    @Test
    void register_shouldReturn409_whenUserAlreadyExists() throws Exception {
        when(userService.registerUser(any(RegisterRequest.class))).thenThrow(new ConflictException("User already exists"));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName": "Jane", "lastName": "Doe",
                                 "username": "jane.doe@example.com", "password": "Secret1!"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("User already exists"));
    }

    @Test
    void login_shouldReturn200WithToken() throws Exception {
        when(authService.login("jane.doe@example.com", "Secret1!"))
                .thenReturn(TokenResponse.builder().accessToken("jwt-token").tokenType("Bearer").expiresIn(3600).build());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username": "jane.doe@example.com", "password": "Secret1!"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("jwt-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(3600));
    }

    @Test
    void login_shouldReturn401_whenCredentialsAreInvalid() throws Exception {
        when(authService.login("jane.doe@example.com", "wrong"))
                .thenThrow(new UnauthorizedException("Invalid username or password"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username": "jane.doe@example.com", "password": "wrong"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }

    @Test
    void login_shouldReturn400_whenFieldsAreBlank() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username": "", "password": ""}
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }
}
