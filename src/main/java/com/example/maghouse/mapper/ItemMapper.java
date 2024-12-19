package com.example.maghouse.mapper;


public interface ItemMapper <From, To>{
    To mapToItem(From itemRequest);
    void mapIteRequestToItemResponse(From itemRequest, To itemResponse);
}
