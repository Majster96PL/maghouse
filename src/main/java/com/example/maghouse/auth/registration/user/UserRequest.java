package com.example.maghouse.auth.registration.user;


import com.example.maghouse.auth.registration.role.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRequest {

    @NotBlank(message = "Firstname required!")
    private String firstname;
    @NotBlank(message = "Lastname required!")
    private String lastname;
    @Email
    @NotBlank(message = "Email required!")
    private String email;
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
    private Role role;

}
