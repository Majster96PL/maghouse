package com.example.maghouse.warehouse;

import com.example.maghouse.warehouse.location.WarehouseLocation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WarehouseRequest {
    private WarehouseLocation warehouseLocation;
}
