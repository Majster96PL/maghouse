package com.example.user_service.registration.user;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@Entity
@Data
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;
    @NotBlank(message = "Username required!")
    @Size(max = 20)
    private String username;
    @NotBlank(message = "Password required!")
    @Size(max = 120)
    private String password;
    @NotBlank(message = "Email required!")
    @Size(max = 20)
    @Email
    private String email;
}
