package com.example.maghouse.auth.login;

import com.example.maghouse.auth.AuthService;
import com.example.maghouse.auth.login.jwt.JwtService;
import com.example.maghouse.auth.mapper.TokenResponseToTokenMapper;
import com.example.maghouse.auth.mapper.UserRequestToUserMapper;
import com.example.maghouse.auth.registration.role.ChangeRoleRequest;
import com.example.maghouse.auth.registration.role.ChangeRoleResponse;
import com.example.maghouse.auth.registration.role.Role;
import com.example.maghouse.auth.registration.token.Token;
import com.example.maghouse.auth.registration.token.TokenRepository;
import com.example.maghouse.auth.registration.token.TokenResponse;
import com.example.maghouse.auth.registration.user.User;
import com.example.maghouse.auth.registration.user.UserRepository;
import com.example.maghouse.auth.registration.user.UserRequest;
import com.example.maghouse.auth.registration.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final UserRequestToUserMapper userRequestToUserMapper;
    private final TokenResponseToTokenMapper tokenResponseToTokenMapper;
    private final TokenRepository tokenRepository;
    private final JwtService jwtService;
    private final AuthService authService;

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public ChangeRoleResponse changeUserRoleByAdmin(ChangeRoleRequest changeRoleRequest) {
       return userService.changeUserRole(changeRoleRequest);

    }

    public TokenResponse updatedUserByAdmin(Long id, UserRequest userRequest) {
        Optional<User> optionalUser = Optional.ofNullable(userService.getUserById(id));
        if(optionalUser.isPresent()) {
            var user = userService.updateUser(id, userRequest);
            return updatedUser(user);
        } else {
            throw new UsernameNotFoundException("User with ID " + id + " not found");
        }
    }

    public TokenResponse updatedUser(User user) {
        var updatedUser = userRepository.save(user);
        String jwtToken = jwtService.getToken(updatedUser);
        String refreshToken = jwtService.generateRefreshToken(updatedUser);
        authService.savedUserToken(updatedUser, jwtToken);
        return tokenResponseToTokenMapper.map(jwtToken, refreshToken);
    }

    public void deleteUserByAdmin(Long id){
        Optional<User> optionalUser= Optional.ofNullable(userService.getUserById(id));
        if(optionalUser.isPresent()) {
            var user = optionalUser.get();
            userRepository.delete(user);
            deleteTokenByAdmin(user);
        } else {
            throw new UsernameNotFoundException("User with ID " + id + " not found");
        }
    }

    protected void deleteTokenByAdmin(User user) {
        List<Token> userToken = tokenRepository.findByUser(user);
        tokenRepository.deleteAll(userToken);
    }
}
