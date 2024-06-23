package com.example.user_service.auth.registration.role;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Permission {

    ADMIN_CREATE("admin:create"),
    ADMIN_READ("admin:read"),
    ADMIN_UPDATE("admin:update"),
    ADMIN_DELETE("admin:delete"),

    SUPERVISOR_CREATE("admin:create"),
    SUPERVISOR_READ("admin:read"),
    SUPERVISOR_UPDATE("admin:update"),
    USER_READ("user:read");


    @Getter
    private final String permission;
}
