package com.example.user_service.auth.registration.role;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@RequiredArgsConstructor
public enum Role {
    ADMIN(
            Set.of(
                    Permission.ADMIN_READ,
                    Permission.ADMIN_CREATE,
                    Permission.ADMIN_UPDATE,
                    Permission.ADMIN_DELETE
            )
    ),
    SUPERVISOR(
            Set.of(
                    Permission.SUPERVISOR_CREATE,
                    Permission.SUPERVISOR_UPDATE,
                    Permission.SUPERVISOR_READ
            )
    ),
    USER(
            Set.of(
                    Permission.USER_READ
            )
    );

    @Getter
    private final Set<Permission> permissions;

}
