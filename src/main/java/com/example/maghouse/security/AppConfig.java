package com.example.maghouse.security;

import com.example.maghouse.auth.AuthService;
import com.example.maghouse.auth.registration.role.Role;
import com.example.maghouse.auth.registration.user.User;
import com.example.maghouse.auth.registration.user.UserRepository;
import com.example.maghouse.auth.registration.user.UserRequest;
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
        return username -> userRepository.findUserByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User with email not found!"));
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

   /* @Bean
    public CommandLineRunner commandLineRunner(AuthService authService){
        return args -> {
            var admin = UserRequest.builder()
                    .firstname("Admin")
                    .lastname("Admin")
                    .email("admin@maghouse.pl")
                    .password(passwordEncoder.bCryptPasswordEncoder().encode("admin"))
                    .role(Role.ADMIN)
                    .build();

            var tokenResponse = authService.registerUser(admin);

            System.out.println("Admin Access Token: " + tokenResponse.getAccessToken());
            System.out.println("Admin Refresh Token: " + tokenResponse.getRefreshToken());
        };
    }*/
}
