package com.example.user_service.auth.controller;

import com.example.user_service.auth.AuthService;
import com.example.user_service.auth.registration.user.UserRequest;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping(path = "/user-service/auth")
@RestController
@AllArgsConstructor
public class AuthController {

    private AuthService authService;

    @PostMapping("/register")
    public String registeruser(UserRequest userRequest) {
        return authService.registerUser(userRequest);
    }
}
