package com.example.maghouse.mapper;

import java.time.LocalDate;

public interface DeliveryMapper<DeliveryRequest, DeliveryResponse, Entity> {
    DeliveryResponse mapToDeliveryResponse(DeliveryRequest deliveryRequest, String numberDelivery, LocalDate date, long userId);
    Entity mapToDelivery(DeliveryResponse deliveryResponse);
    DeliveryResponse mapToResponse(Entity deliveryEntity);
}
