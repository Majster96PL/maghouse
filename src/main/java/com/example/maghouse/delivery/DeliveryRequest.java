package com.example.maghouse.delivery;

import com.example.maghouse.warehouse.location.WarehouseLocation;
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
    private WarehouseLocation warehouseLocation;
}
