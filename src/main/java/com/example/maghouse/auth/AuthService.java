package com.example.maghouse.auth;

import com.example.maghouse.auth.login.LoginRequest;
import com.example.maghouse.auth.login.jwt.JwtService;
import com.example.maghouse.auth.mapper.TokenResponseToTokenMapper;
import com.example.maghouse.auth.mapper.UserRequestToUserMapper;
import com.example.maghouse.auth.registration.token.Token;
import com.example.maghouse.auth.registration.token.TokenRepository;
import com.example.maghouse.auth.registration.token.TokenResponse;
import com.example.maghouse.auth.registration.token.TokenType;
import com.example.maghouse.auth.registration.user.User;
import com.example.maghouse.auth.registration.user.UserRepository;
import com.example.maghouse.auth.registration.user.UserRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@AllArgsConstructor
public class AuthService {

    private UserRepository userRepository;
    private JwtService jwtService;
    private TokenRepository tokenRepository;
    private UserRequestToUserMapper userRequestToUserMapper;
    private TokenResponseToTokenMapper tokenResponseToTokenMapper;
    private AuthenticationManager authenticationManager;

    public TokenResponse registerUser(UserRequest userRequest) {
        var user = userRequestToUserMapper.map(userRequest);
        var savedUser = userRepository.save(user);
        var jwtToken = jwtService.getToken(savedUser);
        var refreshToken = jwtService.generateRefreshToken(user);
        savedUserToken(savedUser, jwtToken);
        return tokenResponseToTokenMapper.map(jwtToken, refreshToken);
    }

    public TokenResponse login(LoginRequest loginRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );
        var user = userRepository.findUserByEmail(loginRequest.getEmail())
                .orElseThrow();
        return generatedAndRespondToken(user);
    }

    private TokenResponse generatedAndRespondToken(User user) {
        var jwtToken = jwtService.getToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        revokeAllUserTokens(user);
        savedUserToken(user, jwtToken);
        return tokenResponseToTokenMapper.map(jwtToken, refreshToken);
    }

    public void savedUserToken(User user, String jwtToken) {
        var token = Token.builder()
                .token(jwtToken)
                .tokenType(TokenType.Bearer)
                .expired(false)
                .revoked(false)
                .user(user)
                .build();
        tokenRepository.save(token);
    }

    public void refreshToken(
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }
        final String refreshToken = authHeader.substring(7);
        final String userEmail = jwtService.extractUserEmail(refreshToken);
        if (userEmail != null) {
            var user = userRepository.findUserByEmail(userEmail)
                    .orElseThrow();
            if (jwtService.isValidToken(refreshToken, user)) {
                refreshAndRespond(user, refreshToken, response);
            }
        }
    }

    private void refreshAndRespond(User user,
                                   String refreshToken,
                                   HttpServletResponse response) throws IOException {
        var accessToken = jwtService.getToken(user);
        revokeAllUserTokens(user);
        savedUserToken(user, refreshToken);
        var authResponse = tokenResponseToTokenMapper.map(accessToken, refreshToken);
        new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
    }

    private void revokeAllUserTokens(User user) {
        var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
        if (validUserTokens.isEmpty())
            return;
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow( () -> new UsernameNotFoundException("User not found!"));
    }
}
