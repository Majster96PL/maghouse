package com.example.user_service.auth.controller;

import com.example.user_service.auth.AuthService;
import com.example.user_service.auth.registration.token.TokenResponse;
import com.example.user_service.auth.registration.user.UserRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping(path = "/user-service/auth")
@RestController
@AllArgsConstructor
public class AuthController {

    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<TokenResponse>registerUser( @RequestBody UserRequest userRequest) {
        return ResponseEntity.ok(authService.registerUser(userRequest));
    }
}
