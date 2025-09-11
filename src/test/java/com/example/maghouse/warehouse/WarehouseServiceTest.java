package com.example.maghouse.warehouse;

import com.example.maghouse.auth.registration.role.Role;
import com.example.maghouse.auth.registration.user.User;
import com.example.maghouse.auth.registration.user.UserRepository;
import com.example.maghouse.item.ItemEntity;
import com.example.maghouse.item.ItemRepository;
import com.example.maghouse.mapper.WarehouseResponseToWarehouseMapper;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WarehouseServiceTest {

    @Mock
    private WarehouseResponseToWarehouseMapper warehouseResponseToWarehouseMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private WarehouseService warehouseService;

    private User user;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.isAuthenticated()).thenReturn(true);
        lenient().when(authentication.getPrincipal()).thenReturn(userDetails);

        user = User.builder()
                .id(1L)
                .firstname("John")
                .lastname("Kovalsky")
                .email("john.kovalsky@maghouse.com")
                .password("password123")
                .role(Role.USER)
                .build();

        lenient().when(userDetails.getUsername()).thenReturn("john.kovalsky@maghouse.com");
        lenient().when(userRepository.findUserByEmail(user.getEmail())).thenReturn(Optional.of(user));
    }

    @Test
    void shouldCreateWarehouse_WhenUserIsAuthenticated() {
        WarehouseRequest warehouseRequest = new WarehouseRequest(WarehouseSpaceType.CONTAINER, WarehouseLocation.Krakow);
        Warehouse warehouse = new Warehouse(1L, warehouseRequest.getWarehouseSpaceType(), warehouseRequest.getWarehouseLocation(), user, new ArrayList<>());

        when(warehouseResponseToWarehouseMapper.mapToEntity(any(WarehouseResponse.class))).thenReturn(warehouse);
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(warehouse);

        Warehouse result = warehouseService.createWarehouse(warehouseRequest);

        assertNotNull(result);
        assertEquals(warehouseRequest.getWarehouseSpaceType(), result.getWarehouseSpaceType());
        assertEquals(warehouseRequest.getWarehouseLocation(), result.getWarehouseLocation());
        verify(warehouseRepository, times(1)).save(any(Warehouse.class));
    }

    @Test
    void shouldThrowSecurityExceptionWhenUserIsNotAuthenticated() {
        when(authentication.isAuthenticated()).thenReturn(false);
        WarehouseRequest warehouseRequest = new WarehouseRequest();
        assertThrows(SecurityException.class, () -> warehouseService.createWarehouse(warehouseRequest));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenUserNotFound() {
        when(userRepository.findUserByEmail(anyString())).thenReturn(Optional.empty());
        WarehouseRequest warehouseRequest = new WarehouseRequest(WarehouseSpaceType.SHELF, WarehouseLocation.Warsaw);
        assertThrows(IllegalArgumentException.class, () -> warehouseService.createWarehouse(warehouseRequest));
    }

    @Test
    void shouldAssignLocationCode() {
        WarehouseSpaceTypeRequest spaceTypeRequest = new WarehouseSpaceTypeRequest(WarehouseSpaceType.SHELF);
        ItemEntity item = new ItemEntity(1L, "Test_Item", "itemCode", 450, null, user, null, new ArrayList<>());
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepository.save(any(ItemEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ItemEntity result = warehouseService.assignLocationCode(spaceTypeRequest, 1L);

        assertNotNull(result.getLocationCode());
        assertTrue(result.getLocationCode().startsWith("S"));
    }

    @Test
    void shouldAssignItemsToWarehouseLocation() {
        WarehouseLocationRequest locationRequest = new WarehouseLocationRequest(WarehouseLocation.Warsaw);
        ItemEntity item = new ItemEntity(1L, "Test_Item", "itemCode", 450, "S01A", user, null, new ArrayList<>());
        Warehouse warehouse = new Warehouse(1L, WarehouseSpaceType.SHELF, WarehouseLocation.Warsaw, user, new ArrayList<>());

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(warehouseRepository.findFirstByWarehouseLocation(locationRequest.getWarehouseLocation())).thenReturn(Optional.of(warehouse));
        when(itemRepository.save(any(ItemEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ItemEntity result = warehouseService.assignItemsToWarehouseLocation(locationRequest, 1L);

        assertNotNull(result.getLocationCode());
        assertTrue(result.getLocationCode().startsWith("W"));
    }

    @Test
    void shouldThrowExceptionWhenItemNotFound() {
        lenient().when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());
        WarehouseLocationRequest locationRequest = new WarehouseLocationRequest(WarehouseLocation.Warsaw);
        assertThrows(IllegalArgumentException.class, () -> warehouseService.assignItemsToWarehouseLocation(locationRequest, 1L));
    }

    @Test
    void shouldUpdateItemLocation() {
        WarehouseLocationRequest locationRequest = new WarehouseLocationRequest(WarehouseLocation.Krakow);
        ItemEntity item = new ItemEntity(1L,
                "Test_Item", "itemCode",
                450,
                "WS01A",
                user,
                null,
                new ArrayList<>());

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepository.save(any(ItemEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ItemEntity result = warehouseService.updatedItemsToWarehouseLocation(locationRequest, 1L);

        assertNotNull(result.getLocationCode());
        assertTrue(result.getLocationCode().startsWith("K"));
    }

    @Test
    void shouldThrowExceptionWhenWarehouseNotFound() {
        lenient().when(warehouseRepository.findFirstByWarehouseLocation(any())).thenReturn(Optional.empty());
        WarehouseLocationRequest locationRequest = new WarehouseLocationRequest(WarehouseLocation.Warsaw);
        assertThrows(IllegalArgumentException.class, () -> warehouseService.assignItemsToWarehouseLocation(locationRequest, 1L));
    }
}