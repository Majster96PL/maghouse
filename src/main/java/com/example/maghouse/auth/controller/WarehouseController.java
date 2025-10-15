package com.example.maghouse.auth.controller;

import com.example.maghouse.auth.registration.user.User;
import com.example.maghouse.auth.registration.user.UserService;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/warehouses/")
@Tag(name = "Warehouse Structure", description = "Endpoints for creating warehouses and managing item storage locations.")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
public class WarehouseController extends BaseController {

    private final WarehouseService warehouseService;
    private final WarehouseResponseToWarehouseMapper warehouseResponseToWarehouseMapper;
    private final ItemResponseToItemMapper itemResponseToItemMapper;

    public WarehouseController(UserService userService,
                                  WarehouseService warehouseService,
                                  WarehouseResponseToWarehouseMapper warehouseResponseToWarehouseMapper,
                                  ItemResponseToItemMapper itemResponseToItemMapper) {
        super(userService);
        this.warehouseService = warehouseService;
        this.warehouseResponseToWarehouseMapper = warehouseResponseToWarehouseMapper;
        this.itemResponseToItemMapper = itemResponseToItemMapper;
    }


    @GetMapping
    @Operation(summary = "Retrieve all warehouses",
            description = "Retrieved a list of a defined warehouse structures.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of warehouses",
                    content = @Content(schema = @Schema(implementation = WarehouseResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden (Access denied)",
                    content = @Content)
    })
    public ResponseEntity<List<WarehouseResponse>> getAllWarehouses() {
        User user = getAuthenticatedUser();
        log.info("User {} requested all warehouses", user.getEmail());
        List<WarehouseEntity> warehouses = warehouseService.getAllWarehouses();
        List<WarehouseResponse> responses = warehouses.stream()
                .map(warehouseResponseToWarehouseMapper::mapToWarehouse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/items/by-location/{warehouseLocationRequest}")
    @Operation(summary = "Retrieve all items by warehouse location",
            description = "Retrieves a list of items assigned to a warehouse location (Warsaw, Krakow, Rzeszow).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of items",
                    content = @Content(schema = @Schema(implementation = ItemResponse.class))),
            @ApiResponse(responseCode = "404", description = "No items found for the given location prefix",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden (Access denied)",
                    content = @Content)
    })
    public ResponseEntity<List<ItemResponse>> getItemsByLocationPrefix(
            @PathVariable WarehouseLocationRequest warehouseLocationRequest) {
        User user = getAuthenticatedUser();
        log.info("User {} requested all warehouses", user.getEmail());
        List<ItemEntity> items = warehouseService.getAllItemsByLocationCodePrefix(
                warehouseLocationRequest.getWarehouseLocation());

        if (items.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<ItemResponse> responses = items.stream()
                .map(itemResponseToItemMapper::mapToItem)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

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
    public ResponseEntity<WarehouseResponse> create(@RequestBody WarehouseRequest warehouseRequest) {
        User user = getAuthenticatedUser();
        log.info("User {} requested all warehouses", user.getEmail());
        WarehouseEntity warehouse = warehouseService.createWarehouse(warehouseRequest, user);
        WarehouseResponse warehouseResponse = warehouseResponseToWarehouseMapper.mapToWarehouse(warehouse);
        if (warehouseResponse == null) {
            throw new IllegalArgumentException("Mapper returned null for WarehouseResponse");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(warehouseResponse);
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
    public ResponseEntity<ItemResponse> assignSpaceType(@PathVariable Long itemId,
                                                        @RequestBody WarehouseSpaceTypeRequest warehouseSpaceTypeRequest) {
        User user = getAuthenticatedUser();
        log.info("User {} requested all warehouses", user.getEmail());
        ItemEntity item = warehouseService.assignWarehouseSpaceType(warehouseSpaceTypeRequest, itemId, user);
        ItemResponse response = itemResponseToItemMapper.mapToItem(item);
        return ResponseEntity.ok(response);

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
    public ResponseEntity<ItemResponse> assignWarehouseLocation(@PathVariable Long itemId,
                                                                @RequestBody WarehouseLocationRequest warehouseLocationRequest) {
        User user = getAuthenticatedUser();
        log.info("User {} requested all warehouses", user.getEmail());
        ItemEntity item = warehouseService.assignItemsToWarehouseLocation(warehouseLocationRequest, itemId, user);
        ItemResponse response = itemResponseToItemMapper.mapToItem(item);
        return ResponseEntity.ok(response);
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
    public ResponseEntity<ItemResponse> updateWarehouseLocation(@PathVariable Long itemId,
                                                                @RequestBody WarehouseLocationRequest warehouseLocationRequest) {
        User user = getAuthenticatedUser();
        log.info("User {} requested all warehouses", user.getEmail());
        ItemEntity item = warehouseService.updatedItemsToWarehouseLocation(warehouseLocationRequest, itemId, user);
        ItemResponse response = itemResponseToItemMapper.mapToItem(item);
        return ResponseEntity.ok(response);
    }
}
