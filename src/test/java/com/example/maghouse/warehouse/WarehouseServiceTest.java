package com.example.maghouse.warehouse;

import com.example.maghouse.auth.registration.role.Role;
import com.example.maghouse.auth.registration.user.User;
import com.example.maghouse.auth.registration.user.UserRepository;
import com.example.maghouse.item.Item;
import com.example.maghouse.item.ItemRepository;
import com.example.maghouse.mapper.WarehouseResponseToWarehouseMapper;
import com.example.maghouse.warehouse.location.WarehouseLocation;
import com.example.maghouse.warehouse.spacetype.WarehouseSpaceType;
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
    void setUp(){
        SecurityContextHolder.setContext(securityContext);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.isAuthenticated()).thenReturn(true);
        lenient().when(authentication.getPrincipal()).thenReturn(userDetails);

        user = User.builder()
                .id(1L)
                .firstname("John")
                .lastname("Kovalsky")
                .email("john.kovalsky@maghaouse.com")
                .password("password123")
                .role(Role.USER)
                .build();

        lenient().when(userDetails.getUsername()).thenReturn("john.kovalsky@maghouse.com");
        lenient().when(userRepository.findUserByEmail(user.getEmail())).thenReturn(Optional.of(user));

        Warehouse warehouse = Warehouse.builder()
                .id(1L)
                .warehouseSpaceType(WarehouseSpaceType.SHELF)
                .warehouseLocation(WarehouseLocation.Krakow)
                .user(user)
                .items(new ArrayList<>())
                .build();
        lenient().when(warehouseRepository.save(any(Warehouse.class))).thenReturn(warehouse);

        Item item = Item.builder()
                .id(1L)
                .name("Item")
                .itemCode("ItemCode01")
                .quantity(100)
                .locationCode("OLD_LOCATION")
                .user(user)
                .warehouse(warehouse)
                .deliveries(new ArrayList<>())
                .build();
        lenient().when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        lenient().when(itemRepository.save(any(Item.class))).thenReturn(item);
    }

    @Test
    void shouldCreateWarehouse_WhenUserIsAuthenticated(){
        WarehouseRequest warehouseRequest = WarehouseRequest.builder()
                .warehouseSpaceType(WarehouseSpaceType.CONTAINER)
                .warehouseLocation(WarehouseLocation.Krakow)
                .build();

        WarehouseResponse warehouseResponse = new WarehouseResponse();
        warehouseResponse.setWarehouseSpaceType(warehouseRequest.getWarehouseSpaceType());
        warehouseResponse.setWarehouseLocation(warehouseRequest.getWarehouseLocation());
        warehouseResponse.setUser(user);

        Warehouse warehouse = Warehouse.builder()
                .id(1L)
                .warehouseSpaceType(warehouseRequest.getWarehouseSpaceType())
                .warehouseLocation(warehouseRequest.getWarehouseLocation())
                .user(user)
                .items(new ArrayList<>())
                .build();
        when(userDetails.getUsername()).thenReturn("john.kovalsky@maghouse.com");
        when(userRepository.findUserByEmail(anyString())).thenReturn(Optional.of(user));
        when(warehouseResponseToWarehouseMapper.mapToEntity(any(WarehouseResponse.class))).thenReturn(warehouse);
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(warehouse);

        Warehouse result = warehouseService.createWarehouse(warehouseRequest);

        assertNotNull(result);
        assertEquals(warehouseRequest.getWarehouseSpaceType(), result.getWarehouseSpaceType());
        assertEquals(warehouseRequest.getWarehouseLocation(), result.getWarehouseLocation());
        assertEquals(user, result.getUser());
        verify(warehouseRepository, times(1)).save(any(Warehouse.class));
        verify(warehouseResponseToWarehouseMapper, times(1)).mapToEntity(any(WarehouseResponse.class));

    }

    @Test
    void shouldThrowSecurityExceptionWhenUserIsNotAuthenticated(){
        when(authentication.isAuthenticated()).thenReturn(false);

        WarehouseRequest warehouseRequest = new WarehouseRequest();

        assertThrows(SecurityException.class, () -> warehouseService.createWarehouse(warehouseRequest));
    }

}
