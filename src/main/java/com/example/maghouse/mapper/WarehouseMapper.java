package com.example.maghouse.mapper;


public interface WarehouseMapper<From, To, Entity> {
    To mapToWarehouseResponse(From warehouseRequest);
    Entity mapToEntityFromResponse(To warehouseResponse);
    To mapToWarehouse(Entity warehouseEntity);

}
