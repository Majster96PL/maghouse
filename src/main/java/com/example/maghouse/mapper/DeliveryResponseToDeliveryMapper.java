package com.example.maghouse.mapper;

import com.example.maghouse.auth.registration.user.User;
import com.example.maghouse.delivery.Delivery;
import com.example.maghouse.delivery.DeliveryRequest;
import com.example.maghouse.delivery.DeliveryResponse;
import com.example.maghouse.delivery.DeliveryStatus;
import org.springframework.stereotype.Component;


import java.sql.Date;
import java.time.LocalDate;

@Component
public class DeliveryResponseToDeliveryMapper implements DeliveryMapper<DeliveryRequest, DeliveryResponse> {

    @Override
    public DeliveryResponse mapToDeliveryResponse(DeliveryRequest deliveryRequest, String numberDelivery, LocalDate date, User user) {
        return DeliveryResponse.builder()
                .supplier(deliveryRequest.getSupplier())
                .data(date)
                .numberDelivery(numberDelivery)
                .itemName(deliveryRequest.getItemName())
                .itemCode(deliveryRequest.getItemCode())
                .quantity(deliveryRequest.getQuantity())
                .deliveryStatus(DeliveryStatus.CREATED)
                .warehouseLocation(deliveryRequest.getWarehouseLocation())
                .user(user)
                .build();
    }

    @Override
    public Delivery mapToDelivery(DeliveryResponse deliveryResponse) {
        return Delivery.builder()
                .supplier(deliveryResponse.getSupplier())
                .date(Date.valueOf(deliveryResponse.getData()))
                .numberDelivery(deliveryResponse.getNumberDelivery())
                .itemName(deliveryResponse.getItemName())
                .itemCode(deliveryResponse.getItemCode())
                .quantity(deliveryResponse.getQuantity())
                .deliveryStatus(deliveryResponse.getDeliveryStatus())
                .warehouseLocation(deliveryResponse.getWarehouseLocation())
                .user(deliveryResponse.getUser())
                .build();
    }
}
