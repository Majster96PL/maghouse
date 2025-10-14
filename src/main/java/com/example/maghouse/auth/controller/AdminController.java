package com.example.maghouse.auth.controller;

import com.example.maghouse.auth.login.AdminService;
import com.example.maghouse.auth.registration.role.ChangeRoleRequest;
import com.example.maghouse.auth.registration.role.ChangeRoleResponse;
import com.example.maghouse.auth.registration.token.TokenResponse;
import com.example.maghouse.auth.registration.user.User;
import com.example.maghouse.auth.registration.user.UserRequest;
import com.example.maghouse.auth.registration.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping(path = "/auth/admin/")
@AllArgsConstructor
@Tag(name = "Admin", description = "User account management")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final AdminService adminService;
    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Retrive a list of all users",
            description = "Operation available only for ADMIN. Returns a list of all users accounts.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrived list of all users",
                    content = @Content(schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "403", description = "Access denied (ADMIN role required!)",
                    content = @Content)
    })
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = adminService.getAllUsersByAdmin();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Retrieve user details by ID",
            description = "Operation available only for ADMIN. Returns detailed user data based on the unique user ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved user details",
                    content = @Content(schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "403", description = "Access denied (ADMIN role required)",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "User with the given ID was not found",
                    content = @Content(schema = @Schema(implementation = Map.class)))
    })
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        return ResponseEntity.ok(user);
    }

    @GetMapping("/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Retrive a list of users by role", description = "Operation available for ADMIN, Returns a list of users by role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved users by role",
                    content = @Content(schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "400", description = "Invalid role provided",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Access denied (ADMIN role required)",
                    content = @Content)
    })
    public ResponseEntity<Optional<User>> getUserByRole(@PathVariable("role") String role) {
        if (role == null || role.trim().isEmpty()) {
            throw new RuntimeException("Invalid role provided");
        }
        Optional<User> users = adminService.getUsersByRoleByAdmin(role);
        return ResponseEntity.ok(users);
    }

    @PutMapping("/user/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update user data",
            description = "Operation available only for ADMIN. Updates user details (e.g., email, name).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User successfully updated",
                    content = @Content(schema = @Schema(implementation = TokenResponse.class))),
            @ApiResponse(responseCode = "404", description = "User does not exist",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Validation error (e.g., UserRequest is null)",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Access denied",
                    content = @Content)
    })
    public TokenResponse updateUser(@PathVariable("id") Long id,
                                    @RequestBody UserRequest userRequest) {
        if (userRequest == null) {
            throw new IllegalArgumentException("User request cannot be null");
        }
        return adminService.updatedUserByAdmin(id, userRequest);
    }

    @PutMapping("/user/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Change user role",
            description = "Operation available only for ADMIN. Modifies the role of a specified user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Role successfully changed",
                    content = @Content(schema = @Schema(implementation = ChangeRoleResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error (e.g., role is null or invalid)",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "User does not exist",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Access denied",
                    content = @Content)
    })
    public ChangeRoleResponse changeUserRoleByAdmin(@Param("role") @RequestBody ChangeRoleRequest changeRoleRequest) {
        if (changeRoleRequest.getRole() == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }
        return adminService.changeUserRoleByAdmin(changeRoleRequest);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete user",
            description = "Operation available only for ADMIN. Deletes the user with the given ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User successfully deleted (or 204 No Content)",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "User does not exist",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Access denied",
                    content = @Content)
    })
    public void deleteUserByAdmin(@PathVariable("id") Long id) {
        adminService.deleteUserByAdmin(id);
    }

}
