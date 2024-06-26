package com.example.user_service.auth.mapper;


public interface UserMapper <From, To> {

    To map(From userRequest);
}
