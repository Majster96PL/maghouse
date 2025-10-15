package com.example.maghouse.auth.controller;

import com.example.maghouse.auth.registration.user.User;
import com.example.maghouse.auth.registration.user.UserService;
import com.example.maghouse.item.ItemEntity;
import com.example.maghouse.item.ItemRequest;
import com.example.maghouse.item.ItemResponse;
import com.example.maghouse.item.ItemService;
import com.example.maghouse.mapper.ItemResponseToItemMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequestMapping(path = "/items/")
@RestController
@Tag(name = "Item Management", description = "Endpoints for managing items and stock quantities in the warehouse.")
@SecurityRequirement(name = "bearerAuth")
public class ItemController extends BaseController {

    private final ItemService itemService;
    private final ItemResponseToItemMapper itemResponseToItemMapper;

    public ItemController(UserService userService,
                          ItemService itemService,
                          ItemResponseToItemMapper itemResponseToItemMapper) {
        super(userService);
        this.itemService = itemService;
        this.itemResponseToItemMapper = itemResponseToItemMapper;
    }

    @GetMapping
    @Operation(summary = "Retrieve a list of all items",
            description = "Returns a list of all item types currently in the warehouse inventory.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved items",
                    content = @Content(schema = @Schema(implementation = ItemResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied",
                    content = @Content)
    })
    public ResponseEntity<List<ItemResponse>> getAllItems() {
        User user = getAuthenticatedUser();
        List<ItemEntity> items = itemService.getAllItems();
        List<ItemResponse> itemResponses = items.stream()
                .map(itemResponseToItemMapper::mapToItem)
                .collect(Collectors.toList());

        return ResponseEntity.ok(itemResponses);
    }

    @GetMapping("/{itemCode}")
    @Operation(summary = "Retrieve item details by item code",
            description = "Returns detailed data for a specific item based on its unique item code.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved item details",
                    content = @Content(schema = @Schema(implementation = ItemResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Item with the given ID was not found",
                    content = @Content(schema = @Schema(implementation = Map.class)))
    })
    public ResponseEntity<ItemResponse> getItemByItemCode(@PathVariable("itemCode") String itemCode){
        User user = getAuthenticatedUser();
        var item = itemService.getItemByItemCode(itemCode);
        var response = itemResponseToItemMapper.mapToItem(item);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @Operation(summary = "Create a new item",
            description = "Adds a new unique item type to the warehouse inventory.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Item successfully created",
                    content = @Content(schema = @Schema(implementation = ItemResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request or item already exists",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "403", description = "Access denied",
                    content = @Content)
    })
    public ResponseEntity<ItemResponse> create(@RequestBody ItemRequest itemRequest) {
        User user = getAuthenticatedUser();
        ItemEntity item = itemService.createItem(itemRequest, user );
        ItemResponse itemResponse = itemResponseToItemMapper.mapToItem(item);
        return ResponseEntity.status(HttpStatus.CREATED).body(itemResponse);
    }

    @PutMapping("/{itemId}")
    @Operation(summary = "Update item quantity",
            description = "Modifies the stock quantity for a specific item.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Item quantity successfully updated",
                    content = @Content(schema = @Schema(implementation = ItemResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid quantity value or request",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "403", description = "Access denied",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Item with the given ID was not found",
                    content = @Content(schema = @Schema(implementation = Map.class)))
    })
    public ResponseEntity<ItemResponse> updateItemQuantity(@PathVariable Long itemId, @RequestBody ItemRequest itemRequest) {
        User user = getAuthenticatedUser();
        ItemEntity updatedItem = itemService.updateItemQuantity(itemId, itemRequest, user);
        ItemResponse updatedItemResponse = itemResponseToItemMapper.mapToItem(updatedItem);
        return ResponseEntity.ok(updatedItemResponse);
    }

    @DeleteMapping("/{itemId}")
    @Operation(summary = "Delete item",
            description = "Removes an item permanently from the inventory.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Item successfully deleted (No Content)",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Access denied",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Item with the given ID was not found",
                    content = @Content(schema = @Schema(implementation = Map.class)))
    })
    public ResponseEntity<Void> deleteItem(@PathVariable Long itemId) {
        User user = getAuthenticatedUser();
        itemService.deleteItem(itemId, user);
        return ResponseEntity.noContent().build();
    }
}
