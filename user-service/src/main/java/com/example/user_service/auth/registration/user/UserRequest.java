package com.example.user_service.auth.registration.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRequest {
    private String firstname;
    private String username;
    private String password;
    private String email;
}
