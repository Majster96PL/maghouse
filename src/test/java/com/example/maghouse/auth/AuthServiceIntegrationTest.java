package com.example.maghouse.auth;

import com.example.maghouse.auth.login.LoginRequest;
import com.example.maghouse.auth.login.jwt.JwtService;
import com.example.maghouse.auth.registration.role.Role;
import com.example.maghouse.auth.registration.token.TokenRepository;
import com.example.maghouse.auth.registration.token.TokenResponse;
import com.example.maghouse.auth.registration.user.User;
import com.example.maghouse.auth.registration.user.UserRequest;
import com.example.maghouse.auth.registration.user.UserService;
import com.example.maghouse.mapper.TokenResponseToTokenMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureMockMvc
@TestPropertySource("classpath:application-test.yml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class AuthServiceIntegrationTest {

    @Autowired
    private AuthService authService;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private TokenResponseToTokenMapper tokenResponseToTokenMapper;

    @MockBean
    private AuthenticationManager authenticationManager;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .firstname("John")
                .lastname("Doe")
                .email("john.doe@example.com")
                .password("password")
                .role(Role.USER)
                .items(null)
                .build();
    }

    private String generatedUniqueToken(){
        return UUID.randomUUID().toString();
    }

    @Test
    void shouldRegisterUserSuccessfully() {
        UserRequest userRequest = new UserRequest(
                "John", "Doe",
                "john.doe@example.com",
                "password",
                Role.USER);

        when(userService.registerUser(userRequest)).thenReturn(user);
        when(jwtService.getToken(user)).thenReturn("jwt_token");
        when(jwtService.generateRefreshToken(user)).thenReturn("refresh_token");
        when(tokenResponseToTokenMapper.map("jwt_token", "refresh_token"))
                .thenReturn(new TokenResponse("jwt_token", "refresh_token"));

        TokenResponse tokenResponse = authService.registerUser(userRequest);

        assertNotNull(tokenResponse);
        assertEquals("jwt_token", tokenResponse.getAccessToken());
        assertEquals("refresh_token", tokenResponse.getRefreshToken());
        verify(userService, times(1)).registerUser(userRequest);
        verify(jwtService, times(1)).getToken(user);
        verify(jwtService, times(1)).generateRefreshToken(user);
    }

    @Test
    void shouldRegisterUserWithDefaultRoleWhenRoleIsNull() {
        UserRequest userRequest = new UserRequest(
                "John",
                "Doe",
                "john.doe@example.com",
                "password",
                null);

        String jwtToken = generatedUniqueToken();
        String refreshToken = generatedUniqueToken();

        when(userService.registerUser(userRequest)).thenReturn(user);
        when(jwtService.getToken(user)).thenReturn(jwtToken);
        when(jwtService.generateRefreshToken(user)).thenReturn(refreshToken);
        when(tokenResponseToTokenMapper.map(jwtToken, refreshToken))
                .thenReturn(new TokenResponse(jwtToken, refreshToken));

        TokenResponse tokenResponse = authService.registerUser(userRequest);

        assertNotNull(tokenResponse);
        assertEquals(jwtToken, tokenResponse.getAccessToken());
        assertEquals(refreshToken, tokenResponse.getRefreshToken());
        verify(userService, times(1)).registerUser(userRequest);
        verify(jwtService, times(1)).getToken(user);
        verify(jwtService, times(1)).generateRefreshToken(user);
    }

    @Test
    void shouldThrowExceptionWhenLoginWithIncorrectPassword() {
        LoginRequest loginRequest = new LoginRequest(
                "john.doe@example.com",
                "incorrect_password");

        when(userService.findByEmail("john.doe@example.com")).thenReturn(user);
        doThrow(new BadCredentialsException("Invalid credentials")).when(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        assertThrows(BadCredentialsException.class, () -> authService.login(loginRequest));
    }

    @Test
    void shouldThrowExceptionWhenLoginWithNonExistingUser() {
        LoginRequest loginRequest = new LoginRequest("non.existing@example.com", "password");

        when(userService.findByEmail("non.existing@example.com")).thenReturn(null);

        assertThrows(NullPointerException.class, () -> authService.login(loginRequest));
    }

    @Test
    void shouldRefreshTokenWhenValidRefreshTokenProvided() throws Exception {
        String refreshToken = "invalid_refresh_token";
        String authHeader = "Bearer " + refreshToken;

        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(authHeader);
        when(jwtService.extractUserEmail(refreshToken)).thenReturn("john.doe@example.com");
        when(userService.findByEmail("john.doe@example.com")).thenReturn(user);
        when(jwtService.isValidToken(refreshToken, user)).thenReturn(false);

        authService.refreshToken(request, response);

        verify(jwtService, times(1)).extractUserEmail(refreshToken);
        verify(jwtService, times(1)).isValidToken(refreshToken, user);
        verify(jwtService, never()).getToken(any());
    }

    @Test
    void shouldNotRefreshTokenWhenNoAuthorizationHeader() throws Exception {
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);

        authService.refreshToken(request, mock(HttpServletResponse.class));

        verify(jwtService, never()).extractUserEmail(anyString());
        verify(jwtService, never()).isValidToken(anyString(), any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenRefreshingWithInvalidToken() throws Exception {
        String refreshToken = "invalid_refresh_token";
        String userEmail = "john.doe@example.com";

        when(jwtService.extractUserEmail(refreshToken)).thenReturn(userEmail);
        when(userService.findByEmail(userEmail)).thenReturn(user);
        when(jwtService.isValidToken(refreshToken, user)).thenReturn(false);

        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + refreshToken);

        authService.refreshToken(request, response);

        verify(jwtService, times(1)).isValidToken(refreshToken, user);
        verify(jwtService, never()).getToken(any());
    }
}

