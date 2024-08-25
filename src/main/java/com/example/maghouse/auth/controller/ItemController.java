package com.example.maghouse.auth.controller;

import com.example.maghouse.auth.mapper.ItemRequestToItemMapper;
import com.example.maghouse.item.Item;
import com.example.maghouse.item.ItemRequest;
import com.example.maghouse.item.ItemResponse;
import com.example.maghouse.item.ItemService;
import lombok.RequiredArgsConstructor;
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
}
