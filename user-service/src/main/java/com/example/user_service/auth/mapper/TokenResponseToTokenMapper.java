package com.example.user_service.auth.mapper;

import com.example.user_service.auth.registration.token.TokenResponse;
import org.springframework.stereotype.Component;

@Component
public class TokenResponseToTokenMapper implements TokenMapper<TokenResponse, TokenResponse> {
    @Override
    public TokenResponse map(String jwtToken, String refreshToken) {
        return TokenResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }
}
