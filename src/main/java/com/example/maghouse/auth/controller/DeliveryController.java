package com.example.maghouse.auth.controller;

import com.example.maghouse.delivery.DeliveryEntity;
import com.example.maghouse.delivery.DeliveryRequest;
import com.example.maghouse.delivery.DeliveryService;
import com.example.maghouse.delivery.status.DeliveryStatusRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/maghouse/")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;

    @PostMapping("/deliveries")
    public ResponseEntity<DeliveryEntity>  create(@RequestBody  DeliveryRequest deliveryRequest){
        DeliveryEntity delivery = deliveryService.createDelivery(deliveryRequest);
        if (deliveryRequest == null) {
            throw new IllegalArgumentException("Delivery request cannot be null");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(delivery);
    }

    @PutMapping("/deliveries/{id}")
    public ResponseEntity<DeliveryEntity> updateDeliveryStatus(@RequestBody DeliveryStatusRequest deliveryStatusRequest,
                                         @PathVariable Long id){
        if(deliveryStatusRequest == null || id == null) {
            throw new IllegalArgumentException("Delivery status request cannot be null");
        }

        DeliveryEntity updatedDelivery = deliveryService.updateDeliveryStatus(deliveryStatusRequest, id);
        return ResponseEntity.ok(updatedDelivery);
    }
}
