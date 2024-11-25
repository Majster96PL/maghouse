package com.example.maghouse.delivery;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@AllArgsConstructor
public class DeliveryNumberGenerator {

    private final DeliveryRepository deliveryRepository;


    public String generateDeliveryNumber(){

        LocalDate actualDate = LocalDate.now();
        int month = actualDate.getMonthValue();
        int year = actualDate.getYear();

        List<Delivery> deliveries = deliveryRepository.findByMonthAndYear(month, year);
        int deliveryCount = deliveries.size() + 1;

        return deliveryCount  + "/" + month + "/" + year;
    }
}
