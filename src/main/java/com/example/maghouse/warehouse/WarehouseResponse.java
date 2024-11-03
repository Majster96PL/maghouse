package com.example.maghouse.warehouse;

import com.example.maghouse.auth.registration.user.User;
import com.example.maghouse.item.Item;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WarehouseResponse {
    private long id;
    private WarehouseSpaceType warehouseSpaceType;
    private String location;
    private User user;
    private Item item;
}