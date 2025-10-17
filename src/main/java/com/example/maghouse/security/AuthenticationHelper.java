package com.example.maghouse.security;

import com.example.maghouse.auth.registration.user.User;
import com.example.maghouse.auth.registration.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthenticationHelper {

    private final UserService userService;

    public User getAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Authentication failed - user not authenticated!");
            throw new SecurityException("Authentication failed - user not authenticated!");
        }
        String email = authentication.getName();
        log.debug("User authenticated: {}", email);
        return userService.findByEmail(email);
    }
}
