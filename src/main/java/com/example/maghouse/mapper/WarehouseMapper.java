package com.example.maghouse.mapper;

import com.example.maghouse.auth.registration.user.User;
import com.example.maghouse.item.Item;

import java.util.List;

public interface WarehouseMapper<From, To> {
    To mapToEntity(From warehouseRequest, User user, List<Item> items);
    To mapToResponse(From warehouse);
    void updateEntityFromRequest(From warehouseRequest, To warehouseResponse, User user, List<Item> items);
}
