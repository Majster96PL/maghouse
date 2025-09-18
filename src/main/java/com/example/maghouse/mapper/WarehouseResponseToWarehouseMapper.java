package com.example.maghouse.mapper;

import com.example.maghouse.item.ItemEntity;
import com.example.maghouse.warehouse.WarehouseEntity;
import com.example.maghouse.warehouse.WarehouseRequest;
import com.example.maghouse.warehouse.WarehouseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class WarehouseResponseToWarehouseMapper implements WarehouseMapper<WarehouseRequest, WarehouseResponse, WarehouseEntity> {
    @Override
    public WarehouseResponse mapToWarehouseResponse(WarehouseRequest warehouseRequest) {
        return WarehouseResponse.builder()
                .warehouseLocation(warehouseRequest.getWarehouseLocation())
                .itemsId(new ArrayList<>())
                .build();
    }

    @Override
    public WarehouseEntity mapToEntityFromResponse(WarehouseResponse warehouseResponse) {
        return WarehouseEntity.builder()
                .warehouseLocation(warehouseResponse.getWarehouseLocation())
                .items(new ArrayList<>())
                .build();
    }

    @Override
    public WarehouseResponse mapToWarehouse(WarehouseEntity warehouseEntity) {
        return WarehouseResponse.builder()
                .warehouseLocation(warehouseEntity.getWarehouseLocation())
                .userId(warehouseEntity.getUser().getId())
                .itemsId(warehouseEntity.getItems().stream()
                        .map(ItemEntity::getId)
                        .collect(Collectors.toList()))
                .build();
    }
}

