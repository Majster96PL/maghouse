package com.example.user_service.registration.user;

import com.example.user_service.security.PasswordEncoder;
import lombok.AllArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserService implements UserDetailsService {

    private static final String MESSAGE_EXCEPTON = "User with username not found";
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Optional<User> optionalUser = userRepository.findUserByUsername(username);
        if(optionalUser.isEmpty())
            throw new UsernameNotFoundException(MESSAGE_EXCEPTON);
        else{
            User user = optionalUser.get();
            return new org.springframework.security.core.userdetails.User(
                    user.getUsername(),
                    user.getPassword(),
                    user.getRoles()
                            .stream()
                            .map(role -> new SimpleGrantedAuthority(role.toString()))
                            .collect(Collectors.toSet())
            );
        }
    }

    private String getNewUser(User user) {
        boolean isUserExits = userRepository
                .findUserByUsername(user.getUsername()).isPresent();
        if (isUserExits) {
            throw new UsernameNotFoundException(MESSAGE_EXCEPTON);
        }
        String password = passwordEncoder.bCryptPasswordEncoder()
                .encode(user.getPassword());
        user.setPassword(password);
        userRepository.save(user);
        return " Create new user successfully!";
    }
}
