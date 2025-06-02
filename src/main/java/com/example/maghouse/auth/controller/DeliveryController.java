package com.example.maghouse.auth.controller;

import com.example.maghouse.delivery.Delivery;
import com.example.maghouse.delivery.DeliveryRequest;
import com.example.maghouse.delivery.DeliveryService;
import com.example.maghouse.delivery.status.DeliveryStatusRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequestMapping(path = "auth/delivery")
@RestController
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;

    @PostMapping("/create")
    public Delivery create(@RequestBody  DeliveryRequest deliveryRequest){
        if (deliveryRequest == null) {
            throw new IllegalArgumentException("Delivery request cannot be null");
        }

        return deliveryService.createDelivery(deliveryRequest);
    }

    @PutMapping("/update-delivery-status/{id}")
    public Delivery updateDeliveryStatus(@RequestBody DeliveryStatusRequest deliveryStatusRequest,
                                         @PathVariable Long id){
        if(deliveryStatusRequest == null || id == null) {
            throw new IllegalArgumentException("Delivery status request cannot be null");
        }
        return deliveryService.updateDeliveryStatus(deliveryStatusRequest, id);
    }
}
