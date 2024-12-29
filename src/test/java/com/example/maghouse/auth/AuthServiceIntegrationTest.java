package com.example.maghouse.auth;

import com.example.maghouse.auth.controller.AuthController;
import com.example.maghouse.auth.login.LoginRequest;
import com.example.maghouse.auth.login.jwt.JwtService;
import com.example.maghouse.auth.registration.role.Role;
import com.example.maghouse.auth.registration.token.TokenRepository;
import com.example.maghouse.auth.registration.token.TokenResponse;
import com.example.maghouse.auth.registration.user.User;
import com.example.maghouse.auth.registration.user.UserRequest;
import com.example.maghouse.auth.registration.user.UserService;
import com.example.maghouse.mapper.TokenResponseToTokenMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;

import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
@TestPropertySource("classpath:application-test.yml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class AuthServiceIntegrationTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private UserService userService;

    @Mock
    private JwtService jwtService;

    @Mock
    private TokenResponseToTokenMapper tokenResponseToTokenMapper;

    @Mock
    private AuthenticationManager authenticationManager;

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    private UserRequest userRequest;
    private LoginRequest loginRequest;
    private User user;
    private String jwtToken;
    private String refreshToken;

    @BeforeEach
    void setUp(){
        userRequest = new UserRequest();
        userRequest.setFirstname("John");
        userRequest.setLastname("Kovalsky");
        userRequest.setEmail("john.kovalsky@maghouse.com");
        userRequest.setPassword("passwordToTest");
        userRequest.setRole(Role.WAREHOUSEMAN);

        loginRequest = new LoginRequest();
        loginRequest.setEmail("john.kovalsky@maghouse.com");
        loginRequest.setPassword("passwordToTest");

        user = new User();
        user.setId(1L);
        user.setFirstname("John");
        user.setLastname("Kovalsky");
        user.setEmail("john.kovalsky@maghouse.com");
        user.setPassword("passwordToTest");
        user.setRole(Role.WAREHOUSEMAN);
        user.setItems(new ArrayList<>());

        jwtToken = "jwtToken";
        refreshToken = "refreshToken";

        mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(authService, userService))
                .build();
    }

    @Test
    void shouldRegisterUserSuccessfully() throws Exception {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mock(Authentication.class));

        when(userService.registerUser(any(UserRequest.class)))
                .thenReturn(user);

        when(jwtService.getToken(eq(user)))
                .thenReturn(jwtToken);

        when(jwtService.generateRefreshToken(eq(user)))
                .thenReturn(refreshToken);

        when(tokenResponseToTokenMapper.map(eq(jwtToken), eq(refreshToken)))
                .thenReturn(new TokenResponse(jwtToken, refreshToken));

        String responseContent = mockMvc.perform(MockMvcRequestBuilders.post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.token").value(jwtToken))
                .andExpect(MockMvcResultMatchers.jsonPath("$.refreshToken").value(refreshToken))
                .andReturn()
                .getResponse()
                .getContentAsString();

        System.out.println("Response content: " + responseContent);

        verify(userService, times(1)).registerUser(any(UserRequest.class));
        verify(jwtService, times(1)).getToken(eq(user));
        verify(jwtService, times(1)).generateRefreshToken(eq(user));
        verify(tokenResponseToTokenMapper, times(1)).map(eq(jwtToken), eq(refreshToken));
    }
    @Test
    void shouldLoginUserSuccessfully() throws Exception {
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(userService.findByEmail(eq(loginRequest.getEmail()))).thenReturn(user);
        when(jwtService.getToken(eq(user))).thenReturn(jwtToken);
        when(jwtService.generateRefreshToken(eq(user))).thenReturn(refreshToken);
        when(tokenResponseToTokenMapper.map(eq(jwtToken), eq(refreshToken)))
                .thenReturn(new TokenResponse(jwtToken, refreshToken));

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.token").value(jwtToken))
                .andExpect(MockMvcResultMatchers.jsonPath("$.refreshToken").value(refreshToken));

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userService, times(1)).findByEmail(eq(loginRequest.getEmail()));
        verify(jwtService, times(1)).getToken(eq(user));
        verify(jwtService, times(1)).generateRefreshToken(eq(user));
        verify(tokenResponseToTokenMapper, times(1)).map(eq(jwtToken), eq(refreshToken));
    }

    @Test
    void shouldRefreshTokenSuccessfully() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + refreshToken);
        when(jwtService.extractUserEmail(eq(refreshToken))).thenReturn(user.getEmail());
        when(userService.findByEmail(eq(user.getEmail()))).thenReturn(user);
        when(jwtService.isValidToken(eq(refreshToken), eq(user))).thenReturn(true);
        when(jwtService.getToken(eq(user))).thenReturn(jwtToken);
        when(tokenResponseToTokenMapper.map(eq(jwtToken), eq(refreshToken)))
                .thenReturn(new TokenResponse(jwtToken, refreshToken));

        authService.refreshToken(request, response);

        verify(request, times(1)).getHeader(HttpHeaders.AUTHORIZATION);
        verify(jwtService, times(1)).extractUserEmail(eq(refreshToken));
        verify(userService, times(1)).findByEmail(eq(user.getEmail()));
        verify(jwtService, times(1)).isValidToken(eq(refreshToken), eq(user));
        verify(jwtService, times(1)).getToken(eq(user));
        verify(tokenResponseToTokenMapper, times(1)).map(eq(jwtToken), eq(refreshToken));
    }
}
