package com.example.user_service.auth.registration.role;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class RoleService {

    private RoleRepository roleRepository;

    private Role getRoleEnum(String roleEnum) {
        return roleRepository.findByRoleEnum(roleEnum);
    }

    public List<Role> getAllRoles(){
        return roleRepository.findAll();
    }
}
