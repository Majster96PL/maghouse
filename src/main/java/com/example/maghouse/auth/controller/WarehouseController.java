package com.example.maghouse.auth.controller;

import com.example.maghouse.item.Item;
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

    @PostMapping("/assign-space-type/{itemId}")
    public Item assignSpaceType ( @PathVariable Long itemId,
                                @RequestBody WarehouseSpaceTypeRequest warehouseSpaceTypeRequest){
         return warehouseService.assignLocationCode(warehouseSpaceTypeRequest, itemId);

    }

    @PutMapping("/assign-location/{itemId}")
    public Item assignWarehouseLocation (@RequestBody  WarehouseLocationRequest warehouseLocationRequest,
                                         @PathVariable Long itemId){
         return warehouseService.assignItemsToWarehouseLocation( warehouseLocationRequest, itemId);
    }

}
