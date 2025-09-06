package com.example.maghouse.mapper;

import com.example.maghouse.delivery.DeliveryEntity;
import com.example.maghouse.delivery.DeliveryRequest;
import com.example.maghouse.delivery.DeliveryResponse;
import com.example.maghouse.delivery.status.DeliveryStatus;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.time.LocalDate;

@Component
public class DeliveryResponseToDeliveryMapper implements DeliveryMapper<DeliveryRequest, DeliveryResponse, DeliveryEntity> {

    @Override
    public DeliveryResponse mapToDeliveryResponse(DeliveryRequest deliveryRequest,
                                                  String numberDelivery, LocalDate date,
                                                  long userId) {
        return DeliveryResponse.builder()
                .supplier(deliveryRequest.getSupplier())
                .data(date)
                .numberDelivery(numberDelivery)
                .itemName(deliveryRequest.getItemName())
                .itemCode(deliveryRequest.getItemCode())
                .quantity(deliveryRequest.getQuantity())
                .deliveryStatus(DeliveryStatus.CREATED)
                .warehouseLocation(deliveryRequest.getWarehouseLocation())
                .userId(userId)
                .build();
    }

    @Override
    public DeliveryEntity mapToDelivery(DeliveryResponse deliveryResponse) {
        return DeliveryEntity.builder()
                .supplier(deliveryResponse.getSupplier())
                .date(Date.valueOf(deliveryResponse.getData()))
                .numberDelivery(deliveryResponse.getNumberDelivery())
                .itemName(deliveryResponse.getItemName())
                .itemCode(deliveryResponse.getItemCode())
                .quantity(deliveryResponse.getQuantity())
                .deliveryStatus(deliveryResponse.getDeliveryStatus())
                .warehouseLocation(deliveryResponse.getWarehouseLocation())
                .user(null)
                .build();
    }

    @Override
    public DeliveryResponse mapToResponse(DeliveryEntity deliveryEntity) {
        return DeliveryResponse.builder()
                .supplier(deliveryEntity.getSupplier())
                .data(deliveryEntity.getDate().toLocalDate())
                .numberDelivery(deliveryEntity.getNumberDelivery())
                .itemName(deliveryEntity.getItemName())
                .itemCode(deliveryEntity.getItemCode())
                .quantity(deliveryEntity.getQuantity())
                .deliveryStatus(deliveryEntity.getDeliveryStatus())
                .warehouseLocation(deliveryEntity.getWarehouseLocation())
                .userId(deliveryEntity.getUser().getId())
                .build();
    }
}


