package com.example.maghouse.item;

import com.example.maghouse.auth.registration.role.Role;
import com.example.maghouse.auth.registration.user.User;
import com.example.maghouse.auth.registration.user.UserRepository;
import com.example.maghouse.security.PasswordEncoder;
import jakarta.transaction.Transactional;
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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestPropertySource("classpath:application-test.yml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@AutoConfigureMockMvc
@Transactional
public class ItemServiceIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ItemService itemService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User user;

    @BeforeEach
    void setUp(){
        setUpTestUser();
        authenticateTestUser();
    }

    private void authenticateTestUser() {
        user = userRepository.findUserByEmail(user.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(user.getEmail(), "password123");
        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private void setUpTestUser() {
        user = User.builder()
                .id(1L)
                .firstname("John")
                .lastname("Kovalsky")
                .email("john.kovalsky@maghouse.com")
                .password(passwordEncoder.bCryptPasswordEncoder().encode("password123"))
                .role(Role.USER)
                .build();
        userRepository.save(user);
    }

    private ItemEntity createAndSaveTestItem(){
        ItemEntity item = com.example.maghouse.item.ItemEntity.builder()
                .id(1L)
                .name("Test Name")
                .itemCode("TestCode")
                .quantity(10)
                .user(user)
                .build();
        return itemRepository.save(item);
    }

    @Test
    public void shouldCreateItemByUser(){
        ItemRequest itemRequest = new ItemRequest("Item", 10);
        ItemEntity testItem = itemService.createItem(itemRequest);

        assertNotNull(testItem.getId());
        assertEquals("Item", testItem.getName());
        assertEquals(10, testItem.getQuantity());
        assertEquals(user.getId(), testItem.getUser().getId());
    }

    @Test
    public void shouldUpdateItemQuantity(){
        ItemEntity item = createAndSaveTestItem();
        ItemRequest updateRequest = new ItemRequest("Test Name", 100);

        ItemEntity updatedItem = itemService.updateItemQuantity(item.getId(), updateRequest);

        assertEquals(100, updatedItem.getQuantity());
    }

    @Test
    public void shouldDeleteItem(){
        ItemEntity item = createAndSaveTestItem();

        itemService.deleteItem(item.getId());

        assertFalse(itemRepository.findById(item.getId()).isPresent());
    }
}
