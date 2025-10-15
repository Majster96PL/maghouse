package com.example.maghouse.auth.controller;

import com.example.maghouse.auth.registration.user.User;
import com.example.maghouse.auth.registration.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Slf4j
public abstract class BaseController {

    protected final UserService userService;

    protected BaseController(UserService userService) {
        this.userService = userService;
    }

    protected User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Authentication failed - user not authenticated!");
            throw new SecurityException("Authentication failed - user not authenticated!");
        }
        String email = authentication.getName();
        log.debug("User authenticated: {}", email);
        return userService.findByEmail(email);
    }
}
