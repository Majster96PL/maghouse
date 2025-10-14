package com.example.maghouse.auth.controller;

import com.example.maghouse.auth.registration.role.Role;
import com.example.maghouse.auth.registration.user.User;
import com.example.maghouse.auth.registration.user.UserRepository;
import com.example.maghouse.item.ItemEntity;
import com.example.maghouse.item.ItemResponse;
import com.example.maghouse.mapper.ItemResponseToItemMapper;
import com.example.maghouse.mapper.WarehouseResponseToWarehouseMapper;
import com.example.maghouse.warehouse.WarehouseEntity;
import com.example.maghouse.warehouse.WarehouseRequest;
import com.example.maghouse.warehouse.WarehouseResponse;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class WarehouseControllerTest {

    @Mock
    private ItemResponseToItemMapper itemResponseToItemMapper;

    @Mock
    private WarehouseResponseToWarehouseMapper warehouseResponseToWarehouseMapper;

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
        WarehouseRequest request = new WarehouseRequest(WarehouseLocation.Warsaw);

        WarehouseEntity warehouseEntity = WarehouseEntity.builder()
                .id(1L)
                .warehouseLocation(WarehouseLocation.Rzeszow)
                .user(user)
                .build();

        WarehouseResponse warehouseResponse = WarehouseResponse.builder()
                .warehouseLocation(request.getWarehouseLocation())
                .userId(user.getId())
                .itemsId(new ArrayList<>())
                .build();

        when(warehouseService.createWarehouse(request, user)).thenReturn(warehouseEntity);
        when(warehouseResponseToWarehouseMapper.mapToWarehouse(warehouseEntity)).thenReturn(warehouseResponse);

        ResponseEntity<WarehouseResponse> result = warehouseController.create(request);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertNotNull(result.getBody());
        verify(warehouseService).createWarehouse(request, user);
        verify(warehouseResponseToWarehouseMapper).mapToWarehouse(warehouseEntity);

    }

    @Test
    void shouldThrowExceptionWhenCreateWarehouseFails() {
        WarehouseRequest warehouseRequest = new WarehouseRequest();

        when(warehouseService.createWarehouse(warehouseRequest, user))
                .thenThrow(new RuntimeException("Request not found!"));

        assertThrows(RuntimeException.class, () -> warehouseController.create(warehouseRequest));
        verify(warehouseService).createWarehouse(warehouseRequest, user);

    }

    @Test
    void shouldAssignSpaceTypeToItem() {
        WarehouseSpaceTypeRequest warehouseSpaceTypeRequest = new WarehouseSpaceTypeRequest(
                WarehouseSpaceType.SHELF
        );

        ItemEntity item = ItemEntity.builder()
                .id(1L)
                .name("ItemName")
                .itemCode("itemCode")
                .user(user)
                .build();
        WarehouseEntity warehouseEntity = WarehouseEntity.builder()
                .id(1L)
                .warehouseLocation(WarehouseLocation.Rzeszow)
                .user(user)
                .build();

        WarehouseResponse warehouseResponse = WarehouseResponse.builder()
                .userId(user.getId())
                .itemsId(new ArrayList<>())
                .build();

        when(warehouseService.assignWarehouseSpaceType(warehouseSpaceTypeRequest, item.getId(), user))
                .thenReturn(item);
        when(itemResponseToItemMapper.mapToItem(item)).thenReturn(new ItemResponse());

        ItemResponse result = warehouseController.assignSpaceType(item.getId(), warehouseSpaceTypeRequest).getBody();

        assertEquals(1L, item.getId());
        assertNotNull(result);
        assertEquals(item.getLocationCode(), result.getLocationCode());
        verify(warehouseService).assignWarehouseSpaceType(warehouseSpaceTypeRequest, item.getId(), user);
    }

    @Test
    void shouldThrowExceptionWhenAssignSpaceTypeFails() {
        Long id = 99L;

        WarehouseSpaceTypeRequest warehouseSpaceTypeRequest = new WarehouseSpaceTypeRequest();

        when(warehouseService.assignWarehouseSpaceType(warehouseSpaceTypeRequest, id, user))
                .thenThrow(new IllegalArgumentException("Item not found!"));

        assertThrows(IllegalArgumentException.class,
                () -> warehouseController.assignSpaceType(id, warehouseSpaceTypeRequest));
        verify(warehouseService).assignWarehouseSpaceType(warehouseSpaceTypeRequest, id, user);
    }

    @Test
    void shouldAssignWarehouseLocationToItem() {
        WarehouseLocationRequest warehouseLocationRequest = new WarehouseLocationRequest(
                WarehouseLocation.Rzeszow
        );

        ItemEntity item = ItemEntity.builder()
                .id(1L)
                .name("ItemName")
                .itemCode("itemCode")
                .locationCode("S05B")
                .user(user)
                .deliveries(null)
                .build();

        WarehouseEntity warehouseEntity = WarehouseEntity.builder()
                .id(1L)
                .warehouseLocation(WarehouseLocation.Rzeszow)
                .user(user)
                .items(List.of(item))
                .build();

        when(warehouseService.assignItemsToWarehouseLocation(warehouseLocationRequest, item.getId(), user))
                .thenReturn(item);

        item.setWarehouseEntity(warehouseEntity);

        ItemResponse itemResponse = ItemResponse.builder()
                .locationCode("RS05B")
                .userId(user.getId())
                .itemCode(item.getItemCode())
                .build();
        when(itemResponseToItemMapper.mapToItem(item)).thenReturn(itemResponse);

        ItemResponse result = warehouseController.assignWarehouseLocation(item.getId(), warehouseLocationRequest).getBody();

        assertNotNull(result);
        assertEquals("RS05B", result.getLocationCode());
        assertEquals(user.getId(), result.getUserId());
        verify(warehouseService).assignItemsToWarehouseLocation(warehouseLocationRequest, item.getId(), user);
    }

    @Test
    void shouldThrowExceptionWhenAssignWarehouseLocationFails() {
        Long id = 100L;
        WarehouseLocationRequest warehouseLocationRequest = new WarehouseLocationRequest();

        when(warehouseService.assignItemsToWarehouseLocation(warehouseLocationRequest, id, user))
                .thenThrow(new IllegalArgumentException("WarehouseEntity not found!!"));

        assertThrows(IllegalArgumentException.class,
                () -> warehouseController.assignWarehouseLocation(id, warehouseLocationRequest));
        verify(warehouseService).assignItemsToWarehouseLocation(warehouseLocationRequest, id, user);
    }

    @Test
    void shouldUpdateWarehouseLocation() {
        Long itemId = 1L;
        WarehouseLocationRequest warehouseLocationRequest = new WarehouseLocationRequest(
                WarehouseLocation.Warsaw
        );
        ItemEntity updatedItem = ItemEntity.builder()
                .id(1L)
                .name("ItemName")
                .itemCode("itemCode")
                .locationCode("WS05B")
                .user(user)
                .deliveries(null)
                .build();
        WarehouseEntity warehouseEntity = WarehouseEntity.builder()
                .id(2L)
                .warehouseLocation(WarehouseLocation.Krakow)
                .user(user)
                .items(List.of(updatedItem))
                .build();

        when(warehouseService.updatedItemsToWarehouseLocation(warehouseLocationRequest, itemId, user))
                .thenReturn(updatedItem);
        updatedItem.setWarehouseEntity(warehouseEntity);

        ItemResponse itemResponse = ItemResponse.builder()
                .locationCode("KSO5B")
                .userId(warehouseEntity.getId())
                .itemCode(updatedItem.getItemCode())
                .build();
        when(itemResponseToItemMapper.mapToItem(updatedItem)).thenReturn(itemResponse);

        ItemResponse result = warehouseController.updateWarehouseLocation(updatedItem.getId(), warehouseLocationRequest).getBody();

        assertNotNull(result);
        assertEquals("KSO5B", result.getLocationCode());
        assertEquals(updatedItem.getItemCode(), result.getItemCode());

        verify(warehouseService).updatedItemsToWarehouseLocation(warehouseLocationRequest, updatedItem.getId(), user);
    }

    @Test
    void shouldThrowExceptionWhenUpdateWarehouseLocationFails() {
        Long id = 404L;
        WarehouseLocationRequest warehouseLocationRequest = new WarehouseLocationRequest();

        when(warehouseService.updatedItemsToWarehouseLocation(warehouseLocationRequest, id, user))
                .thenThrow(new RuntimeException("Update failed!!"));

        assertThrows(RuntimeException.class, () -> warehouseController.updateWarehouseLocation(id, warehouseLocationRequest));
        verify(warehouseService).updatedItemsToWarehouseLocation(warehouseLocationRequest, id, user);
    }
}

