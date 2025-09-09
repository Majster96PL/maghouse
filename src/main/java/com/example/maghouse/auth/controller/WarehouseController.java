package com.example.maghouse.auth.controller;

import com.example.maghouse.item.ItemEntity;
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

    @PostMapping
    public WarehouseResponse create(@RequestBody WarehouseRequest warehouseRequest){
        WarehouseEntity warehouse = warehouseService.createWarehouse(warehouseRequest);
        return warehouseResponseToWarehouseMapper.mapToWarehouse(warehouse);
    }

    @PostMapping("/assign-space-type/{itemId}")
    public ItemEntity assignSpaceType (@PathVariable Long itemId,
                                       @RequestBody WarehouseSpaceTypeRequest warehouseSpaceTypeRequest){
         return warehouseService.assignLocationCode(warehouseSpaceTypeRequest, itemId);

    }

    @PostMapping("/assign-location/{itemId}")
    public ItemEntity assignWarehouseLocation (@PathVariable Long itemId,
                                         @RequestBody  WarehouseLocationRequest warehouseLocationRequest){
         return warehouseService.assignItemsToWarehouseLocation( warehouseLocationRequest, itemId);
    }

    @PutMapping("/{itemId}")
    public ItemEntity updateWarehouseLocation(@PathVariable Long itemId,
                                        @RequestBody  WarehouseLocationRequest warehouseLocationRequest){
        return warehouseService.updatedItemsToWarehouseLocation(warehouseLocationRequest, itemId);
    }

}
