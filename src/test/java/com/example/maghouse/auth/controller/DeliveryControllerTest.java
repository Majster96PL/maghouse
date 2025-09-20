package com.example.maghouse.auth.controller;

import com.example.maghouse.auth.registration.role.Role;
import com.example.maghouse.auth.registration.user.User;
import com.example.maghouse.auth.registration.user.UserRepository;
import com.example.maghouse.delivery.DeliveryEntity;
import com.example.maghouse.delivery.DeliveryRequest;
import com.example.maghouse.delivery.DeliveryResponse;
import com.example.maghouse.delivery.DeliveryService;
import com.example.maghouse.delivery.status.DeliveryStatus;
import com.example.maghouse.delivery.status.DeliveryStatusRequest;
import com.example.maghouse.mapper.DeliveryResponseToDeliveryMapper;
import com.example.maghouse.warehouse.location.WarehouseLocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DeliveryControllerTest {

    @Mock
    private DeliveryResponseToDeliveryMapper deliveryResponseToDeliveryMapper;
    @Mock
    private DeliveryService deliveryService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserDetails userDetails;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private DeliveryController deliveryController;

    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        SecurityContextHolder.setContext(securityContext);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.isAuthenticated()).thenReturn(true);
        lenient().when(authentication.getPrincipal()).thenReturn(userDetails);

        user = User.builder()
                .id(1L)
                .firstname("John")
                .lastname("Kovalsky")
                .email("john.kovalsky@maghouse.com")
                .password("testPassword")
                .role(Role.MANAGER)
                .build();

        lenient().when(userDetails.getUsername()).thenReturn(user.getEmail());
        lenient().when(userRepository.findUserByEmail(user.getEmail())).thenReturn(Optional.of(user));
    }

    @Test
    void shouldCreateDeliveryWhenUserIsAuthenticated(){
        DeliveryRequest request = new DeliveryRequest(
                "inpost",
                "ItemName",
                "ItemCode",
                100,
                WarehouseLocation.Rzeszow
        );

        DeliveryEntity exceptedDelivery = DeliveryEntity.builder()
                .supplier("inpost")
                .date(Date.valueOf(LocalDate.now()))
                .numberDelivery(null)
                .itemName("Item Name")
                .itemCode("Item Code")
                .deliveryStatus(DeliveryStatus.CREATED)
                .warehouseLocation(WarehouseLocation.Rzeszow)
                .user(user)
                .item(null)
                .build();
        when(deliveryService.createDelivery(request)).thenReturn(exceptedDelivery);
        when(deliveryResponseToDeliveryMapper.mapToResponse(any(DeliveryEntity.class))).thenReturn(new DeliveryResponse());

        ResponseEntity<DeliveryResponse> result = deliveryController.create(request);

        assertNotNull(result);
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(new DeliveryResponse(), result.getBody());
        verify(deliveryService).createDelivery(request);
    }

    @Test
    void shouldThrowExceptionWhenCreatingDeliveryWithNullRequest(){
        assertThrows(IllegalArgumentException.class, () -> deliveryController.create(null));
        verify(deliveryService, never()).createDelivery(any());
    }

    @Test
    void shouldUpdateDeliveryStatusSuccessfully(){
        Long id = 1L;
        DeliveryStatusRequest deliveryStatusRequest = new DeliveryStatusRequest(DeliveryStatus.IN_PROGRESS);

        DeliveryEntity updatedDelivery = DeliveryEntity.builder()
                .supplier("inpost")
                .date(Date.valueOf(LocalDate.now()))
                .numberDelivery(null)
                .itemName("Item Name")
                .itemCode("Item Code")
                .deliveryStatus(DeliveryStatus.IN_PROGRESS)
                .warehouseLocation(WarehouseLocation.Rzeszow)
                .user(user)
                .item(null)
                .build();

        when(deliveryService.updateDeliveryStatus(deliveryStatusRequest, id)).thenReturn(updatedDelivery);

        ResponseEntity<DeliveryResponse> result = deliveryController.updateDeliveryStatus(deliveryStatusRequest, id);

        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(deliveryService).updateDeliveryStatus(deliveryStatusRequest, id);
    }

    @Test
    void shouldThrowExceptionWhenServiceFailsToUpdateStatus(){
        Long id = 2L;
        DeliveryStatusRequest deliveryStatusRequest = new DeliveryStatusRequest(DeliveryStatus.CANCELLED);

        when(deliveryService.updateDeliveryStatus(deliveryStatusRequest, id))
                .thenThrow(new RuntimeException("Delivery not found!"));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                deliveryController.updateDeliveryStatus(deliveryStatusRequest, id)
        );

        assertEquals("Delivery not found!", exception.getMessage());
        verify(deliveryService).updateDeliveryStatus(deliveryStatusRequest, id);
    }

    @Test
    void shouldThrowExceptionWhenUpdateDeliveryStatusRequestIsNull() throws Exception{
        Long id = 1L;

        assertThrows(IllegalArgumentException.class, () ->
                deliveryController.updateDeliveryStatus(null, id));

        verify(deliveryService, never()).updateDeliveryStatus(any(), any());
    }

    @Test
    void shouldThrowExceptionWhenUpdateDeliveryStatusWithNullId(){
        DeliveryStatusRequest deliveryStatusRequest = new DeliveryStatusRequest(DeliveryStatus.DELIVERED);

        assertThrows(IllegalArgumentException.class, () ->
                deliveryController.updateDeliveryStatus(deliveryStatusRequest, null));

        verify(deliveryService, never()).updateDeliveryStatus(any(), any());
    }

    void shouldThrowAllExceptionsWhenUserIsNotAuthenticated(){
        when(securityContext.getAuthentication()).thenReturn(null);

        DeliveryRequest deliveryRequest = new DeliveryRequest(
                "inpost",
                "ItemName",
                "ItemCode",
                100,
                WarehouseLocation.Rzeszow
        );

        assertDoesNotThrow(() -> deliveryController.create(deliveryRequest));

        Long id = 1L;
        DeliveryStatusRequest deliveryStatusRequest = new DeliveryStatusRequest(DeliveryStatus.IN_PROGRESS);

        assertDoesNotThrow(() -> deliveryController.updateDeliveryStatus(deliveryStatusRequest, id));
    }

    @Test
    void shouldThrowAllExceptionsWhenUserNotFound(){
        when(userDetails.getUsername()).thenReturn(user.getEmail());
        when(userRepository.findUserByEmail(user.getEmail())).thenReturn(Optional.empty());

        DeliveryRequest deliveryRequest = new DeliveryRequest(
                "inpost",
                "ItemName",
                "ItemCode",
                100,
                WarehouseLocation.Rzeszow
        );

        assertDoesNotThrow(() -> deliveryController.create(deliveryRequest));

        Long id = 1L;
        DeliveryStatusRequest deliveryStatusRequest = new DeliveryStatusRequest(DeliveryStatus.IN_PROGRESS);

        assertDoesNotThrow(() -> deliveryController.updateDeliveryStatus(deliveryStatusRequest, id));
    }
}
