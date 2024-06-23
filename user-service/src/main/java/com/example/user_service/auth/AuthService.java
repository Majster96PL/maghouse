package com.example.user_service.auth;

import com.example.user_service.auth.login.jwt.JwtService;
import com.example.user_service.auth.mapper.TokenResponseToTokenMapper;
import com.example.user_service.auth.mapper.UserRequestToUserMapper;
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
    private UserRequestToUserMapper userRequestToUserMapper;
    private TokenResponseToTokenMapper tokenResponseToTokenMapper;

    public TokenResponse registerUser(UserRequest userRequest) {
        var user = userRequestToUserMapper.map(userRequest);
        var savedUser = userRepository.save(user);
        var jwtToken = jwtService.getToken(savedUser);
        savedUserToken(savedUser, jwtToken);
        return tokenResponseToTokenMapper.map(jwtToken, null);
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
