package com.example.maghouse.auth;

import com.example.maghouse.auth.login.LoginRequest;
import com.example.maghouse.auth.login.jwt.JwtService;
import com.example.maghouse.auth.registration.role.Role;
import com.example.maghouse.auth.registration.token.Token;
import com.example.maghouse.auth.registration.token.TokenRepository;
import com.example.maghouse.auth.registration.token.TokenResponse;
import com.example.maghouse.auth.registration.user.User;
import com.example.maghouse.auth.registration.user.UserRequest;
import com.example.maghouse.auth.registration.user.UserService;
import com.example.maghouse.mapper.TokenResponseToTokenMapper;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private JwtService jwtService;

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private TokenResponseToTokenMapper tokenResponseToTokenMapper;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private User user;
    private UserRequest userRequest;

    @BeforeEach
    void setUp(){
        userRequest = UserRequest.builder()
                .firstname("John")
                .lastname("Kovalsky")
                .email("john.kovalsky@maghouse.com")
                .password("password")
                .build();

        user = User.builder()
                .id(1L)
                .firstname("John")
                .lastname("Kovalsky")
                .email("john.kovalsky@maghouse.com")
                .password("password")
                .role(Role.USER)
                .items(new ArrayList<>())
                .build();
    }

    @Test
    void shouldRegisterUserAsUserWhenNoRoleProvided(){
        String jwtToken = "jwt-token";
        String refreshToken = "refreshToken";

        given(userService.registerUser(any(UserRequest.class))).willReturn(user);
        given(jwtService.getToken(user)).willReturn(jwtToken);
        given(jwtService.generateRefreshToken(user)).willReturn(refreshToken);
        given(tokenResponseToTokenMapper.map(jwtToken, refreshToken))
                .willReturn(new TokenResponse(jwtToken, refreshToken));

        TokenResponse tokenResponse = authService.registerUser(userRequest);

        assertEquals(jwtToken, tokenResponse.getAccessToken());
        assertEquals(refreshToken, tokenResponse.getRefreshToken());
        verify(userService).registerUser(userRequest);
        verify(tokenRepository).save(any(Token.class));

    }

    @Test
    void shouldAuthenticateAndGenerateTokensForLogin() {
        LoginRequest loginRequest = new LoginRequest("test@example.com", "password");
        String jwtToken = "jwt-token";
        String refreshToken = "refresh-token";

        given(userService.findByEmail(loginRequest.getEmail())).willReturn(user);
        given(jwtService.getToken(user)).willReturn(jwtToken);
        given(jwtService.generateRefreshToken(user)).willReturn(refreshToken);
        given(tokenResponseToTokenMapper.map(jwtToken, refreshToken))
                .willReturn(new TokenResponse(jwtToken, refreshToken));

        TokenResponse response = authService.login(loginRequest);

        assertEquals(jwtToken, response.getAccessToken());
        assertEquals(refreshToken, response.getRefreshToken());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(tokenRepository).save(any(Token.class));
    }

    @Test
    void shouldRevokeAllTokensWhenRefreshing() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        String refreshToken = "refresh-token";
        String jwtToken = "jwt-token";

        given(request.getHeader(HttpHeaders.AUTHORIZATION)).willReturn("Bearer " + refreshToken);
        given(jwtService.extractUserEmail(refreshToken)).willReturn(user.getEmail());
        given(userService.findByEmail(user.getEmail())).willReturn(user);
        given(jwtService.isValidToken(refreshToken, user)).willReturn(true);
        given(jwtService.getToken(user)).willReturn(jwtToken);
        given(tokenResponseToTokenMapper.map(jwtToken, refreshToken))
                .willReturn(new TokenResponse(jwtToken, refreshToken));

        ServletOutputStream servletOutputStream = mock(ServletOutputStream.class);
        given(response.getOutputStream()).willReturn(servletOutputStream);

        authService.refreshToken(request, response);

        verify(tokenRepository).save(any(Token.class));
        verify(jwtService).getToken(user);
        verify(jwtService).isValidToken(refreshToken, user);
        verify(response).getOutputStream();
    }

    @Test
    void shouldThrowExceptionWhenInvalidCredentialsAreProvidedDuringLogin() {
        LoginRequest loginRequest = new LoginRequest("invalid@example.com", "wrong-password");

        doThrow(new RuntimeException("Bad credentials"))
                .when(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));

        Exception exception = assertThrows(RuntimeException.class, () -> authService.login(loginRequest));
        assertEquals("Bad credentials", exception.getMessage());
    }
}
