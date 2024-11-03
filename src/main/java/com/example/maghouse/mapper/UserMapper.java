package com.example.maghouse.mapper;


public interface UserMapper <From, To> {

    To map(From userRequest);
    void updatedUserFromUserRequest(From userRequest, To user);
}
