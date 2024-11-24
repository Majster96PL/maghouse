package com.example.maghouse.auth.registration.role;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Permission {

    ADMIN_CREATE("admin:create"),
    ADMIN_READ("admin:read"),
    ADMIN_UPDATE("admin:update"),
    ADMIN_DELETE("admin:delete"),

    MANAGER_CREATE("admin:create"),
    MANAGER_READ("admin:read"),
    MANAGER_UPDATE("admin:update"),

    WAREHOUSEMAN_READ("warehouseman:read"),
    WAREHOUSEMAN_UPDATE("warehouseman:update"),

    DRIVER_READ("river:read"),

    USER_READ("user:read");


    @Getter
    private final String permission;
}
