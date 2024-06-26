package com.example.user_service.auth.controller;

import com.example.user_service.auth.AuthService;
import com.example.user_service.auth.login.LoginRequest;
import com.example.user_service.auth.registration.token.TokenResponse;
import com.example.user_service.auth.registration.user.UserRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping(path = "/user-service/auth")
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @PreAuthorize("hasAuthority('USER')")
    public TokenResponse registerUser( @RequestBody UserRequest userRequest) {
       return authService.registerUser(userRequest);
    }

    @PostMapping("/login")
    public TokenResponse login(@RequestBody LoginRequest loginRequest){
        return authService.login(loginRequest);
    }

    @PostMapping("/refresh")
    public void refreshToken(
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        authService.refreshToken(request, response);
    }
}
