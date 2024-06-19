package com.example.user_service.auth;

import com.example.user_service.auth.registration.role.Role;
import com.example.user_service.auth.registration.role.RoleEnum;
import com.example.user_service.auth.registration.role.RoleService;
import com.example.user_service.auth.registration.user.User;
import com.example.user_service.auth.registration.user.UserRequest;
import com.example.user_service.auth.registration.user.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@AllArgsConstructor
public class AuthService {
    private static final String ROLE_EXCEPTION = "Invalid role!";
    private UserService userService;
    private RoleService roleService;

    public String registerUser(UserRequest userRequest) {
        RoleEnum roleEnum = RoleEnum.USER;
        Role role = roleService.getAllRoles().stream()
                        .filter(r ->r.getRoleEnum().equals(roleEnum.toString()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(ROLE_EXCEPTION));
        User user = new User();
        user.setFirstname(userRequest.getFirstname());
        user.setUsername(userRequest.getUsername());
        user.setPassword(userRequest.getPassword());
        user.setEmail(userRequest.getEmail());
        user.setRoles(Collections.singleton(role));
        return userService.getNewUser(user);
    }
}
