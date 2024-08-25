package com.example.maghouse.auth.controller;

import com.example.maghouse.auth.AuthService;
import com.example.maghouse.auth.login.LoginRequest;
import com.example.maghouse.auth.registration.token.TokenResponse;
import com.example.maghouse.auth.registration.user.UserRequest;
import com.example.maghouse.auth.registration.user.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequestMapping(path = "/auth/")
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
   // @PreAuthorize("hasAuthority('USER')")
    public TokenResponse registerUser(@RequestBody UserRequest userRequest) {
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

    @GetMapping("/users/{id}")
    public User getUserById(@PathVariable("id") Long id){
        return authService.getUserById(id);
    }
}
