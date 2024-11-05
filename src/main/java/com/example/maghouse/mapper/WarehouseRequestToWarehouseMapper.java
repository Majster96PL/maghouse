package com.example.maghouse.mapper;

import com.example.maghouse.warehouse.Warehouse;
import com.example.maghouse.warehouse.WarehouseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class WarehouseRequestToWarehouseMapper implements WarehouseMapper<WarehouseResponse, Warehouse> {

    @Override
    public Warehouse mapToEntity(WarehouseResponse warehouseResponse) {
        return Warehouse.builder()
                .id(warehouseResponse.getId())
                .warehouseSpaceType(warehouseResponse.getWarehouseSpaceType())
                .location(warehouseResponse.getLocation())
                .user(warehouseResponse.getUser())
                .items(warehouseResponse.getItems())
                .build();
    }

    @Override
    public void updateEntityFromRequest(WarehouseResponse warehouseRequest, Warehouse warehouseResponse) {
        warehouseResponse.setWarehouseSpaceType(warehouseRequest.getWarehouseSpaceType());
        warehouseResponse.setLocation(warehouseRequest.getLocation());
    }
}

