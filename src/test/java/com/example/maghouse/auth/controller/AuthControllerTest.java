package com.example.maghouse.auth.controller;

import com.example.maghouse.auth.AuthService;
import com.example.maghouse.auth.exception.GlobalExceptionHandler;
import com.example.maghouse.auth.login.LoginRequest;
import com.example.maghouse.auth.login.jwt.JwtService;
import com.example.maghouse.auth.registration.role.Role;
import com.example.maghouse.auth.registration.token.TokenRepository;
import com.example.maghouse.auth.registration.token.TokenResponse;
import com.example.maghouse.auth.registration.user.User;
import com.example.maghouse.auth.registration.user.UserRequest;
import com.example.maghouse.auth.registration.user.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private TokenRepository tokenRepository;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
    }

    @Test
    void shouldRegisterUserSuccessfully() throws Exception {
        UserRequest userRequest = UserRequest.builder()
                .firstname("John")
                .lastname("Kovalsky")
                .email("john.kovalsky@maghouse.com")
                .password("testPassword")
                .role(Role.USER)
                .build();

        TokenResponse tokenResponse= TokenResponse.builder()
                .accessToken("ACCESS_TOKEN")
                .refreshToken("REFRESH_TOKEN")
                .build();

        when(authService.registerUser(userRequest)).thenReturn(tokenResponse);

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value("ACCESS_TOKEN"))
                .andExpect(jsonPath("$.refresh_token").value("REFRESH_TOKEN"));

        verify(authService, times(1)).registerUser(userRequest);
    }

    @Test
    void shouldReturnBadRequestWhenRegisteringUserWithInvalidData() throws Exception{
        UserRequest userRequest = UserRequest.builder()
                .firstname("")
                .lastname("")
                .email("invalid-email")
                .password("123456")
                .build();

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.firstname").exists())
                .andExpect(jsonPath("$.lastname").exists())
                .andExpect(jsonPath("$.email").exists())
                .andExpect(jsonPath("$.password").exists());


        verify(authService, never()).registerUser(any());
    }

    @Test
    void shouldLoginSuccessfully() throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .email("john.kovalsky@maghouse.com")
                .password("testPassword")
                .build();

        TokenResponse tokenResponse = TokenResponse.builder()
                .accessToken("accessToken123")
                .refreshToken("refreshToken123")
                .build();

        when(authService.login(loginRequest)).thenReturn(tokenResponse);

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value("accessToken123"))
                .andExpect(jsonPath("$.refresh_token").value("refreshToken123"));

        verify(authService, times(1)).login(loginRequest);
    }

    @Test
    void shouldReturnUnauthorizedWhenLoginFails() throws Exception{
        LoginRequest loginRequest = LoginRequest.builder()
                .email("john.kovalsky@maghouse.com")
                .password("wrongPassword")
                .build();

        when(authService.login(loginRequest)).thenThrow(new RuntimeException("Invalid credentials"));

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid credentials"));

        verify(authService, times(1)).login(loginRequest);
    }

    @Test
    void shouldRefreshTokenSuccessfully() throws Exception {
        doNothing().when(authService).refreshToken(any(HttpServletRequest.class), any(HttpServletResponse.class));

        mockMvc.perform(post("/auth/refresh"))
                .andExpect(status().isOk());

        verify(authService, times(1)).refreshToken(any(HttpServletRequest.class), any(HttpServletResponse.class));

    }

    @Test
    void shouldReturnUnauthorizedWhenRefreshTokenFails() throws Exception{
        doThrow(new RuntimeException("Invalid token")).when(authService).refreshToken(any(HttpServletRequest.class), any(HttpServletResponse.class));

        mockMvc.perform(post("/auth/refresh"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid token"));

        verify(authService, times(1)).refreshToken(any(HttpServletRequest.class), any(HttpServletResponse.class));
    }

    @Test
    void shouldGetUserByIdSuccessfully() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setFirstname("John");
        user.setLastname("Doe");
        user.setEmail("john.doe@example.com");
        user.setRole(Role.USER);

        when(userService.getUserById(1L)).thenReturn(user);

        mockMvc.perform(get("/auth/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstname").value("John"))
                .andExpect(jsonPath("$.lastname").value("Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.role").value("USER"));

        verify(userService, times(1)).getUserById(1L);
    }

    @Test
    void shouldReturnNotFoundWhenUserDoesNotExist() throws Exception {
        when(userService.getUserById(99L)).thenThrow(new RuntimeException("User not found"));

        mockMvc.perform(get("/auth/99")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User not found"));

        verify(userService, times(1)).getUserById(99L);
    }
}
