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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(path = "/warehouses/")
@AllArgsConstructor
@Tag(name = "Warehouse Structure", description = "Endpoints for creating warehouses and managing item storage locations.")
@SecurityRequirement(name = "bearerAuth")
public class WarehouseController {

    private final WarehouseService warehouseService;
    private final WarehouseResponseToWarehouseMapper warehouseResponseToWarehouseMapper;
    private final ItemResponseToItemMapper itemResponseToItemMapper;

    @PostMapping
    @Operation(summary = "Create a new warehouse",
            description = "Initializes a new warehouse structure in the system.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Warehouse successfully created",
                    content = @Content(schema = @Schema(implementation = WarehouseResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request or warehouse already exists",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden (admin:create required)",
                    content = @Content)
    })
    public WarehouseResponse create(@RequestBody WarehouseRequest warehouseRequest) {
        WarehouseEntity warehouse = warehouseService.createWarehouse(warehouseRequest);
        return warehouseResponseToWarehouseMapper.mapToWarehouse(warehouse);
    }

    @PostMapping("/assign-space-type/{itemId}")
    @Operation(summary = "Assign a space type to an item",
            description = "Links a specific item type to a required warehouse space type (e.g., cold storage, hazardous).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Space type successfully assigned to item",
                    content = @Content(schema = @Schema(implementation = ItemResponse.class))),
            @ApiResponse(responseCode = "404", description = "Item or space type not found",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden (manager:update required)",
                    content = @Content)
    })
    public ItemResponse assignSpaceType(@PathVariable Long itemId,
                                        @RequestBody WarehouseSpaceTypeRequest warehouseSpaceTypeRequest) {
        ItemEntity item = warehouseService.assignWarehouseSpaceType(warehouseSpaceTypeRequest, itemId);
        return itemResponseToItemMapper.mapToItem(item);

    }

    @PostMapping("/assign-location/{itemId}")
    @Operation(summary = "Assign item to a warehouse location",
            description = "Assigns the item's current stock to a specific physical location in the warehouse.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Item location successfully assigned",
                    content = @Content(schema = @Schema(implementation = ItemResponse.class))),
            @ApiResponse(responseCode = "404", description = "Item or location not found",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden (warehouseman:update required)",
                    content = @Content)
    })
    public ItemResponse assignWarehouseLocation(@PathVariable Long itemId,
                                                @RequestBody WarehouseLocationRequest warehouseLocationRequest) {
        ItemEntity item = warehouseService.assignItemsToWarehouseLocation(warehouseLocationRequest, itemId);
        return itemResponseToItemMapper.mapToItem(item);
    }

    @PutMapping("/items/{itemId}/location")
    @Operation(summary = "Update item location",
            description = "Moves an item's stock from its current location to a new one.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Item location successfully updated",
                    content = @Content(schema = @Schema(implementation = ItemResponse.class))),
            @ApiResponse(responseCode = "404", description = "Item or new location not found",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden (warehouseman:update required)",
                    content = @Content)
    })
    public ItemResponse updateWarehouseLocation(@PathVariable Long itemId,
                                                @RequestBody WarehouseLocationRequest warehouseLocationRequest) {
        ItemEntity item = warehouseService.updatedItemsToWarehouseLocation(warehouseLocationRequest, itemId);
        return itemResponseToItemMapper.mapToItem(item);
    }

}
