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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/warehouses/")
@AllArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;
    private final WarehouseResponseToWarehouseMapper warehouseResponseToWarehouseMapper;
    private final ItemResponseToItemMapper itemResponseToItemMapper;

    @PostMapping
    public ResponseEntity<WarehouseResponse> create(@RequestBody WarehouseRequest warehouseRequest) {
        WarehouseEntity warehouse = warehouseService.createWarehouse(warehouseRequest);
        WarehouseResponse warehouseResponse = warehouseResponseToWarehouseMapper.mapToWarehouse(warehouse);
        if (warehouseResponse == null) {
            throw new IllegalArgumentException("Mapper returned null for WarehouseResponse");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(warehouseResponse);
    }

    @PostMapping("/assign-space-type/{itemId}")
    public ResponseEntity<ItemResponse> assignSpaceType(@PathVariable Long itemId,
                                        @RequestBody WarehouseSpaceTypeRequest warehouseSpaceTypeRequest) {
        ItemEntity item = warehouseService.assignWarehouseSpaceType(warehouseSpaceTypeRequest, itemId);
        ItemResponse response = itemResponseToItemMapper.mapToItem(item);
        return ResponseEntity.ok(response);

    }

    @PostMapping("/assign-location/{itemId}")
    public ResponseEntity<ItemResponse>  assignWarehouseLocation(@PathVariable Long itemId,
                                                @RequestBody WarehouseLocationRequest warehouseLocationRequest) {
        ItemEntity item = warehouseService.assignItemsToWarehouseLocation(warehouseLocationRequest, itemId);
        ItemResponse response = itemResponseToItemMapper.mapToItem(item);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/items/{itemId}/location")
    public ResponseEntity<ItemResponse>  updateWarehouseLocation(@PathVariable Long itemId,
                                                @RequestBody WarehouseLocationRequest warehouseLocationRequest) {
        ItemEntity item = warehouseService.updatedItemsToWarehouseLocation(warehouseLocationRequest, itemId);
        ItemResponse response = itemResponseToItemMapper.mapToItem(item);
        return ResponseEntity.ok(response);
    }
}
