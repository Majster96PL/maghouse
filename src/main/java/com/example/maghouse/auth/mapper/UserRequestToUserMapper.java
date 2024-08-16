package com.example.maghouse.auth.mapper;

import com.example.maghouse.auth.registration.role.Role;
import com.example.maghouse.auth.registration.user.UserRequest;
import com.example.maghouse.security.PasswordEncoder;
import com.example.maghouse.auth.registration.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserRequestToUserMapper implements UserMapper<UserRequest, User> {

    private final PasswordEncoder passwordEncoder;
    @Override
    public User map(UserRequest userRequest) {
        return User.builder()
                .firstname(userRequest.getFirstname())
                .lastname(userRequest.getLastname())
                .email(userRequest.getEmail())
                .password(passwordEncoder.bCryptPasswordEncoder().encode(userRequest.getPassword()))
                .role(Role.USER)
                .build();
    }
}
