package com.example.maghouse.auth.controller;

import com.example.maghouse.auth.registration.role.Role;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private AuthenticationManager authenticationManager;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User user;
    private ItemEntity item;

    @BeforeEach
    void setUp() {
        this.setUpUser();
        this.authenticateUser();
        this.createAndSaveTestItem();

    }

    @Test
    void shouldCreateItem() throws Exception {
        ItemRequest itemRequest = new ItemRequest("Item1", 10);

        mockMvc.perform(post("/items/")
                        .content(objectMapper.writeValueAsString(itemRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Item1"))
                .andExpect(jsonPath("$.quantity").value(10));
    }

    @Test
    void shouldUpdateItemQuantity() throws Exception {
        ItemEntity item = createAndSaveTestItem();

        ItemRequest updateRequest = new ItemRequest("Item1", 15);

        mockMvc.perform(put("/items/" + item.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(15));
    }

    @Test
    void shouldDeleteItem() throws Exception {
        ItemEntity item = createAndSaveTestItem();

        mockMvc.perform(delete("/items/" + item.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    private User setUpUser() {
        user = User.builder()
                .id(1L)
                .firstname("John")
                .lastname("Doe")
                .email("johndoe@example.com")
                .password(passwordEncoder.bCryptPasswordEncoder().encode("password"))
                .role(Role.USER)
                .build();
        return userRepository.save(user);
    }

    private void authenticateUser() {
        user = userRepository.findUserByEmail(user.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found!"));
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                user.getEmail(), "password"
        );
        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private ItemEntity createAndSaveTestItem() {
        item = ItemEntity.builder()
                .id(1L)
                .name("Test Name")
                .itemCode("TestCode")
                .quantity(10)
                .user(user)
                .build();
        return itemRepository.save(item);
    }
}
