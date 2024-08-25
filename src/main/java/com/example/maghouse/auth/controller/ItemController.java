package com.example.maghouse.auth.controller;

import com.example.maghouse.auth.mapper.ItemRequestToItemMapper;
import com.example.maghouse.item.Item;
import com.example.maghouse.item.ItemRequest;
import com.example.maghouse.item.ItemResponse;
import com.example.maghouse.item.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping(path = "/auth/item/")
@RestController
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @PostMapping("/create")
    //@PreAuthorize("hasAuthority('USER')")
    public Item create(@RequestBody ItemRequest itemRequest, ItemResponse itemResponse) {
        return itemService.createItem(itemRequest, itemResponse);
    }

    @PutMapping("/update/{itemId}")
    public ResponseEntity<Item> updateItemQuantity(@PathVariable Long itemId, @RequestBody ItemRequest itemRequest) {
        Item updatedItem = itemService.updateItemQuantity(itemId, itemRequest);
        return ResponseEntity.ok(updatedItem);
    }

    @DeleteMapping("/delete/{itemId}")
    public void deleteItem(@PathVariable Long itemId) {
         itemService.deleteItem(itemId);
    }
}
