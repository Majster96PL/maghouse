package com.example.maghouse.delivery;

import com.example.maghouse.delivery.status.DeliveryStatus;
import com.example.maghouse.warehouse.location.WarehouseLocation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeliveryResponse {
    private String supplier;
    private LocalDate data;
    private String numberDelivery;
    private String itemName;
    private String itemCode;
    private int quantity;
    private DeliveryStatus deliveryStatus;
    private WarehouseLocation warehouseLocation;
    private long userId;
}
