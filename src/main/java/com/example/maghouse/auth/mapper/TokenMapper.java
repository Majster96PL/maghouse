package com.example.maghouse.auth.mapper;

public interface TokenMapper<From, To> {

    To map(String jwtToken, String refreshToken);
}
