package com.example.maghouse.auth.login;

import com.example.maghouse.auth.AuthService;
import com.example.maghouse.auth.login.jwt.JwtService;
import com.example.maghouse.auth.registration.role.ChangeRoleRequest;
import com.example.maghouse.auth.registration.role.ChangeRoleResponse;
import com.example.maghouse.auth.registration.role.Role;
import com.example.maghouse.auth.registration.token.TokenResponse;
import com.example.maghouse.auth.registration.user.User;
import com.example.maghouse.auth.registration.user.UserRepository;
import com.example.maghouse.auth.registration.user.UserRequest;
import com.example.maghouse.auth.registration.user.UserService;
import com.example.maghouse.mapper.TokenResponseToTokenMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenResponseToTokenMapper tokenResponseToTokenMapper;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AdminService adminService;

    private User user;
    private UserRequest userRequest;
    private ChangeRoleRequest changeRoleRequest;
    private ChangeRoleResponse changeRoleResponse;
    private TokenResponse tokenResponse;

    @BeforeEach
    void setUp(){
        user = User.builder()
                .id(1L)
                .firstname("John")
                .lastname("Kovalsky")
                .email("john.kovalsky@maghouse.com")
                .password("passwordToTest")
                .role(Role.WAREHOUSEMAN)
                .items(List.of())
                .build();

        userRequest = UserRequest.builder()
                .firstname("John")
                .lastname("Doe")
                .email("johndoe@example.com")
                .password("securepassword")
                .role(Role.USER)
                .build();

        changeRoleRequest = new ChangeRoleRequest("johndoe@example.com", Role.ADMIN);

        changeRoleResponse = new ChangeRoleResponse("johndoe@example.com", Role.ADMIN);

        tokenResponse = TokenResponse.builder()
                .accessToken("jwtToken")
                .refreshToken("refreshToken")
                .build();
    }

    @Test
    void shouldReturnAllUsers() {
        List<User> expectedUsers = List.of(user);

        when(userService.findAllUsers()).thenReturn(expectedUsers);

        List<User> result = adminService.getAllUsersByAdmin();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userService, times(1)).findAllUsers();
    }

    @Test
    void shouldChangeUserRoleByAdmin() {
        when(userService.changeUserRole(changeRoleRequest)).thenReturn(changeRoleResponse);

        ChangeRoleResponse result = adminService.changeUserRoleByAdmin(changeRoleRequest);

        assertNotNull(result);
        assertEquals(changeRoleResponse, result);
        verify(userService, times(1)).changeUserRole(changeRoleRequest);
    }

    @Test
    void shouldUpdateUserByAdminWhenUserExists() {
        Long userId = 1L;

        when(userService.getUserById(userId)).thenReturn(user);
        when(userService.updateUser(userId, userRequest)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(jwtService.getToken(user)).thenReturn("jwtToken");
        when(jwtService.generateRefreshToken(user)).thenReturn("refreshToken");
        when(tokenResponseToTokenMapper.map("jwtToken", "refreshToken")).thenReturn(tokenResponse);

        TokenResponse result = adminService.updatedUserByAdmin(userId, userRequest);

        assertNotNull(result);
        assertEquals(tokenResponse, result);
        verify(userService, times(1)).getUserById(userId);
        verify(userService, times(1)).updateUser(userId, userRequest);
        verify(userRepository, times(1)).save(user);
        verify(jwtService, times(1)).getToken(user);
        verify(jwtService, times(1)).generateRefreshToken(user);
        verify(authService, times(1)).savedUserToken(user, "jwtToken");
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        Long userId = 1L;

        when(userService.getUserById(userId)).thenReturn(null);

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () ->
                adminService.updatedUserByAdmin(userId, userRequest));

        assertEquals("User with ID 1 not found", exception.getMessage());
        verify(userService, times(1)).getUserById(userId);
        verify(userService, never()).updateUser(anyLong(), any());
    }

    @Test
    void shouldDeleteUserByAdmin() {
        Long userId = 1L;

        doNothing().when(userService).deleteUser(userId);

        adminService.deleteUserByAdmin(userId);

        verify(userService, times(1)).deleteUser(userId);
    }
}
