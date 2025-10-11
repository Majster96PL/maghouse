package com.example.maghouse.delivery;

import com.example.maghouse.auth.registration.user.UserRepository;
import com.example.maghouse.delivery.status.DeliveryStatus;
import com.example.maghouse.delivery.status.DeliveryStatusRequest;
import com.example.maghouse.item.ItemEntity;
import com.example.maghouse.item.ItemRepository;
import com.example.maghouse.mapper.DeliveryResponseToDeliveryMapper;
import com.example.maghouse.warehouse.WarehouseService;
import com.example.maghouse.warehouse.location.WarehouseLocation;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class DeliveryService {

    private final UserRepository userRepository;
    private final DeliveryNumberGenerator deliveryNumberGenerator;
    private final DeliveryResponseToDeliveryMapper deliveryResponseToDeliveryMapper;
    private final DeliveryRepository deliveryRepository;
    private final ItemRepository itemRepository;
    private final WarehouseService warehouseService;

    public List<DeliveryEntity> getAllDeliveries() {
        return deliveryRepository.findAll();
    }

    public List<DeliveryEntity> getDeliveriesByStatus(DeliveryStatus status) {
        return deliveryRepository.findByDeliveryStatus(status);
    }

    public Optional<DeliveryEntity> getDeliveryByNumber(String deliveryNumber) {
        return deliveryRepository.findByNumberDelivery(deliveryNumber);
    }

    public List<DeliveryEntity> getDeliveriesBySupplier(String supplierName) {
        return deliveryRepository.findBySupplierContainingIgnoreCase(supplierName);
    }

    public List<DeliveryEntity> getDeliveriesByLocation(WarehouseLocation warehouseLocation) {
        return deliveryRepository.findByWarehouseLocation(warehouseLocation);
    }

    public List<DeliveryEntity> getDeliveriesByItemCode(String itemCode) {
        return deliveryRepository.findByItemCode(itemCode);
    }

    public Optional<DeliveryEntity> getDeliveryById(Long id) {
        return deliveryRepository.findById(id);
    }

    @Transactional
    public DeliveryEntity createDelivery(DeliveryRequest deliveryRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("User is not authenticated");
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        var user = userRepository.findUserByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User with email not found!"));
        LocalDate data = LocalDate.now();
        String numberDelivery = deliveryNumberGenerator.generateDeliveryNumber();
        if (numberDelivery == null) {
            throw new NullPointerException("Delivery number cannot be null");
        }
        var deliveryResponse = deliveryResponseToDeliveryMapper.mapToDeliveryResponse(
                deliveryRequest, numberDelivery, data, user.getId());
        var delivery = deliveryResponseToDeliveryMapper.mapToDelivery(deliveryResponse);
        delivery.setUser(user);
        WarehouseLocation warehouseLocation = determineWarehouseLocationFromItemLocation(deliveryRequest.getItemCode());
        delivery.setWarehouseLocation(warehouseLocation);

        return deliveryRepository.save(delivery);
    }

    public DeliveryEntity updateDeliveryStatus(DeliveryStatusRequest deliveryStatusRequest, Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("User is not authenticated");
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        var user = userRepository.findUserByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User with email not found!"));
        var delivery = deliveryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Delivery not found!"));
        delivery.setDeliveryStatus(deliveryStatusRequest.getDeliveryStatus());
        if (deliveryStatusRequest.getDeliveryStatus() == DeliveryStatus.DELIVERED) {
            itemRepository.findByItemCode(delivery.getItemCode()).ifPresent(item -> {
                item.setQuantity(item.getQuantity() + delivery.getQuantity());
                itemRepository.save(item);
            });
        }
        return deliveryRepository.save(delivery);
    }

    private WarehouseLocation determineWarehouseLocationFromItemLocation(String itemCode) {
        ItemEntity item = itemRepository.findByItemCode(itemCode)
                .orElseThrow(() -> new IllegalArgumentException("Item with code " + itemCode + " not found!"));

        String locationCode = item.getLocationCode();
        if (locationCode == null || locationCode.isEmpty()) {
            throw new IllegalArgumentException("Item location code not found!");
        }
        String locationPrefix = locationCode.substring(0, 1).toUpperCase();
        return warehouseService.getWarehouseLocationByPrefix(locationPrefix);
    }
}
