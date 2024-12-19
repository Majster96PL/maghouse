package com.example.maghouse.auth;

import com.example.maghouse.auth.login.jwt.JwtService;
import com.example.maghouse.auth.registration.role.Role;
import com.example.maghouse.auth.registration.token.Token;
import com.example.maghouse.auth.registration.token.TokenRepository;
import com.example.maghouse.auth.registration.token.TokenResponse;
import com.example.maghouse.auth.registration.user.User;
import com.example.maghouse.auth.registration.user.UserRequest;
import com.example.maghouse.auth.registration.user.UserService;
import com.example.maghouse.mapper.TokenResponseToTokenMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

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
}
