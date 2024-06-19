package com.example.user_service.auth.registration.user;

import com.example.user_service.auth.registration.role.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Set;


@Entity
@Data
@Table(name = "users")
@AllArgsConstructor
@EqualsAndHashCode
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;
    @NotBlank(message = "Firstname required!")
    @Size(max = 20)
    private String firstname;
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
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "userId"),
            inverseJoinColumns = @JoinColumn(name = "roleId")
    )
    private Set<Role> roles;

    public User(String firstname,
                String username,
                String password,
                String email,
                Set<Role> roles) {
        this.firstname = firstname;
        this.username = username;
        this.password = password;
        this.email = email;
        this.roles = roles;
    }
}
