package com.example.user_service.auth.mapper;

public interface TokenMapper<From, To> {

    To map(String jwtToken, String refreshToken);
}
