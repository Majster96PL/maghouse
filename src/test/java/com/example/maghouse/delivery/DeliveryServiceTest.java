package com.example.maghouse.delivery;

import com.example.maghouse.auth.registration.role.Role;
import com.example.maghouse.auth.registration.user.User;
import com.example.maghouse.auth.registration.user.UserRepository;
import com.example.maghouse.delivery.status.DeliveryStatus;
import com.example.maghouse.item.Item;
import com.example.maghouse.item.ItemRepository;
import com.example.maghouse.mapper.DeliveryResponseToDeliveryMapper;
import com.example.maghouse.warehouse.location.WarehouseLocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DeliveryServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private DeliveryNumberGenerator deliveryNumberGenerator;

    @Mock
    private DeliveryResponseToDeliveryMapper deliveryResponseToDeliveryMapper;

    @Mock
    private DeliveryRepository deliveryRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private DeliveryService deliveryService;

    private User user;
    private Item item;
    private Delivery delivery;

    @BeforeEach
    void setUp(){
        user = User.builder()
                .id(1L)
                .firstname("John")
                .lastname("Kovalsky")
                .email("john.kovalsky@maghouse.com")
                .password("testPassword")
                .role(Role.DRIVER)
                .items(new ArrayList<>())
                .build();

        item = Item.builder()
                .name("ItemName")
                .itemCode("itemCode")
                .quantity(100)
                .locationCode(null)
                .user(user)
                .warehouse(null)
                .deliveries(new ArrayList<>())
                .build();

        delivery = Delivery.builder()
                .supplier("inpost")
                .date(Date.valueOf(LocalDate.now()))
                .numberDelivery(deliveryNumberGenerator.generateDeliveryNumber())
                .itemName(item.getName())
                .itemCode(item.getItemCode())
                .deliveryStatus(DeliveryStatus.CREATED)
                .warehouseLocation(WarehouseLocation.Rzeszow)
                .user(user)
                .item(item)
                .build();

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn(user.getEmail());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
