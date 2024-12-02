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

        int deliveryCount = deliveryRepository.countByMonthAndYear(month, year) + 1;

        return deliveryCount  + "/" + month + "/" + year;
    }
}
