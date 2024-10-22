package com.example.maghouse.auth.registration.user;


import com.example.maghouse.auth.mapper.UserRequestToUserMapper;
import com.example.maghouse.auth.registration.role.Role;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserRequestToUserMapper userRequestToUserMapper;

    public User registerUser(UserRequest userRequest) {
        var user = userRequestToUserMapper.map(userRequest);
        return userRepository.save(user);
    }

    public void changeUserRole(String email, Role role) {
        var user = findByEmail(email);
        user.setRole(role);
        userRepository.save(user);
    }

    public User findByEmail(String email) {
        return userRepository.findUserByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found!"));
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow( () -> new UsernameNotFoundException("User not found!"));
    }

}
