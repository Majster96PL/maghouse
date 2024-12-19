package com.example.maghouse.auth.registration.user;

import com.example.maghouse.auth.registration.role.ChangeRoleRequest;
import com.example.maghouse.auth.registration.role.ChangeRoleResponse;
import com.example.maghouse.auth.registration.role.Role;
import com.example.maghouse.mapper.UserRequestToUserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ExtendWith(SpringExtension.class)
@TestPropertySource("classpath:application-test.yml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@AutoConfigureMockMvc
public class UserServiceIntegrationTest {

    @Autowired
    private  UserService userService;
    @Autowired
    private  UserRepository userRepository;
    @Autowired
    private  UserRequestToUserMapper userRequestToUserMapper;

    private UserRequest userRequest;

    @BeforeEach
    void setUp(){
        userRequest = new UserRequest(
                "John",
                "Kovalsky",
                "john.kovalsky@gmail.com",
                "password",
                Role.DRIVER
        );
    }

    @Test
    void shouldRegisterUserSuccessfully(){
        User registeredUser = userService.registerUser(userRequest);

        assertNotNull(registeredUser);
        assertEquals(userRequest.getEmail(), registeredUser.getEmail());
        assertEquals(userRequest.getRole(), registeredUser.getRole());
    }

    @Test
    void shouldChangeUserRoleSuccessfully(){
        String email = "john.kovalsky@maghouse.com";
        Role newRole = Role.WAREHOUSEMAN;
        ChangeRoleRequest changeRoleRequest = new ChangeRoleRequest(email, newRole);

        User user = new User();
        user.setEmail(email);
        user.setRole(Role.DRIVER);
        userRepository.save(user);

        ChangeRoleResponse changeRoleResponse = userService.changeUserRole(changeRoleRequest);

        assertNotNull(changeRoleRequest);
        assertEquals(email, changeRoleRequest.getEmail());
        assertEquals(newRole, changeRoleRequest.getRole());

        User updatedUser = userRepository.findUserByEmail(email).orElseThrow();
        assertEquals(newRole, updatedUser.getRole());
    }

    @Test
    void shouldUpdateUserSuccessfully() {
        User user = new User();
        user.setFirstname("John");
        user.setLastname("Kovalsky");
        user.setEmail("john.kovalsky@maghouse.com");
        user.setPassword("passwordToTest");
        user.setRole(Role.USER);
        userRepository.save(user);

        UserRequest updateRequest = new UserRequest("Johnny", "Kovalsky", "john.kovalsky@maghouse.com", "newPassword", Role.USER);


        User updatedUser = userService.updateUser(user.getId(), updateRequest);


        assertNotNull(updatedUser);
        assertEquals(updateRequest.getFirstname(), updatedUser.getFirstname());
        assertEquals(updateRequest.getLastname(), updatedUser.getLastname());
        assertEquals(updateRequest.getEmail(), updatedUser.getEmail());
        assertEquals(updateRequest.getRole(), updatedUser.getRole());
    }

    @Test
    void shouldDeleteUserSuccessfully() {
        User user = new User();
        user.setFirstname("John");
        user.setLastname("Kovalsky");
        user.setEmail("john.kovalsky@maghouse.com");
        user.setPassword("passwordToTest");
        user.setRole(Role.USER);
        userRepository.save(user);


        userService.deleteUser(user.getId());

        assertThrows(UsernameNotFoundException.class, () -> userService.getUserById(user.getId()));
    }
}
