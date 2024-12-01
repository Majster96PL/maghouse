package com.example.maghouse.delivery;

import com.example.maghouse.auth.registration.user.User;
import com.example.maghouse.warehouse.location.WarehouseLocation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryResponse {
    private String supplier;
    private Data data;
    private String numberDelivery;
    private String itemName;
    private String itemCode;
    private int quantity;
    private DeliveryStatus deliveryStatus;
    private WarehouseLocation warehouseLocation;
    private User user;
}
