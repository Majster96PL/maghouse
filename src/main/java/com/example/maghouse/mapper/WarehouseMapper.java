package com.example.maghouse.mapper;


public interface WarehouseMapper<From, To> {
    To mapToEntity(From warehouseResponse);
    void updateEntityFromRequest(From warehouseRequest, To warehouseResponse);
}
