package com.example.maghouse.auth.controller;

import com.example.maghouse.auth.login.LoginRequest;
import com.example.maghouse.auth.registration.role.Role;
import com.example.maghouse.auth.registration.token.TokenResponse;
import com.example.maghouse.auth.registration.user.User;
import com.example.maghouse.auth.registration.user.UserRepository;
import com.example.maghouse.item.ItemEntity;
import com.example.maghouse.item.ItemRepository;
import com.example.maghouse.item.ItemRequest;
import com.example.maghouse.security.PasswordEncoder;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@TestPropertySource("classpath:application-test.yml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
public class ItemControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User user;
    private String userToken;

    @BeforeEach
    void setUp() throws Exception {

         user = User.builder()
                 .id(1L)
                .firstname("John")
                .lastname("Doe")
                .email("johndoe@example.com")
                .password(passwordEncoder.bCryptPasswordEncoder().encode("password"))
                .role(Role.USER)
                .build();
        userRepository.save(user);

        userToken = getAccessToken();
    }

    private ItemEntity createAndSaveTestItem(){
        ItemEntity item = ItemEntity.builder()
                .id(1L)
                .name("Test Name")
                .itemCode("TestCode")
                .quantity(10)
                .user(user)
                .deliveries(null)
                .warehouseEntity(null)
                .build();
        return itemRepository.save(item);
    }

    @Test
    void shouldCreateItem() throws Exception {
        ItemRequest itemRequest = new ItemRequest("Item1", 10);

        mockMvc.perform(post("/items/")
                        .content(objectMapper.writeValueAsString(itemRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Item1"))
                .andExpect(jsonPath("$.quantity").value(10));
    }

    @Test
    void shouldUpdateItemQuantity() throws Exception {
        ItemEntity item = createAndSaveTestItem();
        itemRepository.save(item);

        ItemRequest updateRequest = new ItemRequest("Item1", 15);

        mockMvc.perform(put("/items/" + item.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest))
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(15));
    }

    @Test
    void shouldDeleteItem() throws Exception {
        ItemEntity item = createAndSaveTestItem();
        itemRepository.save(item);

        mockMvc.perform(delete("/delete/" + item.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNoContent());
    }

    private String getAccessToken() throws Exception {
        LoginRequest loginRequest = new LoginRequest("johndoe@example.com", "password");

        String response = mockMvc.perform(post("/maghouse/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        TokenResponse tokenResponse = objectMapper.readValue(response, TokenResponse.class);
        return tokenResponse.getAccessToken();
    }
}
