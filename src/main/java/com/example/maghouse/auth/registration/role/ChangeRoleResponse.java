package com.example.maghouse.auth.registration.role;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChangeRoleResponse {

    private String email;
    private Role newRole;
}
