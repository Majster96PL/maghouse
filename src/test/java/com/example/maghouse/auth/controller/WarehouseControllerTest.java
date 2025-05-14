package com.example.maghouse.auth.controller;

import com.example.maghouse.auth.registration.role.Role;
import com.example.maghouse.auth.registration.user.User;
import com.example.maghouse.auth.registration.user.UserRepository;
import com.example.maghouse.warehouse.Warehouse;
import com.example.maghouse.warehouse.WarehouseRequest;
import com.example.maghouse.warehouse.WarehouseService;
import com.example.maghouse.warehouse.location.WarehouseLocation;
import com.example.maghouse.warehouse.spacetype.WarehouseSpaceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class WarehouseControllerTest {

    @Mock
    private WarehouseService warehouseService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserDetails userDetails;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private WarehouseController warehouseController;

    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        SecurityContextHolder.setContext(securityContext);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.isAuthenticated()).thenReturn(true);
        lenient().when(authentication.getPrincipal()).thenReturn(userDetails);

        user = User.builder()
                .id(1L)
                .firstname("John")
                .lastname("Kovalsky")
                .email("john.kovalsky@maghouse.com")
                .password("testPassword")
                .role(Role.MANAGER)
                .build();

        lenient().when(userDetails.getUsername()).thenReturn(user.getEmail());
        lenient().when(userRepository.findUserByEmail(user.getEmail())).thenReturn(Optional.of(user));

    }

    @Test
    void shouldCreateWarehouseWhenUserAuthenticated() {
        WarehouseRequest request = new WarehouseRequest();
        request.setWarehouseSpaceType(WarehouseSpaceType.SHELF);
        request.setWarehouseLocation(WarehouseLocation.Rzeszow);

        Warehouse warehouse = Warehouse.builder()
                .id(1L)
                .warehouseSpaceType(WarehouseSpaceType.SHELF)
                .warehouseLocation(WarehouseLocation.Rzeszow)
                .user(user)
                .build();

        when(warehouseService.createWarehouse(request)).thenReturn(warehouse);

        Warehouse result = warehouseController.create(request);

        assertEquals(1L, result.getId());
        assertEquals(user, result.getUser());
        verify(warehouseService).createWarehouse(request);

    }
}

