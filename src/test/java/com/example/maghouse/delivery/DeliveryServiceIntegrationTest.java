package com.example.maghouse.delivery;

import com.example.maghouse.auth.registration.role.Role;
import com.example.maghouse.auth.registration.user.User;
import com.example.maghouse.auth.registration.user.UserRepository;
import com.example.maghouse.delivery.status.DeliveryStatus;
import com.example.maghouse.delivery.status.DeliveryStatusRequest;
import com.example.maghouse.item.Item;
import com.example.maghouse.item.ItemRepository;
import com.example.maghouse.security.PasswordEncoder;
import com.example.maghouse.warehouse.location.WarehouseLocation;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestPropertySource("classpath:application-test.yml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@AutoConfigureMockMvc
@Transactional
public class DeliveryServiceIntegrationTest {

    @Autowired
    private DeliveryService deliveryService;

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User user;
    private Item item;
    private Delivery delivery;

    @Autowired
    private AuthenticationManager authenticationManager;

    @BeforeEach
    void setUp(){
        setUpUser();
        authenticateUser();
        createTestItem();
        createDeliveryTest();
    }

    @AfterEach
    void tearDown(){
        SecurityContextHolder.clearContext();
    }

    private void setUpUser(){
        user = User.builder()
                .firstname("John")
                .lastname("Kovalsky")
                .email("john.kovalsky@maghouse.com")
                .password(passwordEncoder.bCryptPasswordEncoder().encode("password123"))
                .role(Role.USER)
                .build();

        userRepository.save(user);
    }

    private void authenticateUser(){
        user = userRepository.findUserByEmail(user.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found!"));
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                user.getEmail(), "password123"
        );
        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private Item createTestItem(){
        item = Item.builder()
                .name("Test name")
                .itemCode("1024-01-235-1967")
                .quantity(100)
                .locationCode("RSO1B")
                .user(user)
                .warehouse(null)
                .deliveries(new ArrayList<>())
                .build();

        return itemRepository.save(item);
    }

    private Delivery createDeliveryTest(){
        delivery = Delivery.builder()
                .supplier("INPOST")
                .date(Date.valueOf(LocalDate.now()))
                .numberDelivery("13/05/2025")
                .itemName(item.getName())
                .itemCode(item.getItemCode())
                .quantity(100)
                .deliveryStatus(DeliveryStatus.CREATED)
                .warehouseLocation(WarehouseLocation.Rzeszow)
                .user(user)
                .item(item)
                .build();

        return deliveryRepository.save(delivery);
    }

    @Test
    void shouldCreateDeliveryWithValidRequest(){
        DeliveryRequest deliveryRequest = new DeliveryRequest(
                "INPOST",
                item.getName(),
                item.getItemCode(),
                100,
                WarehouseLocation.Rzeszow
        );

        Delivery result = deliveryService.createDelivery(deliveryRequest);

        assertNotNull(result.getId());
        assertEquals(DeliveryStatus.CREATED,result.getDeliveryStatus());
        assertEquals(user.getEmail(), result.getUser().getEmail());
    }

    @Test
    void shouldUpdateStatusToInProgressWithoutChangingItemQuantity(){
        DeliveryStatusRequest deliveryStatusRequest = new DeliveryStatusRequest(DeliveryStatus.IN_PROGRESS);
        int initialQuantity = item.getQuantity();

        Delivery updatedDelivery = deliveryService.updateDeliveryStatus(deliveryStatusRequest, delivery.getId());
        assertEquals(DeliveryStatus.IN_PROGRESS, updatedDelivery.getDeliveryStatus());

        Item unchangedItem = itemRepository.findById(item.getId()).orElseThrow();
        assertEquals(initialQuantity, unchangedItem.getQuantity());

        Delivery persistedDelivery = deliveryRepository.findById(delivery.getId()).orElseThrow();
        assertEquals(DeliveryStatus.IN_PROGRESS, persistedDelivery.getDeliveryStatus());
    }

    @Test
    void shouldUpdateStatusToDeliveredAndIncreaseItemQuantity(){
        int initialQuantity = item.getQuantity();
        int deliveryQuantity = delivery.getQuantity();
        DeliveryStatusRequest deliveryStatusRequest = new DeliveryStatusRequest(DeliveryStatus.DELIVERED);

        Delivery updatedDelivery = deliveryService.updateDeliveryStatus(deliveryStatusRequest, delivery.getId());

        Item updatedItem = itemRepository.findById(item.getId()).orElseThrow();
        assertEquals(DeliveryStatus.DELIVERED, updatedDelivery.getDeliveryStatus());
        assertEquals(initialQuantity + deliveryQuantity, updatedItem.getQuantity());
    }

    @Test
    void shouldThrowAllWhenUserNotAuthenticated(){
        SecurityContextHolder.clearContext();
        DeliveryRequest deliveryRequest = new DeliveryRequest(
                "INPOST",
                item.getName(),
                item.getItemCode(),
                100,
                WarehouseLocation.Rzeszow
        );
        DeliveryStatusRequest statusRequest = new DeliveryStatusRequest(DeliveryStatus.IN_PROGRESS);

        assertThrows(SecurityException.class, () ->
                        deliveryService.createDelivery(deliveryRequest),
                "CreateDelivery should throw when not authenticated"
        );

        assertThrows(SecurityException.class, () ->
                        deliveryService.updateDeliveryStatus(statusRequest, delivery.getId()),
                "UpdateDeliveryStatus should throw when not authenticated"
        );
    }

}
