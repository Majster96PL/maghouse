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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {


    @InjectMocks
    private AuthController authController;

    @Mock
    private AuthService authService;

    @Mock
    private UserService userService;


    @Test
    void shouldRegisterUserAndReturnTokenResponse() {
        UserRequest userRequest = new UserRequest(
                "Firstname",
                "Lastname",
                "test@example.com",
                "password123", null);

        TokenResponse expectedResponse = new TokenResponse("accessToken", "refreshToken");

        when(authService.registerUser(userRequest)).thenReturn(expectedResponse);

        TokenResponse actualResponse = authController.registerUser(userRequest);

        assertNotNull(actualResponse);
        assertEquals(expectedResponse.getAccessToken(), actualResponse.getAccessToken());
        assertEquals(expectedResponse.getRefreshToken(), actualResponse.getRefreshToken());
        verify(authService, times(1)).registerUser(userRequest);
    }

    @Test
    void shouldThrowExceptionWhenInvalidUserRequest() {
        UserRequest invalidUserRequest = new UserRequest(
                null,
                "",
                "invalid",
                "short",
                null);

        when(authService.registerUser(invalidUserRequest)).thenThrow(new IllegalArgumentException("Invalid user request"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> authController.registerUser(invalidUserRequest));
        assertEquals("Invalid user request", exception.getMessage());
        verify(authService, times(1)).registerUser(invalidUserRequest);
    }

    @Test
    void shouldLoginAndReturnTokenResponse() {
        LoginRequest loginRequest = new LoginRequest("testuser", "password123");
        TokenResponse expectedResponse = new TokenResponse("accessToken", "refreshToken");

        when(authService.login(loginRequest)).thenReturn(expectedResponse);

        TokenResponse actualResponse = authController.login(loginRequest);

        assertNotNull(actualResponse);
        assertEquals(expectedResponse.getAccessToken(), actualResponse.getAccessToken());
        assertEquals(expectedResponse.getRefreshToken(), actualResponse.getRefreshToken());
        verify(authService, times(1)).login(loginRequest);
    }

    @Test
    void shouldThrowExceptionWhenInvalidLoginRequest() {
        LoginRequest invalidLoginRequest = new LoginRequest("", "password123");

        when(authService.login(invalidLoginRequest)).thenThrow(new IllegalArgumentException("Invalid login request"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> authController.login(invalidLoginRequest));
        assertEquals("Invalid login request", exception.getMessage());
        verify(authService, times(1)).login(invalidLoginRequest);
    }

    @Test
    void shouldRefreshToken() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        doNothing().when(authService).refreshToken(request, response);

        authController.refreshToken(request, response);

        verify(authService, times(1)).refreshToken(request, response);
    }

    @Test
    void shouldHandleExceptionDuringRefreshToken() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        doThrow(new RuntimeException("Token refresh error")).when(authService).refreshToken(request, response);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authController.refreshToken(request, response));
        assertEquals("Token refresh error", exception.getMessage());
        verify(authService, times(1)).refreshToken(request, response);
    }
}
