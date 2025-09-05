package com.example.maghouse.auth.controller;

import com.example.maghouse.auth.registration.role.Role;
import com.example.maghouse.auth.registration.user.User;
import com.example.maghouse.auth.registration.user.UserRepository;
import com.example.maghouse.delivery.Delivery;
import com.example.maghouse.delivery.DeliveryRepository;
import com.example.maghouse.delivery.DeliveryRequest;
import com.example.maghouse.delivery.status.DeliveryStatus;
import com.example.maghouse.delivery.status.DeliveryStatusRequest;
import com.example.maghouse.item.Item;
import com.example.maghouse.item.ItemRepository;
import com.example.maghouse.security.PasswordEncoder;
import com.example.maghouse.warehouse.location.WarehouseLocation;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@TestPropertySource("classpath:application-test.yml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@AutoConfigureMockMvc
@Transactional
public class DeliveryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User user;
    private Delivery delivery;

    @BeforeEach
    void setUp(){
        this.setUpUser();
        this.authenticateUser();
        this.createAndSaveTestDelivery();
    }

    @AfterEach
    void tearDown(){
        SecurityContextHolder.clearContext();
    }

    private void setUpUser() {
        user = User.builder()
                .id(1L)
                .firstname("John")
                .lastname("Kovalsky")
                .email("john.kovalsky@maghouse.com")
                .password(passwordEncoder.bCryptPasswordEncoder().encode("testPassword"))
                .role(Role.USER)
                .build();

         userRepository.save(user);
    }

    private void authenticateUser() {
        user = userRepository.findUserByEmail(user.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found!"));
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                user.getEmail(), "testPassword"
        );

        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private Delivery createAndSaveTestDelivery() {
        delivery = Delivery.builder()
                .supplier("inpost")
                .date(Date.valueOf(LocalDate.now()))
                .numberDelivery(null)
                .itemName("Item Name")
                .itemCode("Item Code")
                .quantity(100)
                .deliveryStatus(DeliveryStatus.CREATED)
                .warehouseLocation(WarehouseLocation.Rzeszow)
                .user(user)
                .item(null)
                .build();

        return deliveryRepository.save(delivery);
    }

    @Test
    void shouldCreateDeliverySuccessfully() throws Exception {
        DeliveryRequest deliveryRequest = new DeliveryRequest(
                "inpost",
                "ItemName",
                "ItemCode",
                100,
                WarehouseLocation.Rzeszow
        );

        mockMvc.perform(post("/maghouse/deliveries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deliveryRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.supplier").value("inpost"))
                .andExpect(jsonPath("$.itemName").value("ItemName"))
                .andExpect(jsonPath("$.itemCode").value("ItemCode"))
                .andExpect(jsonPath("$.quantity").value(100))
                .andExpect( jsonPath("$.warehouseLocation").value("Rzeszow"));
    }

    @Test
    void shouldUpdateDeliveryStatusToInProgress() throws Exception {
        DeliveryStatusRequest deliveryStatusRequest = new DeliveryStatusRequest(DeliveryStatus.IN_PROGRESS);

        mockMvc.perform(put("/maghouse/deliveries/" + delivery.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(deliveryStatusRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deliveryStatus").value("IN_PROGRESS"));
    }

    @Test
    void shouldUpdateDeliveryStatusToDeliveredAndUpdatedItemQuantity() throws Exception{
        Item item = Item.builder()
                .name(delivery.getItemName())
                .itemCode(delivery.getItemCode())
                .locationCode(null)
                .quantity(100)
                .user(user)
                .warehouse(null)
                .deliveries(new ArrayList<>())
                .build();

        itemRepository.save(item);

        delivery.setItem(item);
        delivery.setDeliveryStatus(DeliveryStatus.IN_PROGRESS);
        deliveryRepository.save(delivery);

        DeliveryStatusRequest deliveryStatusRequest = new DeliveryStatusRequest(DeliveryStatus.DELIVERED);

        mockMvc.perform(put("/maghouse/deliveries" + delivery.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(deliveryStatusRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deliveryStatus").value("DELIVERED"));

        Item updatedItem = itemRepository.findByItemCode(item.getItemCode())
                        .orElseThrow(() -> new RuntimeException("Item noc found!"));

        assertEquals(200, updatedItem.getQuantity());
    }

}
