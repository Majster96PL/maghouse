package com.example.user_service.auth.mapper;

import com.example.user_service.auth.registration.user.User;
import com.example.user_service.auth.registration.user.UserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserRequestToUserMapper implements UserMapper<UserRequest, User> {
    @Override
    public User map(UserRequest userRequest) {
        return User.builder()
                .firstname(userRequest.getFirstname())
                .lastname(userRequest.getLastname())
                .email(userRequest.getEmail())
                .password(userRequest.getPassword())
                .role(userRequest.getRole())
                .build();
    }
}
