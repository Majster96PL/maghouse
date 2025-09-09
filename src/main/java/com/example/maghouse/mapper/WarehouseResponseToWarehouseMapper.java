package com.example.maghouse.mapper;

import com.example.maghouse.item.Item;
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
                .warehouseSpaceType(warehouseRequest.getWarehouseSpaceType())
                .warehouseLocation(warehouseRequest.getWarehouseLocation())
                .userId(null)
                .itemsId(new ArrayList<>())
                .build();
    }

    @Override
    public WarehouseEntity mapToEntityFromResponse(WarehouseResponse warehouseResponse) {
        return WarehouseEntity.builder()
                .warehouseSpaceType(warehouseResponse.getWarehouseSpaceType())
                .warehouseLocation(warehouseResponse.getWarehouseLocation())
                .user(null)
                .items(new ArrayList<>())
                .build();
    }

    @Override
    public WarehouseResponse mapToWarehouse(WarehouseEntity warehouseEntity) {
        return WarehouseResponse.builder()
                .warehouseSpaceType(warehouseEntity.getWarehouseSpaceType())
                .warehouseLocation(warehouseEntity.getWarehouseLocation())
                .userId(warehouseEntity.getId())
                .itemsId(warehouseEntity.getItems().stream()
                        .map(Item::getId)
                        .collect(Collectors.toList()))
                .build();
    }
}

