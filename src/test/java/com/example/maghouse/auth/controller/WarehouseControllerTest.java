package com.example.maghouse.auth.controller;

import com.example.maghouse.auth.registration.role.Role;
import com.example.maghouse.auth.registration.user.User;
import com.example.maghouse.auth.registration.user.UserRepository;
import com.example.maghouse.item.Item;
import com.example.maghouse.warehouse.Warehouse;
import com.example.maghouse.warehouse.WarehouseRequest;
import com.example.maghouse.warehouse.WarehouseService;
import com.example.maghouse.warehouse.location.WarehouseLocation;
import com.example.maghouse.warehouse.location.WarehouseLocationRequest;
import com.example.maghouse.warehouse.spacetype.WarehouseSpaceType;
import com.example.maghouse.warehouse.spacetype.WarehouseSpaceTypeRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    @Test
    void shouldThrowExceptionWhenCreateWarehouseFails(){
        WarehouseRequest warehouseRequest = new WarehouseRequest();

        when(warehouseService.createWarehouse(warehouseRequest))
                .thenThrow(new RuntimeException("Request not found!"));

        assertThrows(RuntimeException.class, () -> warehouseController.create(warehouseRequest));
        verify(warehouseService).createWarehouse(warehouseRequest);

    }


    @Test
    void shouldAssignSpaceTypeToItem(){
        WarehouseSpaceTypeRequest warehouseSpaceTypeRequest = new WarehouseSpaceTypeRequest(
                WarehouseSpaceType.SHELF
        );

        Item item = Item.builder()
                .id(1L)
                .name("ItemName")
                .itemCode("itemCode")
                .locationCode("S05B")
                .user(user)
                .deliveries(null)
                .build();

        when(warehouseService.assignLocationCode(warehouseSpaceTypeRequest, item.getId()))
                .thenReturn(item);

        Item result = warehouseController.assignSpaceType(item.getId(), warehouseSpaceTypeRequest);

        assertEquals(1L, item.getId());
        assertEquals("S05B", result.getLocationCode());
        verify(warehouseService).assignLocationCode(warehouseSpaceTypeRequest, item.getId());
    }

    @Test
    void shouldThrowExceptionWhenAssignSpaceTypeFails(){
        Long id = 99L;

        WarehouseSpaceTypeRequest warehouseSpaceTypeRequest = new WarehouseSpaceTypeRequest();

        when(warehouseService.assignLocationCode(warehouseSpaceTypeRequest, id))
                .thenThrow(new IllegalArgumentException("Item not found!"));

        assertThrows(IllegalArgumentException.class,
                () -> warehouseController.assignSpaceType(id, warehouseSpaceTypeRequest));
        verify(warehouseService).assignLocationCode(warehouseSpaceTypeRequest, id);
    }

    @Test
    void shouldAssignWarehouseLocationToItem() {
        WarehouseLocationRequest warehouseLocationRequest = new WarehouseLocationRequest(
                WarehouseLocation.Rzeszow
        );

        Item item = Item.builder()
                .id(1L)
                .name("ItemName")
                .itemCode("itemCode")
                .locationCode("RS05B")
                .user(user)
                .deliveries(null)
                .build();

        Warehouse warehouse = Warehouse.builder()
                .id(1L)
                .warehouseSpaceType(WarehouseSpaceType.SHELF)
                .warehouseLocation(WarehouseLocation.Rzeszow)
                .user(user)
                .items(List.of(item))
                .build();

        when(warehouseService.assignItemsToWarehouseLocation(warehouseLocationRequest, item.getId()))
                .thenReturn(item);

        item.setWarehouse(warehouse);

        Item result = warehouseController.assignWarehouseLocation(warehouseLocationRequest, item.getId());

        assertEquals(item.getId(), result.getId());
        assertEquals("RS05B", result.getLocationCode());
        assertEquals(warehouse.getId(), result.getWarehouse().getId());
        verify(warehouseService).assignItemsToWarehouseLocation(warehouseLocationRequest, item.getId());

    }

    @Test
    void shouldThrowExceptionWhenAssignWarehouseLocationFails(){
        Long id = 100L;

        WarehouseLocationRequest warehouseLocationRequest = new WarehouseLocationRequest();

        when(warehouseService.assignItemsToWarehouseLocation(warehouseLocationRequest, id))
                .thenThrow(new IllegalArgumentException("Warehouse not found!!"));

        assertThrows(IllegalArgumentException.class,
                () -> warehouseController.assignWarehouseLocation(warehouseLocationRequest, id));
        verify(warehouseService).assignItemsToWarehouseLocation(warehouseLocationRequest, id);
    }

    @Test
    void shouldUpdateWarehouseLocation(){
        Long itemId = 1L;

        WarehouseLocationRequest warehouseLocationRequest = new WarehouseLocationRequest(
                WarehouseLocation.Warsaw
        );

        Item updatedItem = Item.builder()
                .id(1L)
                .name("ItemName")
                .itemCode("itemCode")
                .locationCode("WS05B")
                .user(user)
                .deliveries(null)
                .build();

        Warehouse warehouse = Warehouse.builder()
                .id(2L)
                .warehouseSpaceType(WarehouseSpaceType.SHELF)
                .warehouseLocation(WarehouseLocation.Warsaw)
                .user(user)
                .items(List.of(updatedItem))
                .build();

        when(warehouseService.updatedItemsToWarehouseLocation(warehouseLocationRequest, itemId))
                .thenReturn(updatedItem);

        Item result = warehouseController.updateWarehouseLocation(warehouseLocationRequest, itemId);

        assertEquals(itemId, result.getId());
        assertEquals("WS05B", result.getLocationCode());
        verify(warehouseService).updatedItemsToWarehouseLocation(warehouseLocationRequest, itemId);

    }

    @Test
    void shouldThrowExceptionWhenUpdateWarehouseLocationFails(){
        Long id = 404L;

        WarehouseLocationRequest warehouseLocationRequest = new WarehouseLocationRequest();

        when(warehouseService.updatedItemsToWarehouseLocation(warehouseLocationRequest, id))
                .thenThrow(new RuntimeException("Update failed!!"));

        assertThrows(RuntimeException.class, () -> warehouseController.updateWarehouseLocation(warehouseLocationRequest, id));
        verify(warehouseService).updatedItemsToWarehouseLocation(warehouseLocationRequest, id);

    }
}

