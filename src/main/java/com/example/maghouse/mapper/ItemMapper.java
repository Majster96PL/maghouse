package com.example.maghouse.mapper;

public interface ItemMapper <From, To, Entity>{
    To mapToItem(From itemRequest, String itemCode, String locationCode, long id);
    Entity mapToResponse(To itemResponse);
    To mapToItemResponse(Entity itemEntity);

}
