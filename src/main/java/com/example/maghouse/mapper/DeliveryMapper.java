package com.example.maghouse.mapper;

import com.example.maghouse.delivery.DeliveryEntity;
import com.example.maghouse.delivery.DeliveryResponse;

import java.time.LocalDate;

public interface DeliveryMapper<From, To, Entity> {
    To mapToDeliveryResponse(From deliveryRequest, String numberDelivery, LocalDate date, long userId);
    DeliveryEntity mapToDelivery(To deliveryResponse);
    DeliveryResponse mapToResponse(DeliveryEntity deliveryEntity);
}
