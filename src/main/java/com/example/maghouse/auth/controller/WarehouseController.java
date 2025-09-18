package com.example.maghouse.auth.controller;

import com.example.maghouse.item.ItemEntity;
import com.example.maghouse.item.ItemResponse;
import com.example.maghouse.mapper.ItemResponseToItemMapper;
import com.example.maghouse.mapper.WarehouseResponseToWarehouseMapper;
import com.example.maghouse.warehouse.WarehouseEntity;
import com.example.maghouse.warehouse.WarehouseRequest;
import com.example.maghouse.warehouse.WarehouseResponse;
import com.example.maghouse.warehouse.WarehouseService;
import com.example.maghouse.warehouse.location.WarehouseLocationRequest;
import com.example.maghouse.warehouse.spacetype.WarehouseSpaceTypeRequest;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/warehouses/")
@AllArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;
    private final WarehouseResponseToWarehouseMapper warehouseResponseToWarehouseMapper;
    private final ItemResponseToItemMapper itemResponseToItemMapper;

    @PostMapping
    public WarehouseResponse create(@RequestBody WarehouseRequest warehouseRequest) {
        WarehouseEntity warehouse = warehouseService.createWarehouse(warehouseRequest);
        return warehouseResponseToWarehouseMapper.mapToWarehouse(warehouse);
    }

    @PostMapping("/assign-space-type/{itemId}")
    public ItemResponse assignSpaceType(@PathVariable Long itemId,
                                        @RequestBody WarehouseSpaceTypeRequest warehouseSpaceTypeRequest) {
        ItemEntity item = warehouseService.assignWarehouseSpaceType(warehouseSpaceTypeRequest, itemId);
        return itemResponseToItemMapper.mapToItem(item);

    }

    @PostMapping("/assign-location/{itemId}")
    public ItemResponse assignWarehouseLocation(@PathVariable Long itemId,
                                                @RequestBody WarehouseLocationRequest warehouseLocationRequest) {
        ItemEntity item = warehouseService.assignItemsToWarehouseLocation(warehouseLocationRequest, itemId);
        return itemResponseToItemMapper.mapToItem(item);
    }

    @PutMapping("/items/{itemId}/location")
    public ItemResponse updateWarehouseLocation(@PathVariable Long itemId,
                                                @RequestBody WarehouseLocationRequest warehouseLocationRequest) {
        ItemEntity item = warehouseService.updatedItemsToWarehouseLocation(warehouseLocationRequest, itemId);
        return itemResponseToItemMapper.mapToItem(item);
    }

}
