package com.example.maghouse.mapper;

import com.example.maghouse.item.Item;
import com.example.maghouse.item.ItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ItemRequestToItemMapper implements ItemMapper<ItemResponse, Item>{


    @Override
    public Item mapToItem(ItemResponse itemResponse) {
        return Item.builder()
                .name(itemResponse.getName())
                .itemCode(itemResponse.getItemCode())
                .quantity(itemResponse.getQuantity())
                .user(itemResponse.getUser())
                .build();
    }

    @Override
    public void mapIteRequestToItemResponse(ItemResponse itemRequest, Item itemResponse) {
            itemResponse.setName(itemRequest.getName());
            itemResponse.setQuantity(itemRequest.getQuantity());

    }
}
