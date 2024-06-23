package com.example.user_service.auth;

import com.example.user_service.auth.login.jwt.JwtService;
import com.example.user_service.auth.registration.role.Role;
import com.example.user_service.auth.registration.token.Token;
import com.example.user_service.auth.registration.token.TokenRepository;
import com.example.user_service.auth.registration.token.TokenResponse;
import com.example.user_service.auth.registration.token.TokenType;
import com.example.user_service.auth.registration.user.User;
import com.example.user_service.auth.registration.user.UserRepository;
import com.example.user_service.auth.registration.user.UserRequest;
import com.example.user_service.security.PasswordEncoder;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthService {

    private PasswordEncoder passwordEncoder;
    private UserRepository userRepository;
    private JwtService jwtService;
    private TokenRepository tokenRepository;

    public TokenResponse registerUser(UserRequest userRequest) {
        var user = User.builder()
                .firstname(userRequest.getFirstname())
                .lastname(userRequest.getLastname())
                .email(userRequest.getEmail())
                .password(passwordEncoder.bCryptPasswordEncoder().encode(userRequest.getPassword()))
                .role(Role.USER)
                .build();
        userRepository.save(user);
        var jwtToken = jwtService.getToken(user);
       return TokenResponse.builder()
               .token(jwtToken)
               .build();
    }

    public void savedUserToken(User user, String jwtToken) {
        var token = Token.builder()
                .token(jwtToken)
                .tokenType(TokenType.Bearer)
                .expired(false)
                .revoked(false)
                .user(user)
                .build();
    }
    private void revokeAllUserTokens(User user) {
        var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getUserId());
        if (validUserTokens.isEmpty())
            return;
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }
}
