package com.example.user_service.auth;

import com.example.user_service.auth.login.jwt.JwtService;
import com.example.user_service.auth.registration.role.Role;
import com.example.user_service.auth.registration.token.TokenResponse;
import com.example.user_service.auth.registration.user.User;
import com.example.user_service.auth.registration.user.UserRepository;
import com.example.user_service.auth.registration.user.UserRequest;
import com.example.user_service.security.PasswordEncoder;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthService {

    private PasswordEncoder passwordEncoder;
    private UserRepository userRepository;
    private JwtService jwtService;

    public TokenResponse registerUser(UserRequest userRequest) {
        var user = User.builder()
                .firstname(userRequest.getFirstname())
                .lastname(userRequest.getLastname())
                .email(userRequest.getEmail())
                .password(passwordEncoder.bCryptPasswordEncoder().encode(userRequest.getPassword()))
                .role(Role.USER)
                .build();
        userRepository.save(user);
        var jwtToken = jwtService.getToken(user);
       return TokenResponse.builder()
               .token(jwtToken)
               .build();
    }
}
