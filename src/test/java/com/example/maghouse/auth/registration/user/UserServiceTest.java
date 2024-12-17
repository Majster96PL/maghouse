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

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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


}
