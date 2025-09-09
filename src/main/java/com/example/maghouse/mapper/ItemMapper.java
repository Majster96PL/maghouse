package com.example.maghouse.mapper;

public interface ItemMapper <From, To, Entity>{
    To mapToItemResponseFromRequest(From itemRequest, String itemCode, String locationCode, long id);
    Entity mapToEntityFromResponse(To itemResponse);
    To mapToItem(Entity itemEntity);

}
