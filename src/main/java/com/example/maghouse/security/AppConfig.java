package com.example.maghouse.security;

import com.example.maghouse.auth.registration.role.Role;
import com.example.maghouse.auth.registration.user.User;
import com.example.maghouse.auth.registration.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Random;

@Configuration
@RequiredArgsConstructor
public class AppConfig {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            User user = userRepository.findUserByEmail(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User with email not found!"));
            if (user.getRole().equals("ADMIN")) {
                System.out.println("Admin found: " + user.getEmail());
        }
        return user;
        };
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService());
        authenticationProvider.setPasswordEncoder(passwordEncoder.bCryptPasswordEncoder());
        return authenticationProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
            throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public Random random(){
        return new Random();
    }

    @Bean
    public CommandLineRunner commandLineRunner(){
        return args -> {
            if(userRepository.findUserByEmail("admin@maghouse.pl").isEmpty()){
                var admin = User.builder()
                        .firstname("Admin")
                        .lastname("Admin")
                        .email("admin@maghouse.pl")
                        .password(passwordEncoder.bCryptPasswordEncoder().encode("admin"))
                        .role(Role.ADMIN)
                        .build();
                userRepository.save(admin);
            } else {
                System.out.println("Admin user already exists!");
            }

        };
    }
}
