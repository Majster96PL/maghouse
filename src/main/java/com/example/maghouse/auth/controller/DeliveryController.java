package com.example.maghouse.auth.controller;

import com.example.maghouse.delivery.Delivery;
import com.example.maghouse.delivery.DeliveryRequest;
import com.example.maghouse.delivery.DeliveryService;
import com.example.maghouse.delivery.status.DeliveryStatusRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/auth/delivery/")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;

    @PostMapping("/create")
    public ResponseEntity<Delivery>  create(@RequestBody  DeliveryRequest deliveryRequest){
        Delivery delivery = deliveryService.createDelivery(deliveryRequest);
        if (deliveryRequest == null) {
            throw new IllegalArgumentException("Delivery request cannot be null");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(delivery);
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
