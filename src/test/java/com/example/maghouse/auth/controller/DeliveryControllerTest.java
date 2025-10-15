package com.example.maghouse.auth.controller;

import com.example.maghouse.auth.registration.role.Role;
import com.example.maghouse.auth.registration.user.User;
import com.example.maghouse.delivery.DeliveryEntity;
import com.example.maghouse.delivery.DeliveryRequest;
import com.example.maghouse.delivery.DeliveryResponse;
import com.example.maghouse.delivery.DeliveryService;
import com.example.maghouse.delivery.status.DeliveryStatus;
import com.example.maghouse.delivery.status.DeliveryStatusRequest;
import com.example.maghouse.mapper.DeliveryResponseToDeliveryMapper;
import com.example.maghouse.security.AuthenticationHelper;
import com.example.maghouse.warehouse.location.WarehouseLocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.sql.Date;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeliveryControllerTest {

    @Mock
    private DeliveryResponseToDeliveryMapper deliveryResponseToDeliveryMapper;

    @Mock
    private DeliveryService deliveryService;

    @Mock
    private AuthenticationHelper authenticationHelper;

    @InjectMocks
    private DeliveryController deliveryController;

    private User user;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .firstname("John")
                .lastname("Kovalsky")
                .email("john.kovalsky@maghouse.com")
                .password("testPassword")
                .role(Role.MANAGER)
                .build();

        authentication = mock(Authentication.class);

        lenient().when(authenticationHelper.getAuthenticatedUser(authentication)).thenReturn(user);
    }

    @Test
    void shouldCreateDeliveryWhenUserIsAuthenticated(){
        DeliveryRequest request = new DeliveryRequest(
                "inpost",
                "ItemName",
                "ItemCode",
                100
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

        when(deliveryService.createDelivery(request, user)).thenReturn(exceptedDelivery);
        when(deliveryResponseToDeliveryMapper.mapToResponse(any(DeliveryEntity.class))).thenReturn(new DeliveryResponse());

        ResponseEntity<DeliveryResponse> result = deliveryController.create(request, authentication);

        assertNotNull(result);
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(new DeliveryResponse(), result.getBody());
        verify(authenticationHelper).getAuthenticatedUser(authentication);
        verify(deliveryService).createDelivery(request, user);
    }

    @Test
    void shouldThrowExceptionWhenCreatingDeliveryWithNullRequest(){
        assertThrows(IllegalArgumentException.class, () -> deliveryController.create(null, authentication));
        verify(authenticationHelper).getAuthenticatedUser(authentication);
        verify(deliveryService, never()).createDelivery(any(), any());
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
        when(deliveryResponseToDeliveryMapper.mapToResponse(updatedDelivery)).thenReturn(new DeliveryResponse());

        ResponseEntity<DeliveryResponse> result = deliveryController.updateDeliveryStatus(deliveryStatusRequest, id, authentication);

        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(authenticationHelper).getAuthenticatedUser(authentication);
        verify(deliveryService).updateDeliveryStatus(deliveryStatusRequest, id);
    }

    @Test
    void shouldThrowExceptionWhenServiceFailsToUpdateStatus(){
        Long id = 2L;
        DeliveryStatusRequest deliveryStatusRequest = new DeliveryStatusRequest(DeliveryStatus.CANCELLED);

        when(deliveryService.updateDeliveryStatus(deliveryStatusRequest, id))
                .thenThrow(new RuntimeException("Delivery not found!"));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                deliveryController.updateDeliveryStatus(deliveryStatusRequest, id, authentication)
        );

        assertEquals("Delivery not found!", exception.getMessage());
        verify(authenticationHelper).getAuthenticatedUser(authentication);
        verify(deliveryService).updateDeliveryStatus(deliveryStatusRequest, id);
    }

    @Test
    void shouldThrowExceptionWhenUpdateDeliveryStatusRequestIsNull() {
        Long id = 1L;

        assertThrows(IllegalArgumentException.class, () ->
                deliveryController.updateDeliveryStatus(null, id, authentication));

        verify(authenticationHelper).getAuthenticatedUser(authentication);
        verify(deliveryService, never()).updateDeliveryStatus(any(), any());
    }

    @Test
    void shouldThrowExceptionWhenUpdateDeliveryStatusWithNullId(){
        DeliveryStatusRequest deliveryStatusRequest = new DeliveryStatusRequest(DeliveryStatus.DELIVERED);

        assertThrows(IllegalArgumentException.class, () ->
                deliveryController.updateDeliveryStatus(deliveryStatusRequest, null, authentication));

        verify(authenticationHelper).getAuthenticatedUser(authentication);
        verify(deliveryService, never()).updateDeliveryStatus(any(), any());
    }

    @Test
    void shouldThrowAllExceptionsWhenUserIsNotAuthenticated(){

        DeliveryRequest deliveryRequest = new DeliveryRequest(
                "inpost",
                "ItemName",
                "ItemCode",
                100
        );

        assertDoesNotThrow(() -> deliveryController.create(deliveryRequest, authentication));

        Long id = 1L;
        DeliveryStatusRequest deliveryStatusRequest = new DeliveryStatusRequest(DeliveryStatus.IN_PROGRESS);

        assertDoesNotThrow(() -> deliveryController.updateDeliveryStatus(deliveryStatusRequest, id, authentication));
    }

    @Test
    void shouldThrowExceptionWhenAuthenticationHelperThrowsSecurityException() {
        DeliveryRequest deliveryRequest = new DeliveryRequest(
                "inpost",
                "ItemName",
                "ItemCode",
                100
        );

        when(authenticationHelper.getAuthenticatedUser(authentication))
                .thenThrow(new SecurityException("Authentication failed"));

        assertThrows(SecurityException.class, () ->
                deliveryController.create(deliveryRequest, authentication)
        );
    }

}