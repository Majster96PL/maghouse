package com.example.maghouse.auth.controller;

import com.example.maghouse.auth.login.AdminService;
import com.example.maghouse.auth.registration.role.ChangeRoleRequest;
import com.example.maghouse.auth.registration.role.ChangeRoleResponse;
import com.example.maghouse.auth.registration.role.Role;
import com.example.maghouse.auth.registration.token.TokenResponse;
import com.example.maghouse.auth.registration.user.UserRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminControllerTest {

    @InjectMocks
    private AdminController adminController;

    @Mock
    private AdminService adminService;

    private UserRequest userRequest;
    private ChangeRoleRequest changeRoleRequest;
    private TokenResponse tokenResponse;
    private ChangeRoleResponse changeRoleResponse;

    @BeforeEach
    void setUp() {

        userRequest = UserRequest.builder()
                .firstname("John")
                .lastname("Doe")
                .email("johndoe@example.com")
                .password("securepassword")
                .role(Role.USER)
                .build();

        changeRoleRequest = new ChangeRoleRequest("johndoe@example.com", Role.ADMIN);

        tokenResponse = TokenResponse.builder()
                .accessToken("test-access-token")
                .refreshToken("test-refresh-token")
                .build();

        changeRoleResponse = new ChangeRoleResponse("johndoe@example.com", Role.ADMIN);
    }

    @Test
    void shouldUpdateUserSuccessfully() {
        lenient().when(adminService.updatedUserByAdmin(eq(1L), any(UserRequest.class))).thenReturn(tokenResponse);


        TokenResponse response = adminController.updateUser(1L, userRequest);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo(tokenResponse.getAccessToken());
        assertThat(response.getRefreshToken()).isEqualTo(tokenResponse.getRefreshToken());
    }

    @Test
    void shouldChangeUserRoleSuccessfully() {
        when(adminService.changeUserRoleByAdmin(any(ChangeRoleRequest.class))).thenReturn(changeRoleResponse);

        ChangeRoleResponse response = adminController.changeUserRoleByAdmin(changeRoleRequest);

        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo(changeRoleResponse.getEmail());
        assertThat(response.getNewRole()).isEqualTo(changeRoleResponse.getNewRole());
    }

    @Test
    void shouldDeleteUserSuccessfully() {
        doNothing().when(adminService).deleteUserByAdmin(1L);

        adminController.deleteUserByAdmin(1L);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundDuringUpdate() {
        when(adminService.updatedUserByAdmin(eq(1L), any(UserRequest.class)))
                .thenThrow(new RuntimeException("User not found"));

        assertThatThrownBy(() -> adminController.updateUser(1L, userRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");
    }

    @Test
    void shouldThrowExceptionForInvalidUserRequest() {
        userRequest = null;

        assertThatThrownBy(() -> adminController.updateUser(1L, userRequest))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundDuringRoleChange() {
        when(adminService.changeUserRoleByAdmin(any(ChangeRoleRequest.class)))
                .thenThrow(new RuntimeException("User not found"));

        assertThatThrownBy(() -> adminController.changeUserRoleByAdmin(changeRoleRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");
    }

    @Test
    void shouldThrowExceptionForInvalidRoleInChangeRoleRequest() {
        changeRoleRequest = new ChangeRoleRequest("johndoe@example.com", null);

        assertThatThrownBy(() -> adminController.changeUserRoleByAdmin(changeRoleRequest))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
