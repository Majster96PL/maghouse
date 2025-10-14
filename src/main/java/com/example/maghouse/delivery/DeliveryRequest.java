package com.example.maghouse.delivery;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryRequest {
    private String supplier;
    private String itemName;
    private String itemCode;
    private int quantity;
}
