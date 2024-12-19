package com.example.maghouse.mapper;

public interface TokenMapper<From, To> {

    To map(String jwtToken, String refreshToken);
}
