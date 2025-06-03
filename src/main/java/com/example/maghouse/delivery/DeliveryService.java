package com.example.maghouse.delivery;


import com.example.maghouse.auth.registration.user.UserRepository;
import com.example.maghouse.delivery.status.DeliveryStatus;
import com.example.maghouse.delivery.status.DeliveryStatusRequest;
import com.example.maghouse.item.ItemRepository;
import com.example.maghouse.mapper.DeliveryResponseToDeliveryMapper;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@AllArgsConstructor
public class DeliveryService {

    private final UserRepository userRepository;
    private final DeliveryNumberGenerator deliveryNumberGenerator;
    private final DeliveryResponseToDeliveryMapper deliveryResponseToDeliveryMapper;
    private final DeliveryRepository deliveryRepository;
    private final ItemRepository itemRepository;

    @Transactional
    public Delivery createDelivery(DeliveryRequest deliveryRequest) {
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
                deliveryRequest, numberDelivery, data, user);
        var delivery = deliveryResponseToDeliveryMapper.mapToDelivery(deliveryResponse);

        return deliveryRepository.save(delivery);
    }

    public Delivery updateDeliveryStatus(DeliveryStatusRequest deliveryStatusRequest, Long id) {
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
            var item = itemRepository.findByItemCode(delivery.getItemCode())
                    .orElseThrow(() -> new IllegalArgumentException("Item not found with itemCode: " + delivery.getItemCode()));
            item.setQuantity(item.getQuantity() + delivery.getQuantity());
            itemRepository.save(item);
        }
        return deliveryRepository.save(delivery);
    }
}
