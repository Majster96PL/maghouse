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
import java.util.Collections;
import java.util.List;
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
    private WarehouseEntity warehouseEntity;
    private ItemEntity item;
    private WarehouseRequest request;

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

        item = ItemEntity.builder()
                .name("ItemName")
                .itemCode("itemCode")
                .quantity(450)
                .user(user)
                .warehouseEntity(warehouseEntity)
                .build();

        request = new WarehouseRequest( WarehouseLocation.Krakow);

        warehouseEntity = WarehouseEntity.builder()
                .warehouseLocation(request.getWarehouseLocation())
                .user(user)
                .items(new ArrayList<>())
                .build();

        lenient().when(userDetails.getUsername()).thenReturn("john.kovalsky@maghouse.com");
        lenient().when(userRepository.findUserByEmail(user.getEmail())).thenReturn(Optional.of(user));
    }

    @Test
    void shouldCreateWarehouse_WhenUserIsAuthenticated() {
        WarehouseResponse mockedWarehouseResponse = new WarehouseResponse();
        mockedWarehouseResponse.setUserId(user.getId());
        mockedWarehouseResponse.setItemsId(Collections.singletonList(item.getId()));

        when(warehouseResponseToWarehouseMapper.mapToWarehouseResponse(any(WarehouseRequest.class)))
                .thenReturn(mockedWarehouseResponse);
        when(warehouseResponseToWarehouseMapper.mapToEntityFromResponse(any(WarehouseResponse.class)))
                .thenReturn(new WarehouseEntity());
        when(itemRepository.findByItemLocationStartingWith("K"))
                .thenReturn(Collections.singletonList(item));
        when(warehouseRepository.save(any(WarehouseEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(itemRepository.saveAll(anyList()))
                .thenReturn(Collections.singletonList(item));

        WarehouseEntity result = warehouseService.createWarehouse(request);

        assertNotNull(result);
        assertFalse(result.getItems().isEmpty());
        verify(warehouseRepository, times(1)).save(any(WarehouseEntity.class));
        verify(itemRepository, times(1)).saveAll(anyList());
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
        WarehouseRequest warehouseRequest = new WarehouseRequest( WarehouseLocation.Warsaw);
        assertThrows(IllegalArgumentException.class, () -> warehouseService.createWarehouse(warehouseRequest));
    }

    @Test
    void shouldAssignLocationCode() {
        WarehouseSpaceTypeRequest spaceTypeRequest = new WarehouseSpaceTypeRequest(WarehouseSpaceType.SHELF);

        List<String> usedSpaceCode = List.of();

        when(itemRepository.findUnassignedItem(item.getId())).thenReturn(Optional.of(item));
        when(itemRepository.findUsedLocationCodes(anyString())).thenReturn(usedSpaceCode);

        ItemEntity result = warehouseService.assignWarehouseSpaceType(spaceTypeRequest, item.getId());

        assertNotNull(result);
        assertTrue(result.getLocationCode().startsWith("S"));
        assertEquals(user, result.getUser());
    }

    @Test
    void shouldAssignItemsToWarehouseLocation() {
        WarehouseLocationRequest locationRequest = new WarehouseLocationRequest(WarehouseLocation.Warsaw);

        item.setLocationCode("S01A");

        lenient().when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        lenient().when(warehouseRepository.findFirstByWarehouseLocation(locationRequest.getWarehouseLocation()))
                .thenReturn(Optional.of(warehouseEntity));
        lenient().when(warehouseResponseToWarehouseMapper.mapToWarehouseResponse(any(WarehouseRequest.class)))
                .thenReturn(new WarehouseResponse());
        lenient().when(warehouseResponseToWarehouseMapper.mapToEntityFromResponse(any(WarehouseResponse.class)))
                .thenReturn(warehouseEntity);

        ItemEntity result = warehouseService.assignItemsToWarehouseLocation(locationRequest, 1L);

        assertNotNull(result);
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
        item.setLocationCode("RS01A");
        WarehouseLocationRequest locationRequest = new WarehouseLocationRequest(WarehouseLocation.Krakow);

        lenient().when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        lenient().when(warehouseRepository.findFirstByWarehouseLocation(locationRequest.getWarehouseLocation()))
                .thenReturn(Optional.of(warehouseEntity));
        lenient().when(warehouseResponseToWarehouseMapper.mapToWarehouseResponse(any(WarehouseRequest.class)))
                .thenReturn(new WarehouseResponse());
        lenient().when(warehouseResponseToWarehouseMapper.mapToEntityFromResponse(any(WarehouseResponse.class)))
                .thenReturn(warehouseEntity);

        ItemEntity result = warehouseService.updatedItemsToWarehouseLocation(locationRequest, 1L);

        assertNotNull(result);
        assertTrue(result.getLocationCode().startsWith("K"));
    }

    @Test
    void shouldThrowExceptionWhenWarehouseNotFound() {
        lenient().when(warehouseRepository.findFirstByWarehouseLocation(any())).thenReturn(Optional.empty());
        WarehouseLocationRequest locationRequest = new WarehouseLocationRequest(WarehouseLocation.Warsaw);
        assertThrows(IllegalArgumentException.class, () -> warehouseService.assignItemsToWarehouseLocation(locationRequest, 1L));
    }
}