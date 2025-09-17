package com.example.maghouse.mapper;


public interface WarehouseMapper<Request, Response, Entity> {
    Response mapToWarehouseResponse(Request warehouseRequest);

    Entity mapToEntityFromResponse(Response warehouseResponse);

    Response mapToWarehouse(Entity warehouseEntity);

}
