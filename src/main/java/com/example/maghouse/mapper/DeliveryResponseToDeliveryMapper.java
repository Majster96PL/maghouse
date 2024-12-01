package com.example.maghouse.mapper;

import com.example.maghouse.delivery.Delivery;
import com.example.maghouse.delivery.DeliveryResponse;

import java.sql.Date;
import java.time.LocalDate;

public class DeliveryResponseToDeliveryMapper implements DeliveryMapper<DeliveryResponse, Delivery> {
    @Override
    public Delivery mapToDelivery(DeliveryResponse deliveryResponse) {
        return Delivery.builder()
                .supplier(deliveryResponse.getSupplier())
                .date(Date.valueOf(LocalDate.now()))
                .numberDelivery(deliveryResponse.getNumberDelivery())
                .itemName(deliveryResponse.getItemName())
                .itemCode(deliveryResponse.getItemCode())
                .deliveryStatus(deliveryResponse.getDeliveryStatus())
                .warehouseLocation(deliveryResponse.getWarehouseLocation())
                .user(deliveryResponse.getUser())
                .build();
    }
}
