package com.example.maghouse.warehouse;

import com.example.maghouse.warehouse.location.WarehouseLocation;
import com.example.maghouse.warehouse.spacetype.WarehouseSpaceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WarehouseResponse {
    private long id;
    private WarehouseSpaceType warehouseSpaceType;
    private WarehouseLocation warehouseLocation;
    private Long userId;
    private List<Long> itemsId;
}
