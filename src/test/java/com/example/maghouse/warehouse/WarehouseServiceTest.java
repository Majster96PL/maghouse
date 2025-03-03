package com.example.maghouse.warehouse;

import com.example.maghouse.auth.registration.role.Role;
import com.example.maghouse.auth.registration.user.User;
import com.example.maghouse.auth.registration.user.UserRepository;
import com.example.maghouse.item.Item;
import com.example.maghouse.item.ItemRepository;
import com.example.maghouse.mapper.WarehouseResponseToWarehouseMapper;
import com.example.maghouse.warehouse.location.WarehouseLocation;
import com.example.maghouse.warehouse.spacetype.WarehouseSpaceType;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WarehouseServiceTest {

    @Mock
    private WarehouseResponseToWarehouseMapper warehouseResponseToWarehouseMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private WarehouseService warehouseService;

    private User user;

    void setUp(){
        MockitoAnnotations.openMocks(this);

        user = User.builder()
                .id(1L)
                .firstname("John")
                .lastname("Kovalsky")
                .email("john.kovalsky@maghaouse.com")
                .password("password123")
                .role(Role.USER)
                .build();

        Authentication authentication = mock(Authentication.class);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);

        UserDetails userDetails = mock(UserDetails.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("john.kovalsky@maghouse.com");

        when(userRepository.findUserByEmail("john.kovalsky@maghouse.com")).thenReturn(Optional.of(user));

        Warehouse warehouse = Warehouse.builder()
                .id(1L)
                .warehouseSpaceType(WarehouseSpaceType.SHELF)
                .warehouseLocation(WarehouseLocation.Krakow)
                .user(user)
                .items(new ArrayList<>())
                .build();
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(warehouse);

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
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenReturn(item);
    }
}
