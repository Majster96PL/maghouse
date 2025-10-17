package com.example.maghouse.auth.controller;

import com.example.maghouse.auth.registration.role.Role;
import com.example.maghouse.auth.registration.user.User;
import com.example.maghouse.item.ItemEntity;
import com.example.maghouse.item.ItemResponse;
import com.example.maghouse.mapper.ItemResponseToItemMapper;
import com.example.maghouse.mapper.WarehouseResponseToWarehouseMapper;
import com.example.maghouse.security.AuthenticationHelper;
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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WarehouseControllerTest {

    @Mock
    private ItemResponseToItemMapper itemResponseToItemMapper;

    @Mock
    private WarehouseResponseToWarehouseMapper warehouseResponseToWarehouseMapper;

    @Mock
    private WarehouseService warehouseService;

    @Mock
    private AuthenticationHelper authenticationHelper;

    @InjectMocks
    private WarehouseController warehouseController;

    private User user;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .firstname("John")
                .lastname("Kovalsky")
                .email("john.kovalsky@maghouse.com")
                .password("testPassword")
                .role(Role.MANAGER)
                .build();

        authentication = mock(Authentication.class);
        lenient().when(authentication.getName()).thenReturn(user.getEmail());
        lenient().when(authentication.isAuthenticated()).thenReturn(true);
        lenient().when(authenticationHelper.getAuthenticatedUser(authentication)).thenReturn(user);
    }

    @Test
    void shouldCreateWarehouseWhenUserAuthenticated() {
        // Given
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

        when(authenticationHelper.getAuthenticatedUser(authentication)).thenReturn(user);
        when(warehouseService.createWarehouse(request, user)).thenReturn(warehouseEntity);
        when(warehouseResponseToWarehouseMapper.mapToWarehouse(warehouseEntity)).thenReturn(warehouseResponse);

        ResponseEntity<WarehouseResponse> result = warehouseController.create(request, authentication);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertNotNull(result.getBody());
        verify(authenticationHelper).getAuthenticatedUser(authentication);
        verify(warehouseService).createWarehouse(request, user);
        verify(warehouseResponseToWarehouseMapper).mapToWarehouse(warehouseEntity);
    }

    @Test
    void shouldThrowExceptionWhenCreateWarehouseFails() {
        WarehouseRequest warehouseRequest = new WarehouseRequest();

        when(authenticationHelper.getAuthenticatedUser(authentication)).thenReturn(user);
        when(warehouseService.createWarehouse(eq(warehouseRequest), eq(user)))
                .thenThrow(new RuntimeException("Request not found!"));

        assertThrows(RuntimeException.class, () -> warehouseController.create(warehouseRequest, authentication));
        verify(authenticationHelper).getAuthenticatedUser(authentication);
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

        ItemResponse itemResponse = ItemResponse.builder().build();

        when(authenticationHelper.getAuthenticatedUser(authentication)).thenReturn(user);
        when(warehouseService.assignWarehouseSpaceType(warehouseSpaceTypeRequest, item.getId(), user))
                .thenReturn(item);
        when(itemResponseToItemMapper.mapToItem(item)).thenReturn(itemResponse);

        ResponseEntity<ItemResponse> responseEntity = warehouseController.assignSpaceType(item.getId(), warehouseSpaceTypeRequest, authentication);
        ItemResponse result = responseEntity.getBody();

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(result);
        verify(authenticationHelper).getAuthenticatedUser(authentication);
        verify(warehouseService).assignWarehouseSpaceType(warehouseSpaceTypeRequest, item.getId(), user);
    }

    @Test
    void shouldThrowExceptionWhenAssignSpaceTypeFails() {
        Long id = 99L;
        WarehouseSpaceTypeRequest warehouseSpaceTypeRequest = new WarehouseSpaceTypeRequest();

        when(authenticationHelper.getAuthenticatedUser(authentication)).thenReturn(user);
        when(warehouseService.assignWarehouseSpaceType(eq(warehouseSpaceTypeRequest), eq(id), eq(user)))
                .thenThrow(new IllegalArgumentException("Item not found!"));

        assertThrows(IllegalArgumentException.class,
                () -> warehouseController.assignSpaceType(id, warehouseSpaceTypeRequest, authentication));
        verify(authenticationHelper).getAuthenticatedUser(authentication);
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

        when(authenticationHelper.getAuthenticatedUser(authentication)).thenReturn(user);
        when(warehouseService.assignItemsToWarehouseLocation(warehouseLocationRequest, item.getId(), user))
                .thenReturn(item);

        item.setWarehouseEntity(warehouseEntity);

        ItemResponse itemResponse = ItemResponse.builder()
                .locationCode("RS05B")
                .userId(user.getId())
                .itemCode(item.getItemCode())
                .build();
        when(itemResponseToItemMapper.mapToItem(item)).thenReturn(itemResponse);

        ResponseEntity<ItemResponse> responseEntity = warehouseController.assignWarehouseLocation(item.getId(), warehouseLocationRequest, authentication);
        ItemResponse result = responseEntity.getBody();

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(result);
        assertEquals("RS05B", result.getLocationCode());
        assertEquals(user.getId(), result.getUserId());
        verify(authenticationHelper).getAuthenticatedUser(authentication);
        verify(warehouseService).assignItemsToWarehouseLocation(warehouseLocationRequest, item.getId(), user);
    }

    @Test
    void shouldThrowExceptionWhenAssignWarehouseLocationFails() {
        Long id = 100L;
        WarehouseLocationRequest warehouseLocationRequest = new WarehouseLocationRequest();

        when(authenticationHelper.getAuthenticatedUser(authentication)).thenReturn(user);
        when(warehouseService.assignItemsToWarehouseLocation(eq(warehouseLocationRequest), eq(id), eq(user)))
                .thenThrow(new IllegalArgumentException("WarehouseEntity not found!!"));

        assertThrows(IllegalArgumentException.class,
                () -> warehouseController.assignWarehouseLocation(id, warehouseLocationRequest, authentication));
        verify(authenticationHelper).getAuthenticatedUser(authentication);
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

        when(authenticationHelper.getAuthenticatedUser(authentication)).thenReturn(user);
        when(warehouseService.updatedItemsToWarehouseLocation(warehouseLocationRequest, itemId, user))
                .thenReturn(updatedItem);
        updatedItem.setWarehouseEntity(warehouseEntity);

        ItemResponse itemResponse = ItemResponse.builder()
                .locationCode("KSO5B")
                .userId(warehouseEntity.getId())
                .itemCode(updatedItem.getItemCode())
                .build();
        when(itemResponseToItemMapper.mapToItem(updatedItem)).thenReturn(itemResponse);

        ResponseEntity<ItemResponse> responseEntity = warehouseController.updateWarehouseLocation(updatedItem.getId(), warehouseLocationRequest, authentication);
        ItemResponse result = responseEntity.getBody();

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(result);
        assertEquals("KSO5B", result.getLocationCode());
        assertEquals(updatedItem.getItemCode(), result.getItemCode());
        verify(authenticationHelper).getAuthenticatedUser(authentication);
        verify(warehouseService).updatedItemsToWarehouseLocation(warehouseLocationRequest, updatedItem.getId(), user);
    }

    @Test
    void shouldThrowExceptionWhenUpdateWarehouseLocationFails() {
        Long id = 404L;
        WarehouseLocationRequest warehouseLocationRequest = new WarehouseLocationRequest();

        when(authenticationHelper.getAuthenticatedUser(authentication)).thenReturn(user);
        when(warehouseService.updatedItemsToWarehouseLocation(any(WarehouseLocationRequest.class), eq(id), eq(user)))
                .thenThrow(new RuntimeException("Update failed!!"));

        assertThrows(RuntimeException.class, () -> warehouseController.updateWarehouseLocation(id, warehouseLocationRequest, authentication));
        verify(authenticationHelper).getAuthenticatedUser(authentication);
        verify(warehouseService).updatedItemsToWarehouseLocation(warehouseLocationRequest, id, user);
    }
}

