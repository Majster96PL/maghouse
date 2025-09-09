package com.example.maghouse.mapper;

import com.example.maghouse.item.ItemEntity;
import com.example.maghouse.item.ItemRequest;
import com.example.maghouse.item.ItemResponse;
import org.springframework.stereotype.Component;

@Component
public class ItemResponseToItemMapper implements ItemMapper<ItemRequest, ItemResponse, ItemEntity>{

    @Override
    public ItemResponse mapToItemResponseFromRequest(ItemRequest itemRequest, String itemCode, String locationCode, long id) {
        return ItemResponse.builder()
                .name(itemRequest.getName())
                .itemCode(itemCode)
                .quantity(itemRequest.getQuantity())
                .locationCode(locationCode)
                .userId(id)
                .build();
    }


    @Override
    public ItemEntity mapToEntityFromResponse(ItemResponse itemResponse) {
        return ItemEntity.builder()
                .name(itemResponse.getName())
                .itemCode(itemResponse.getItemCode())
                .quantity(itemResponse.getQuantity())
                .user(null)
                .warehouse(null)
                .deliveries(null)
                .build();
    }


    @Override
    public ItemResponse mapToItem(ItemEntity itemEntity) {
        return ItemResponse.builder()
                .name(itemEntity.getName())
                .itemCode(itemEntity.getItemCode())
                .quantity(itemEntity.getQuantity())
                .locationCode(itemEntity.getLocationCode())
                .userId(itemEntity.getUser().getId())
                .build();
    }
}
