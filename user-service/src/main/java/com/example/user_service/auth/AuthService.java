package com.example.user_service.auth;

import com.example.user_service.auth.registration.role.Role;
import com.example.user_service.auth.registration.role.RoleEnum;
import com.example.user_service.auth.registration.user.User;
import com.example.user_service.auth.registration.user.UserRequest;
import com.example.user_service.auth.registration.user.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthService {
    private UserService userService;
    private Role role;

    private void registerUser(UserRequest userRequest) {
        userService.getNewUser(new User(
                userRequest.getFirstname(),
                userRequest.getUsername(),
                userRequest.getPassword(),
                userRequest.getEmail(),
                role.getRoleEnum(RoleEnum.USER)
        ));
    }
}
