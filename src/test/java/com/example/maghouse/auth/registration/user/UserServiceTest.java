package com.example.maghouse.auth.registration.user;

import com.example.maghouse.auth.login.jwt.JwtService;
import com.example.maghouse.auth.registration.role.ChangeRoleRequest;
import com.example.maghouse.auth.registration.role.ChangeRoleResponse;
import com.example.maghouse.auth.registration.role.Role;
import com.example.maghouse.mapper.UserRequestToUserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserRequestToUserMapper userRequestToUserMapper;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserService userService;

    private UserRequest userRequest;
    private User user;
    private ChangeRoleRequest changeRoleRequest;

    @BeforeEach
    void setUp(){

        userRequest = new UserRequest();
        userRequest.setFirstname("John");
        userRequest.setLastname("Kovalsky");
        userRequest.setEmail("john.kovalsky@maghouse.com");
        userRequest.setPassword("passwordToTest");
        userRequest.setRole(Role.USER);

        user = new User();
        user.setId(1L);
        user.setFirstname("John");
        user.setLastname("Kovalsky");
        user.setEmail("john.kovalsky@maghouse.com");
        user.setPassword("passwordToTest");
        user.setRole(Role.USER);
        user.setItems(new ArrayList<>());
        changeRoleRequest = new ChangeRoleRequest(
                "john.kovalsky@maghouse.com", Role.WAREHOUSEMAN);

    }

    @Test
    void shouldRegisterUserSuccessfully(){
        when(userRequestToUserMapper.map(userRequest)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);

        User registerUser = userService.registerUser(userRequest);

        assertNotNull(registerUser);
        assertEquals("john.kovalsky@maghouse.com", registerUser.getEmail());
        verify(userRequestToUserMapper, times(1)).map(userRequest);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void shouldChangeUserRoleSuccessfully(){
        when(userRepository.findUserByEmail(changeRoleRequest.getEmail())).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        ChangeRoleResponse changeRoleResponse = userService.changeUserRole(changeRoleRequest);

        assertNotNull(changeRoleResponse);
        assertEquals(changeRoleRequest.getEmail(), changeRoleResponse.getEmail());
        assertEquals(changeRoleRequest.getRole(), changeRoleResponse.getNewRole());
        verify(userRepository, times(1)).findUserByEmail(changeRoleRequest.getEmail());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundWhileChangingRole(){
        when(userRepository.findUserByEmail(changeRoleRequest.getEmail())).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userService.changeUserRole(changeRoleRequest));
        verify(userRepository, times(1)).findUserByEmail(changeRoleRequest.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldUpdatedUserSuccessfully(){
        Long userId = 1l;
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        doNothing().when(userRequestToUserMapper).updatedUserFromUserRequest(userRequest, user);
        when(userRepository.save(user)).thenReturn(user);

        User updatedUser = userService.updateUser(userId, userRequest);

        assertNotNull(updatedUser);
        verify(userRepository, times(1)).findById(userId);
        verify(userRequestToUserMapper, times(1)).updatedUserFromUserRequest(userRequest, user);
        verify(userRepository, times(1)).save(user);

    }

    @Test
    void shouldFindUserByEmailSuccessfully(){
        String email = "john.kovalsky@maghouse.com";
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(user));

        User foundUser = userService.findByEmail(email);

        assertNotNull(foundUser);
        assertEquals(email, foundUser.getEmail());
        verify(userRepository, times(1)).findUserByEmail(email);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundByEmail(){
        String email = "unknown@maghouse.com";
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userService.findByEmail(email));
        verify(userRepository, times(1)).findUserByEmail(email);
    }

    @Test
    void shouldDeleteUserSuccessfully(){
        Long id = 1L;
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        doNothing().when(jwtService).deleteTokenByUser(user);
        doNothing().when(userRepository).delete(user);

        userService.deleteUser(id);

        verify(userRepository, times(1)).findById(id);
        verify(jwtService, times(1)).deleteTokenByUser(user);
        verify(userRepository, times(1)).delete(user);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundWhileDeleting(){
        Long id = 1L;
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userService.deleteUser(id));
        verify(userRepository, times(1)).findById(id);
        verify(jwtService, never()).deleteTokenByUser(any(User.class));
        verify(userRepository, never()).delete(any(User.class));

    }
}
