package com.example.maghouse.unit;

import com.example.maghouse.auth.login.jwt.JwtService;
import com.example.maghouse.auth.registration.role.ChangeRoleRequest;
import com.example.maghouse.auth.registration.role.ChangeRoleResponse;
import com.example.maghouse.auth.registration.role.Role;
import com.example.maghouse.auth.registration.user.User;
import com.example.maghouse.auth.registration.user.UserRepository;
import com.example.maghouse.auth.registration.user.UserRequest;
import com.example.maghouse.auth.registration.user.UserService;
import com.example.maghouse.mapper.UserRequestToUserMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ExtendWith(MockitoExtension.class)
@Transactional
@ActiveProfiles("test")
@TestPropertySource(locations ="classpath:application-test.yml")
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserRequestToUserMapper userRequestToUserMapper;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserService userService;

    private User user;
    private UserRequest userRequest;
    private ChangeRoleRequest changeRoleRequest;

    @BeforeEach
    void setUp() {
        user = new User();
        userRequest = new UserRequest();
        changeRoleRequest = new ChangeRoleRequest();
        changeRoleRequest.setEmail("test@example.com");
        changeRoleRequest.setRole(Role.ADMIN);
    }

    @Test
    void registerUser() {
        when(userRequestToUserMapper.map(any(UserRequest.class))).thenReturn(user);
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = userService.registerUser(userRequest);

        verify(userRequestToUserMapper).map(userRequest);
        verify(userRepository).save(user);
        assertEquals(user, result);
    }

    @Test
    void changeUserRole() {
        when(userRepository.findUserByEmail(anyString())).thenReturn(Optional.of(user));

        ChangeRoleResponse response = userService.changeUserRole(changeRoleRequest);

        verify(userRepository).findUserByEmail(changeRoleRequest.getEmail());
        verify(userRepository).save(user);
        assertEquals(changeRoleRequest.getEmail(), response.getEmail());
        assertEquals(changeRoleRequest.getRole(), response.getNewRole());
    }

    @Test
    void updateUser() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = userService.updateUser(1L, userRequest);

        verify(userRequestToUserMapper).updatedUserFromUserRequest(userRequest, user);
        verify(userRepository).save(user);
        assertEquals(user, result);
    }

    @Test
    void findByEmail() {
        when(userRepository.findUserByEmail(anyString())).thenReturn(Optional.of(user));

        User result = userService.findByEmail("test@example.com");

        verify(userRepository).findUserByEmail("test@example.com");
        assertEquals(user, result);
    }

    @Test
    void getUserById() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        User result = userService.getUserById(1L);

        verify(userRepository).findById(1L);
        assertEquals(user, result);
    }

    @Test
    void deleteUser() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        userService.deleteUser(1L);

        verify(jwtService).deleteTokenByUser(user);
        verify(userRepository).delete(user);
    }
}
