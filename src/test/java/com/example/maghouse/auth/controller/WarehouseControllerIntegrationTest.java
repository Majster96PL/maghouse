package com.example.maghouse.auth.controller;

import com.example.maghouse.auth.registration.role.Role;
import com.example.maghouse.auth.registration.user.User;
import com.example.maghouse.auth.registration.user.UserRepository;
import com.example.maghouse.item.Item;
import com.example.maghouse.item.ItemRepository;
import com.example.maghouse.security.PasswordEncoder;
import com.example.maghouse.warehouse.Warehouse;
import com.example.maghouse.warehouse.WarehouseRepository;
import com.example.maghouse.warehouse.WarehouseRequest;
import com.example.maghouse.warehouse.WarehouseService;
import com.example.maghouse.warehouse.location.WarehouseLocation;
import com.example.maghouse.warehouse.location.WarehouseLocationRequest;
import com.example.maghouse.warehouse.spacetype.WarehouseSpaceType;
import com.example.maghouse.warehouse.spacetype.WarehouseSpaceTypeRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
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

import java.util.ArrayList;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@TestPropertySource("classpath:application-test.yml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
public class WarehouseControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private WarehouseService warehouseService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    private User user;
    private Item item;
    private Warehouse warehouse;


    @BeforeEach
    void setUp() {
        setUpTestUser();
        createAndSaveTestWarehouse();
        createAndSaveTestItem();
        authenticateTestUser();
        warehouseRepository.deleteAll();
    }

    @AfterEach
    void tearDown(){
        SecurityContextHolder.clearContext();
    }


    private void authenticateTestUser() {
        user = userRepository.findUserByEmail(user.getEmail())
                .orElseThrow( () -> new RuntimeException("User not found!!"));

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                user.getEmail(), "testPassword"
        );

        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private void setUpTestUser() {
        user = User.builder()
                .id(1L)
                .firstname("John")
                .lastname("Kovalsky")
                .email("john.kovalsky@maghouse.com")
                .password(passwordEncoder.bCryptPasswordEncoder().encode("testPassword"))
                .role(Role.ADMIN)
                .build();

        userRepository.save(user);
    }

    private Warehouse createAndSaveTestWarehouse() {
        warehouse = Warehouse.builder()
                .warehouseSpaceType(WarehouseSpaceType.CONTAINER)
                .warehouseLocation(WarehouseLocation.Warsaw)
                .user(user)
                .items(new ArrayList<>())
                .build();

        return warehouseRepository.save(warehouse);
    }

    private Item createAndSaveTestItem(){
         item = Item.builder()
                .name("TestName")
                .itemCode("itemCode")
                .locationCode(null)
                .quantity(100)
                .user(user)
                .warehouse(warehouse)
                .deliveries(null)
                .build();

        return itemRepository.save(item);
    }

    @Test
    void shouldCreateWarehousePersistDatabase() throws Exception {
        WarehouseRequest request = new WarehouseRequest(
                WarehouseSpaceType.CONTAINER,
                WarehouseLocation.Warsaw
        );

        mockMvc.perform(post("/auth/warehouse/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.warehouseSpaceType").value("CONTAINER"));

    }

    @Test
    void shouldAssignWarehouseSpaceTypeToItem() throws Exception {
        item.setLocationCode("C10C");
        item.setWarehouse(warehouse);

        itemRepository.save(item);

        WarehouseSpaceTypeRequest warehouseSpaceTypeRequest = new WarehouseSpaceTypeRequest(
                WarehouseSpaceType.CONTAINER
        );

        mockMvc.perform(post("/auth/warehouse/assign-space-type/" + item.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(warehouseSpaceTypeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.locationCode").exists());

    }

    @Test
    void shouldAssignWarehouseLocationToItem() throws Exception {
        item.setLocationCode("C10C");
        item.setWarehouse(warehouse);

        itemRepository.save(item);

        WarehouseLocationRequest warehouseLocationRequest = new WarehouseLocationRequest(
                WarehouseLocation.Rzeszow
        );

        mockMvc.perform(post("/auth/warehouse/assign-location/" + item.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(warehouseLocationRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.locationCode").value("RC10C"));
    }
}
