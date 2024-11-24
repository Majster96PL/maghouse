package com.example.maghouse.auth.controller;

import com.example.maghouse.auth.login.AdminService;
import com.example.maghouse.auth.registration.role.ChangeRoleRequest;
import com.example.maghouse.auth.registration.role.ChangeRoleResponse;
import com.example.maghouse.auth.registration.token.TokenResponse;
import com.example.maghouse.auth.registration.user.UserRequest;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/auth/admin/")
@AllArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public TokenResponse updateUser( @PathVariable("id") Long id,
                                     @RequestBody UserRequest userRequest) {
        return adminService.updatedUserByAdmin(id, userRequest);
    }

    @PutMapping("/change")
    @PreAuthorize("hasRole('ADMIN')")
    public ChangeRoleResponse changeUserRoleByAdmin(@RequestBody ChangeRoleRequest changeRoleRequest){
         return adminService.changeUserRoleByAdmin(changeRoleRequest);
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void deleteUserByAdmin(@PathVariable("id") Long id) {
        adminService.deleteUserByAdmin(id);
    }

}
