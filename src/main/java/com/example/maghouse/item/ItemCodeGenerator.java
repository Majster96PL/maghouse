package com.example.maghouse.item;

import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class ItemCodeGenerator {

    private static final Random RANDOM = new Random();

    public String generateItemCode() {
        int firstPart = generateRandomNumber(1000, 9999);
        int secondPart = generateRandomNumber(10, 99);
        int thirdPart = generateRandomNumber(100, 999);
        int fourthPart = generateRandomNumber(1000, 9999);

        return String.format("%04d-%02d-%03d-%04d", firstPart, secondPart, thirdPart, fourthPart);
    }
    private int generateRandomNumber(int min, int max){
        return RANDOM.nextInt((max - min) + 1) + min;
    }
}
