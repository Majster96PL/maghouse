package com.example.maghouse.auth.controller;

import com.example.maghouse.item.Item;
import com.example.maghouse.warehouse.Warehouse;
import com.example.maghouse.warehouse.WarehouseRequest;
import com.example.maghouse.warehouse.WarehouseService;
import com.example.maghouse.warehouse.location.WarehouseLocationRequest;
import com.example.maghouse.warehouse.spacetype.WarehouseSpaceTypeRequest;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/auth/warehouse/")
@AllArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;

    @PostMapping("/create")
    public Warehouse create(@RequestBody WarehouseRequest warehouseRequest){
        return warehouseService.createWarehouse(warehouseRequest);
    }

    @PostMapping("/assign-space-type/{itemId}")
    public Item assignSpaceType ( @PathVariable Long itemId,
                                @RequestBody WarehouseSpaceTypeRequest warehouseSpaceTypeRequest){
         return warehouseService.assignLocationCode(warehouseSpaceTypeRequest, itemId);

    }

    @PostMapping("/assign-location/{itemId}")
    public Item assignWarehouseLocation (@PathVariable Long itemId,
                                         @RequestBody  WarehouseLocationRequest warehouseLocationRequest){
         return warehouseService.assignItemsToWarehouseLocation( warehouseLocationRequest, itemId);
    }

    @PutMapping("/update-location/{itemId}")
    public Item updateWarehouseLocation(@PathVariable Long itemId,
                                        @RequestBody  WarehouseLocationRequest warehouseLocationRequest){
        return warehouseService.updatedItemsToWarehouseLocation(warehouseLocationRequest, itemId);
    }

}
