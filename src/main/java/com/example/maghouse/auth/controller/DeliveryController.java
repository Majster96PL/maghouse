package com.example.maghouse.auth.controller;

import com.example.maghouse.delivery.Delivery;
import com.example.maghouse.delivery.DeliveryRequest;
import com.example.maghouse.delivery.DeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping(path = "auth/delivery")
@RestController
@RequiredArgsConstructor
public class DeliveryController {

    private static DeliveryService deliveryService;

    @PostMapping("/create")
    public Delivery create(@RequestBody  DeliveryRequest deliveryRequest){
        return deliveryService.createDelivery(deliveryRequest) ;
    }
}
