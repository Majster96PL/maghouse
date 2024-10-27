package com.example.maghouse.auth.registration.user;


import com.example.maghouse.auth.mapper.UserRequestToUserMapper;
import com.example.maghouse.auth.registration.role.ChangeRoleRequest;
import com.example.maghouse.auth.registration.role.ChangeRoleResponse;
import com.example.maghouse.auth.registration.token.TokenRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserRequestToUserMapper userRequestToUserMapper;
    private final TokenRepository tokenRepository;

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

    public User deleteUserByAdmin(Long id) {
        Optional<User> optionalUser = Optional.ofNullable(getUserById(id));
        if (optionalUser.isPresent()) {
            var user = optionalUser.get();
            userRepository.delete(user);
            tokenRepository.deleteById(user.getId());
        } else {
            throw new UsernameNotFoundException("User with ID " + id + " not found");
        }
        return id;
    }
}
