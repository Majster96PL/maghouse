package com.example.maghouse.mapper;

public interface DeliveryMapper<From, To> {
    To mapToDelivery(From deliveryRequest);

}
