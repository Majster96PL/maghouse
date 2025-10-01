package com.example.maghouse.auth.registration.user;


import com.example.maghouse.auth.login.jwt.JwtService;
import com.example.maghouse.mapper.UserRequestToUserMapper;
import com.example.maghouse.auth.registration.role.ChangeRoleRequest;
import com.example.maghouse.auth.registration.role.ChangeRoleResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserRequestToUserMapper userRequestToUserMapper;
    private final JwtService jwtService;

    public User registerUser(UserRequest userRequest) {
        var user = userRequestToUserMapper.map(userRequest);
        return userRepository.save(user);
    }

    public ChangeRoleResponse changeUserRole(ChangeRoleRequest changeRoleRequest) {
        var user = findByEmail(changeRoleRequest.getEmail());
        user.setRole(changeRoleRequest.getRole());
        userRepository.save(user);
        return new ChangeRoleResponse(changeRoleRequest.getEmail(), changeRoleRequest.getRole());
    }

    public User updateUser (Long id, UserRequest userRequest) {
        var user = getUserById(id);
        userRequestToUserMapper.updatedUserFromUserRequest(userRequest, user);
        return userRepository.save(user);
    }

    public User findByEmail(String email) {
        return userRepository.findUserByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found!"));
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow( () -> new UsernameNotFoundException("User not found!"));
    }

    public Optional<User> getUsersByRole(String role) {
        if (role == null || role.trim().isEmpty()) {
            throw new IllegalArgumentException("Role cannot be null or empty");
        }
        return userRepository.findByRoleName(role);
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public void deleteUser(Long id) {
        var user = getUserById(id);
        jwtService.deleteTokenByUser(user);
        userRepository.delete(user);

    }
}
