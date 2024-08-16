package com.example.maghouse.auth.mapper;

import com.example.maghouse.auth.registration.token.TokenResponse;
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
