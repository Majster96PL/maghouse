package com.example.maghouse.mapper;

public interface WarehouseMapper<From, To> {
    To mapToWarehouse(From warehouseRequest);
    void mapWarehouseRequestToWarehouseResponse(From warehouseRequest, To warehouseResponse);
}
