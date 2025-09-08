package com.example.maghouse.auth.controller;

import com.example.maghouse.item.ItemEntity;
import com.example.maghouse.item.ItemRequest;
import com.example.maghouse.item.ItemResponse;
import com.example.maghouse.item.ItemService;
import com.example.maghouse.mapper.ItemResponseToItemMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping(path = "/items/")
@RestController
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;
    private final ItemResponseToItemMapper itemResponseToItemMapper;

    @PostMapping
    public ResponseEntity<ItemResponse> create(@RequestBody ItemRequest itemRequest) {
        ItemEntity item = itemService.createItem(itemRequest);
        ItemResponse itemResponse = itemResponseToItemMapper.mapToItem(item);
        return ResponseEntity.status(HttpStatus.CREATED).body(itemResponse);
    }
    @PutMapping("/{itemId}")
    public ResponseEntity<ItemResponse> updateItemQuantity(@PathVariable Long itemId, @RequestBody ItemRequest itemRequest) {
        ItemEntity updatedItem = itemService.updateItemQuantity(itemId, itemRequest);
        ItemResponse updatedItemResponse = itemResponseToItemMapper.mapToItemResponse(updatedItem);
        return ResponseEntity.ok(updatedItemResponse);
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long itemId) {
        itemService.deleteItem(itemId);
        return ResponseEntity.noContent().build();
    }
}
