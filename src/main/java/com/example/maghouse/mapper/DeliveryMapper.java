package com.example.maghouse.mapper;

import com.example.maghouse.auth.registration.user.User;
import com.example.maghouse.delivery.DeliveryEntity;

import java.time.LocalDate;

public interface DeliveryMapper<From, To> {
    To mapToDeliveryResponse(From deliveryRequest, String numberDelivery, LocalDate date, User user);
    DeliveryEntity mapToDelivery(To deliveryResponse);

}
