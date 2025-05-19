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
import org.junit.jupiter.api.Test;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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

        Item item = Item.builder()
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
                .numberDelivery(null)
                .itemName(item.getName())
                .itemCode(item.getItemCode())
                .deliveryStatus(DeliveryStatus.CREATED)
                .warehouseLocation(WarehouseLocation.Rzeszow)
                .user(user)
                .item(item)
                .build();

        lenient().when(authentication.isAuthenticated()).thenReturn(true);
        lenient().when(authentication.getPrincipal()).thenReturn(userDetails);
        lenient().when(userDetails.getUsername()).thenReturn(user.getEmail());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        deliveryRepository.deleteAll();
    }

    @Test
    void shouldCreateDeliverySuccessfully() {
        String deliveryNumber = "1/05/2025";
        when(deliveryNumberGenerator.generateDeliveryNumber()).thenReturn("1/05/2025");
        LocalDate date = LocalDate.now();

        DeliveryRequest request = new DeliveryRequest(
                "inpost",
                "ItemName",
                "ItemCode",
                100,
                WarehouseLocation.Rzeszow
        );

        DeliveryResponse deliveryResponse =
                deliveryResponseToDeliveryMapper.mapToDeliveryResponse(
        request, deliveryNumber, date, user );

        when(userRepository.findUserByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(deliveryResponseToDeliveryMapper.mapToDeliveryResponse(
                eq(request),
                eq(deliveryNumber),
                eq(date),
                eq(user))
        ).thenReturn(deliveryResponse);

        when(deliveryResponseToDeliveryMapper.mapToDelivery(deliveryResponse))
                .thenReturn(delivery);

        when(deliveryRepository.save(delivery)).thenReturn(delivery);

        Delivery result = deliveryService.createDelivery(request);

        assertNotNull(result);
        assertEquals(delivery.getNumberDelivery(), result.getNumberDelivery());
        assertEquals(delivery.getSupplier(), result.getSupplier());
        assertEquals(delivery.getItemName(), result.getItemName());
    }

    @Test
    void shouldThrowExceptionWhenNotAuthenticatedUser(){
        SecurityContextHolder.clearContext();

        DeliveryRequest request = new DeliveryRequest(
                "inpost",
                "ItemName",
                "ItemCode",
                100,
                WarehouseLocation.Rzeszow
        );

        assertThrows(SecurityException.class,
                () -> deliveryService.createDelivery(request));

    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundByEmail(){
        DeliveryRequest request = new DeliveryRequest(
                "inpost",
                "ItemName",
                "ItemCode",
                100,
                WarehouseLocation.Rzeszow
        );

        when(userRepository.findUserByEmail(user.getEmail())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> deliveryService.createDelivery(request));
    }

    @Test
    void shouldThrowExceptionWhenDeliveryNumberGeneratorReturnsNull(){
        DeliveryRequest request = new DeliveryRequest(
                "inpost",
                "ItemName",
                "ItemCode",
                100,
                WarehouseLocation.Rzeszow
        );

        when(userRepository.findUserByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(deliveryNumberGenerator.generateDeliveryNumber()).thenReturn(null);

        assertThrows(NullPointerException.class, () -> deliveryService.createDelivery(request));
    }
}
