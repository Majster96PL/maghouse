package com.example.maghouse.warehouse;

import com.example.maghouse.auth.registration.role.Role;
import com.example.maghouse.auth.registration.user.User;
import com.example.maghouse.auth.registration.user.UserRepository;
import com.example.maghouse.item.Item;
import com.example.maghouse.item.ItemRepository;
import com.example.maghouse.security.PasswordEncoder;
import com.example.maghouse.warehouse.location.WarehouseLocation;
import com.example.maghouse.warehouse.location.WarehouseLocationRequest;
import com.example.maghouse.warehouse.spacetype.WarehouseSpaceType;
import com.example.maghouse.warehouse.spacetype.WarehouseSpaceTypeRequest;
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

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestPropertySource("classpath:application-test.yml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@AutoConfigureMockMvc
@Transactional
public class WarehouseServiceIntegrationTest {

    @Autowired
    private WarehouseService warehouseService;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private ItemRepository itemRepository;

    private User user;
    private Item item;
    private Warehouse warehouse;

    @BeforeEach
    void setUp(){
        setUpTestUser();
        authenticateTestUser();
        createAndSaveTestItem();
        createAndSaveTestWarehouse();
    }

    @AfterEach
    void tearDown(){
        SecurityContextHolder.clearContext();
    }

    private void authenticateTestUser() {
        user = userRepository.findUserByEmail(user.getEmail())
                   .orElseThrow(() -> new RuntimeException("User not found!"));
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                user.getEmail(), "password123"
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
                .password(passwordEncoder.bCryptPasswordEncoder().encode("password123"))
                .role(Role.MANAGER)
                .build();
        userRepository.save(user);
    }

    private Item createAndSaveTestItem(){
        item = Item.builder()
                .id(1L)
                .name("Test Name")
                .itemCode("TEST123")
                .quantity(10)
                .user(user)
                .warehouse(null)
                .build();

        return itemRepository.save(item);
    }

    private Warehouse createAndSaveTestWarehouse(){
         warehouse = Warehouse.builder()
                .id(1L)
                .warehouseSpaceType(WarehouseSpaceType.SHELF)
                .warehouseLocation(WarehouseLocation.Warsaw)
                .user(user)
                .items(new ArrayList<>())
                .build();

        return warehouseRepository.save(warehouse);
    }

    @Test
    void shouldCreateWarehousePersistInDatabase(){
        WarehouseRequest request = new WarehouseRequest();
        request.setWarehouseSpaceType(WarehouseSpaceType.SHELF);
        request.setWarehouseLocation(WarehouseLocation.Rzeszow);

        Warehouse result = warehouseService.createWarehouse(request);

        assertNotNull(result.getId());
        assertEquals(WarehouseSpaceType.SHELF, result.getWarehouseSpaceType());
        assertEquals(WarehouseLocation.Rzeszow, result.getWarehouseLocation());
        assertEquals(user, result.getUser());
    }

    @Test
    void shouldAssignCorrectLocation(){
        Warehouse warehouse = createAndSaveTestWarehouse();

        Item item = createAndSaveTestItem();

        WarehouseSpaceTypeRequest request = new WarehouseSpaceTypeRequest(WarehouseSpaceType.DRAVER);

        Item result = warehouseService.assignLocationCode(request, item.getId());

        assertNotNull(result);
        assertNotNull(result.getLocationCode());
        assertTrue(result.getLocationCode().matches("^D\\d{2}[A-C]$"));
        assertEquals(user, result.getUser());
    }

    @Test
    void shouldThrowWhenUserNotAuthenticated(){
        WarehouseLocationRequest warehouseLocationRequest = new WarehouseLocationRequest(WarehouseLocation.Krakow);

        SecurityContextHolder.getContext().setAuthentication(null);

        assertThrows(SecurityException.class,
                () -> warehouseService.assignItemsToWarehouseLocation(warehouseLocationRequest, 1L)
        );
    }

    @Test
    void shouldUpdateLocationPrefix(){
        Warehouse warehouse = createAndSaveTestWarehouse();

        Item item = createAndSaveTestItem();

        WarehouseLocationRequest warehouseLocationRequest = new WarehouseLocationRequest(WarehouseLocation.Krakow);

//        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        Item result = warehouseService.updatedItemsToWarehouseLocation(warehouseLocationRequest, item.getId());

        assertNotNull(result);
        assertTrue(result.getLocationCode().matches("^KS\\d{2}[A-C]$"));
        assertEquals(user, result.getUser());
        assertEquals(warehouse, result.getWarehouse());
    }
}
