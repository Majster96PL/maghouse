package com.example.maghouse.auth.mapper;

import com.example.maghouse.item.Item;
import com.example.maghouse.item.ItemRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ItemRequestToItemMapper implements ItemMapper<ItemRequest, Item>{

    @Override
    public Item map(ItemRequest itemRequest) {
        return Item.builder()
                .name(itemRequest.getName())
                .itemCode(itemRequest.getItemCode())
                .quantity(itemRequest.getQuantity())
                .build();
    }
}
