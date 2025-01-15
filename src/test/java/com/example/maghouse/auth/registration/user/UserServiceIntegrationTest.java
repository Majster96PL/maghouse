package com.example.maghouse.auth.registration.user;

import com.example.maghouse.auth.registration.role.ChangeRoleRequest;
import com.example.maghouse.auth.registration.role.ChangeRoleResponse;
import com.example.maghouse.auth.registration.role.Role;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestPropertySource("classpath:application-test.yml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@AutoConfigureMockMvc
@Transactional
public class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private UserRequest userRequest;

    @BeforeEach
    void setUp() {
        userRequest = UserRequest.builder()
                .firstname("John")
                .lastname("Kovalsky")
                .email("john.kovalsky@mail.com")
                .password("password123")
                .role(Role.DRIVER)
                .build();
    }

    @Test
    void shouldRegisterUserSuccessfully() {
        User registeredUser = userService.registerUser(userRequest);

        assertNotNull(registeredUser);
        assertEquals(userRequest.getEmail(), registeredUser.getEmail());
        assertEquals(userRequest.getRole(), registeredUser.getRole());
    }

    @Test
    void shouldChangeUserRoleSuccessfully() {
        String email = "john.kovalsky@maghouse.com";
        Role newRole = Role.WAREHOUSEMAN;
        ChangeRoleRequest changeRoleRequest = new ChangeRoleRequest(email, newRole);

        User user = User.builder()
                .firstname("John")
                .lastname("Kovalsky")
                .email(email)
                .password("password123")
                .role(Role.DRIVER)
                .build();
        userRepository.save(user);

        ChangeRoleResponse changeRoleResponse = userService.changeUserRole(changeRoleRequest);

        assertNotNull(changeRoleResponse);
        assertEquals(email, changeRoleResponse.getEmail());
        assertEquals(newRole, changeRoleResponse.getNewRole());

        User updatedUser = userRepository.findUserByEmail(email).orElseThrow();
        assertEquals(newRole, updatedUser.getRole());
    }

    @Test
    void shouldUpdateUserSuccessfully() {
        User user = User.builder()
                .firstname("John")
                .lastname("Kovalsky")
                .email("john.kovalsky@mail.com")
                .password("password123")
                .role(Role.USER)
                .build();
        userRepository.save(user);

        UserRequest updateRequest = UserRequest.builder()
                .firstname("Johnny")
                .lastname("Kovalsky")
                .email("john.kovalsky@maghouse.com")
                .password("newPassword123")
                .role(Role.USER)
                .build();


        User updatedUser = userService.updateUser(user.getId(), updateRequest);

        assertNotNull(updatedUser);
        assertEquals(updateRequest.getFirstname(), updatedUser.getFirstname());
        assertEquals(updateRequest.getLastname(), updatedUser.getLastname());
        assertEquals(updateRequest.getEmail(), updatedUser.getEmail());
        assertEquals(updateRequest.getRole(), updatedUser.getRole());
    }

    @Test
    void shouldDeleteUserSuccessfully() {
        User user = User.builder()
                .firstname("John")
                .lastname("Kovalsky")
                .email("john.kovalsky@mail.com")
                .password("password123")
                .role(Role.USER)
                .build();
        userRepository.save(user);

        userService.deleteUser(user.getId());

        assertThrows(UsernameNotFoundException.class, () -> userService.getUserById(user.getId()));
    }
}
