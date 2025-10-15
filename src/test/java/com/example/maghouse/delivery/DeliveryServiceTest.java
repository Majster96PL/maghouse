package com.example.maghouse.delivery;

import com.example.maghouse.auth.registration.role.Role;
import com.example.maghouse.auth.registration.user.User;
import com.example.maghouse.delivery.status.DeliveryStatus;
import com.example.maghouse.delivery.status.DeliveryStatusRequest;
import com.example.maghouse.item.ItemEntity;
import com.example.maghouse.item.ItemRepository;
import com.example.maghouse.mapper.DeliveryResponseToDeliveryMapper;
import com.example.maghouse.warehouse.WarehouseService;
import com.example.maghouse.warehouse.location.WarehouseLocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DeliveryServiceTest {

    @Mock
    private DeliveryNumberGenerator deliveryNumberGenerator;

    @Mock
    private DeliveryResponseToDeliveryMapper deliveryResponseToDeliveryMapper;

    @Mock
    private DeliveryRepository deliveryRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private WarehouseService warehouseService;

    @InjectMocks
    private DeliveryService deliveryService;

    private ItemEntity item;
    private User user;
    private DeliveryEntity delivery;

    @BeforeEach
    void setUp(){
        user = User.builder()
                .id(1L)
                .firstname("John")
                .lastname("Kovalsky")
                .email("john.kovalsky@maghouse.com")
                .password("testPassword")
                .role(Role.DRIVER)
                .items(new ArrayList<>())
                .build();

        item = ItemEntity.builder()
                .name("ItemName")
                .itemCode("ItemCode")
                .quantity(100)
                .locationCode("RS10B")
                .user(user)
                .warehouseEntity(null)
                .deliveries(new ArrayList<>())
                .build();

        delivery = DeliveryEntity.builder()
                .supplier("inpost")
                .date(Date.valueOf(LocalDate.now()))
                .numberDelivery(null)
                .itemName(item.getName())
                .itemCode(item.getItemCode())
                .deliveryStatus(DeliveryStatus.CREATED)
                .warehouseLocation(WarehouseLocation.Rzeszow)
                .user(user)
                .item(item)
                .build();
    }

    @Test
    void shouldCreateDeliverySuccessfully() {
        String deliveryNumber = "1/05/2025";
        when(deliveryNumberGenerator.generateDeliveryNumber()).thenReturn("1/05/2025");
        LocalDate date = LocalDate.now();

        DeliveryRequest request = new DeliveryRequest(
                "inpost",
                "ItemName",
                "ItemCode",
                100
        );

        DeliveryResponse deliveryResponse = new DeliveryResponse();

        when(itemRepository.findByItemCode("ItemCode")).thenReturn(Optional.of(item));
        when(warehouseService.getWarehouseLocationByPrefix("R")).thenReturn(WarehouseLocation.Rzeszow);
        when(deliveryResponseToDeliveryMapper.mapToDeliveryResponse(
                eq(request),
                eq(deliveryNumber),
                eq(date),
                eq(user.getId()))
        ).thenReturn(deliveryResponse);

        when(deliveryResponseToDeliveryMapper.mapToDelivery(deliveryResponse))
                .thenReturn(delivery);

        when(deliveryRepository.save(delivery)).thenReturn(delivery);

        DeliveryEntity result = deliveryService.createDelivery(request, user);

        assertNotNull(result);
        assertEquals(WarehouseLocation.Rzeszow, result.getWarehouseLocation());
        assertEquals(delivery.getNumberDelivery(), result.getNumberDelivery());
        assertEquals(delivery.getSupplier(), result.getSupplier());
        assertEquals(delivery.getItemName(), result.getItemName());
    }

    @Test
    void shouldThrowExceptionWhenNotAuthenticatedUser(){

        DeliveryRequest request = new DeliveryRequest(
                "inpost",
                "ItemName",
                "ItemCode",
                100
        );

        assertThrows(NullPointerException.class,
                () -> deliveryService.createDelivery(request, user));

    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundByEmail(){
        DeliveryRequest request = new DeliveryRequest(
                "inpost",
                "ItemName",
                "ItemCode",
                100
        );

        assertThrows(NullPointerException.class, () -> deliveryService.createDelivery(request, user));
    }

    @Test
    void shouldThrowExceptionWhenDeliveryNumberGeneratorReturnsNull(){
        DeliveryRequest request = new DeliveryRequest(
                "inpost",
                "ItemName",
                "ItemCode",
                100
        );

        when(deliveryNumberGenerator.generateDeliveryNumber()).thenReturn(null);

        assertThrows(NullPointerException.class, () -> deliveryService.createDelivery(request, user));
    }

    @Test
    void shouldUpdateDeliveryStatusSuccessfully(){
        DeliveryStatusRequest deliveryStatusRequest = new DeliveryStatusRequest(DeliveryStatus.IN_PROGRESS);

        when(deliveryRepository.findById(delivery.getId())).thenReturn(Optional.of(delivery));
        when(deliveryRepository.save(delivery)).thenReturn(delivery);

        DeliveryEntity result = deliveryService.updateDeliveryStatus(deliveryStatusRequest, delivery.getId());

        assertNotNull(result);
        assertEquals(DeliveryStatus.IN_PROGRESS, result.getDeliveryStatus());
    }

    @Test
    void shouldUpdateStatusToDeliveryAndUpdateItemQuantity(){
        DeliveryStatusRequest deliveryStatusRequest = new DeliveryStatusRequest(DeliveryStatus.DELIVERED);

        int initialQuantity = item.getQuantity();
        int deliveryQuantity = delivery.getQuantity();

        when(deliveryRepository.findById(delivery.getId())).thenReturn(Optional.of(delivery));
        when(itemRepository.findByItemCode(delivery.getItemCode())).thenReturn(Optional.of(item));
        when(itemRepository.save(item)).thenReturn(item);
        when(deliveryRepository.save(delivery)).thenReturn(delivery);

        DeliveryEntity result = deliveryService.updateDeliveryStatus(deliveryStatusRequest, delivery.getId());

        assertNotNull(result);
        assertEquals(DeliveryStatus.DELIVERED, result.getDeliveryStatus());
        assertEquals(initialQuantity + deliveryQuantity, item.getQuantity());
        verify(itemRepository).save(item);
    }

    @Test
   void shouldThrowExceptionWhenUpdatingStatusForNotAuthenticatedUser(){
        DeliveryStatusRequest deliveryStatusRequest = new DeliveryStatusRequest(DeliveryStatus.IN_PROGRESS);

        when(deliveryRepository.findById(delivery.getId())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> deliveryService.updateDeliveryStatus(deliveryStatusRequest, delivery.getId()));
   }

   @Test
   void shouldThrowExceptionWhenUserNotFoundDuringStatusUpdate(){
        DeliveryStatusRequest deliveryStatusRequest = new DeliveryStatusRequest(DeliveryStatus.IN_PROGRESS);

        assertThrows(IllegalArgumentException.class,
                () -> deliveryService.updateDeliveryStatus(deliveryStatusRequest, delivery.getId()));
   }

   @Test
   void shouldThrowExceptionWhenDeliveryNotFoundDuringStatusUpdate(){
        DeliveryStatusRequest deliveryStatusRequest = new DeliveryStatusRequest(DeliveryStatus.IN_PROGRESS);
        when(deliveryRepository.findById(delivery.getId())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> deliveryService.updateDeliveryStatus(deliveryStatusRequest, delivery.getId()));
    }

    @Test
    void shouldThrowExceptionWhenItemNotFoundDuringStatusUpdate(){
        DeliveryStatusRequest deliveryStatusRequest = new DeliveryStatusRequest(DeliveryStatus.DELIVERED);
        when(deliveryRepository.findById(delivery.getId())).thenReturn(Optional.of(delivery));
        when(itemRepository.findByItemCode(delivery.getItemCode())).thenReturn(Optional.empty());

        deliveryService.updateDeliveryStatus(deliveryStatusRequest, delivery.getId());

        verify(deliveryRepository, times(1)).findById(delivery.getId());
        verify(itemRepository, times(1)).findByItemCode(delivery.getItemCode());
        verify(deliveryRepository, times(1)).save(any(DeliveryEntity.class));
        verify(itemRepository, never()).save(any(ItemEntity.class));
    }
}