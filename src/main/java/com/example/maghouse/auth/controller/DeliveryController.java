package com.example.maghouse.auth.controller;

import com.example.maghouse.delivery.DeliveryEntity;
import com.example.maghouse.delivery.DeliveryRequest;
import com.example.maghouse.delivery.DeliveryResponse;
import com.example.maghouse.delivery.DeliveryService;
import com.example.maghouse.delivery.status.DeliveryStatusRequest;
import com.example.maghouse.mapper.DeliveryResponseToDeliveryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/deliveries/")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;
    private final DeliveryResponseToDeliveryMapper deliveryResponseToDeliveryMapper;

    @PostMapping
    public ResponseEntity<DeliveryResponse> create(@RequestBody  DeliveryRequest deliveryRequest){
        DeliveryEntity deliveryEntity = deliveryService.createDelivery(deliveryRequest);
        DeliveryResponse deliveryResponse = deliveryResponseToDeliveryMapper.mapToResponse(deliveryEntity);
        if (deliveryRequest == null) {
            throw new IllegalArgumentException("Delivery request cannot be null");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(deliveryResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DeliveryResponse> updateDeliveryStatus(@RequestBody DeliveryStatusRequest deliveryStatusRequest,
                                         @PathVariable Long id){
        DeliveryEntity updatedDelivery = deliveryService.updateDeliveryStatus(deliveryStatusRequest, id);
        DeliveryResponse deliveryResponse = deliveryResponseToDeliveryMapper.mapToResponse(updatedDelivery);
        if(deliveryStatusRequest == null || id == null) {
            throw new IllegalArgumentException("Delivery status request cannot be null");
        }
        return ResponseEntity.ok(deliveryResponse);
    }
}
