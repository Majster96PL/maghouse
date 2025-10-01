package com.example.maghouse.auth.login;

import com.example.maghouse.auth.registration.role.ChangeRoleRequest;
import com.example.maghouse.auth.registration.role.ChangeRoleResponse;
import com.example.maghouse.auth.registration.role.Role;
import com.example.maghouse.auth.registration.token.TokenResponse;
import com.example.maghouse.auth.registration.user.User;
import com.example.maghouse.auth.registration.user.UserRepository;
import com.example.maghouse.auth.registration.user.UserRequest;
import com.example.maghouse.security.PasswordEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureMockMvc
@TestPropertySource("classpath:application-test.yml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Transactional
public class AdminServiceIntegrationTest {

    @Autowired
    private AdminService adminService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User user;
    private UserRequest userRequest;
    private ChangeRoleRequest changeRoleRequest;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .firstname("John")
                .lastname("Doe")
                .email("johndoe@example.com")
                .password(passwordEncoder.bCryptPasswordEncoder().encode("securepassword"))
                .role(Role.USER)
                .items(List.of())
                .build();

        userRepository.save(user);

        userRequest = UserRequest.builder()
                .firstname("John")
                .lastname("Doe")
                .email("johndoe@example.com")
                .password("securepassword")
                .role(Role.USER)
                .build();

        changeRoleRequest = new ChangeRoleRequest("johndoe@example.com", Role.ADMIN);
    }

    @Test
    void shouldReturnAllUsers() {
        List<User> users = adminService.getAllUsersByAdmin();

        assertThat(users).isNotEmpty();
        assertThat(users.stream().anyMatch(u -> u.getEmail().equals(user.getEmail()))).isTrue();
    }

    @Test
    void shouldChangeUserRoleByAdmin() {
        ChangeRoleResponse response = adminService.changeUserRoleByAdmin(changeRoleRequest);

        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo(changeRoleRequest.getEmail());
        assertThat(response.getNewRole()).isEqualTo(changeRoleRequest.getRole());

        User updatedUser = userRepository.findUserByEmail(changeRoleRequest.getEmail()).orElseThrow();
        assertThat(updatedUser.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    void shouldUpdateUserByAdminWhenUserExists() {
        UserRequest updatedRequest = UserRequest.builder()
                .firstname("Jane")
                .lastname("Smith")
                .email("johndoe@example.com")
                .password("newpassword")
                .role(Role.USER)
                .build();

        TokenResponse response = adminService.updatedUserByAdmin(user.getId(), updatedRequest);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isNotEmpty();
        assertThat(response.getRefreshToken()).isNotEmpty();

        User updatedUser = userRepository.findById(user.getId()).orElseThrow();
        assertThat(updatedUser.getFirstname()).isEqualTo(updatedRequest.getFirstname());
        assertThat(updatedUser.getLastname()).isEqualTo(updatedRequest.getLastname());
        assertThat(passwordEncoder.bCryptPasswordEncoder().matches(updatedRequest.getPassword(), updatedUser.getPassword())).isTrue();
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        Long invalidUserId = 999L;

        Exception exception = assertThrows(UsernameNotFoundException.class, () ->
                adminService.updatedUserByAdmin(invalidUserId, userRequest));

        assertThat(exception.getMessage()).isEqualTo("User not found!");
    }

    @Test
    void shouldDeleteUserByAdmin() {
        adminService.deleteUserByAdmin(user.getId());

        Optional<User> deletedUser = userRepository.findById(user.getId());
        assertThat(deletedUser).isEmpty();
    }
}