package com.example.maghouse.warehouse;

import com.example.maghouse.auth.registration.role.Role;
import com.example.maghouse.auth.registration.user.User;
import com.example.maghouse.auth.registration.user.UserRepository;
import com.example.maghouse.item.ItemEntity;
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

import static org.junit.jupiter.api.Assertions.*;


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
    private ItemEntity item;
    private WarehouseEntity warehouseEntity;

    @BeforeEach
    void setUp(){
        setUpTestUser();
        authenticateTestUser();
        createAndSaveTestItem();
        createAndSaveTestWarehouse();
        warehouseRepository.deleteAll();
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

    private ItemEntity createAndSaveTestItem(){
        item = ItemEntity.builder()
                .id(1L)
                .name("Test Name")
                .itemCode("TEST123")
                .locationCode(null)
                .quantity(10)
                .user(user)
                .warehouseEntity(null)
                .build();

        return itemRepository.save(item);
    }

    private WarehouseEntity createAndSaveTestWarehouse(){
         warehouseEntity = WarehouseEntity.builder()
                .id(1L)
                .warehouseLocation(WarehouseLocation.Warsaw)
                .user(user)
                .items(new ArrayList<>())
                .build();

        return warehouseRepository.save(warehouseEntity);
    }

    @Test
    void shouldCreateWarehousePersistInDatabase(){
        WarehouseRequest request = new WarehouseRequest();
        request.setWarehouseLocation(WarehouseLocation.Rzeszow);

        WarehouseEntity result = warehouseService.createWarehouse(request);

        assertNotNull(result.getId());
        assertEquals(WarehouseLocation.Rzeszow, result.getWarehouseLocation());
        assertEquals(user, result.getUser());
    }

    @Test
    void shouldAssignCorrectLocation(){
        WarehouseEntity warehouseEntity = createAndSaveTestWarehouse();

        ItemEntity item = createAndSaveTestItem();

        WarehouseSpaceTypeRequest request = new WarehouseSpaceTypeRequest(WarehouseSpaceType.DRAVER);

        ItemEntity result = warehouseService.assignWarehouseSpaceType(request, item.getId());

        assertNotNull(result);
        assertNotNull(result.getLocationCode());
        assertTrue(result.getLocationCode().matches("^D\\d{2}[A-C]$"));
        assertEquals(user, result.getUser());
    }

    @Test
    void shouldUpdateLocationPrefix(){
        ItemEntity item = createAndSaveTestItem();
        item.setLocationCode("RS01C");
        itemRepository.save(item);

        WarehouseLocationRequest warehouseLocationRequest = new WarehouseLocationRequest(WarehouseLocation.Krakow);

        ItemEntity result = warehouseService.updatedItemsToWarehouseLocation(warehouseLocationRequest, item.getId());

        assertNotNull(result);
        assertTrue(result.getLocationCode().matches("^KS\\d{2}[A-C]$"));
        assertEquals(user, result.getUser());
    }

    @Test
    void shouldAssignItemsToWarehouseLocation(){
        WarehouseEntity warehouseEntity = createAndSaveTestWarehouse();

        ItemEntity item = createAndSaveTestItem();
        item.setLocationCode("S02B");
        itemRepository.save(item);

        WarehouseLocationRequest warehouseLocationRequest = new WarehouseLocationRequest(WarehouseLocation.Warsaw);

        ItemEntity result = warehouseService.assignItemsToWarehouseLocation(warehouseLocationRequest, item.getId());

        assertNotNull(result);
        assertEquals("WS02B", result.getLocationCode());
        assertEquals(warehouseEntity.getId(), result.getWarehouseEntity().getId());
    }

    @Test
    void shouldThrowAllWhenUserNotAuthenticated(){
        WarehouseLocationRequest warehouseLocationRequest = new WarehouseLocationRequest(WarehouseLocation.Krakow);
        WarehouseSpaceTypeRequest warehouseSpaceTypeRequest = new WarehouseSpaceTypeRequest(WarehouseSpaceType.DRAVER);
        WarehouseRequest warehouseRequest = new WarehouseRequest();

        SecurityContextHolder.getContext().setAuthentication(null);

        assertThrows(SecurityException.class,
                () -> warehouseService.createWarehouse(warehouseRequest)
        );

        assertThrows(SecurityException.class,
                () -> warehouseService.assignItemsToWarehouseLocation(warehouseLocationRequest, 1L)
        );

        assertThrows(SecurityException.class,
                () -> warehouseService.updatedItemsToWarehouseLocation(warehouseLocationRequest, 1L)
        );

        assertThrows(SecurityException.class,
                () -> warehouseService.assignWarehouseSpaceType(warehouseSpaceTypeRequest, 1L)
        );
    }
}
