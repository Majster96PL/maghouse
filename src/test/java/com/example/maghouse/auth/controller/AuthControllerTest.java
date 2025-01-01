package com.example.maghouse.auth.controller;

import com.example.maghouse.auth.AuthService;
import com.example.maghouse.auth.registration.user.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @InjectMocks
    private AuthController authController;

    private ObjectMapper objectMapper;
}
