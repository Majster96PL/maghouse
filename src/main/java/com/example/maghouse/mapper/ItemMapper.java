package com.example.maghouse.mapper;


public interface ItemMapper <From, To>{
    To map(From itemRequest);
    void mapIteRequestToItemResponse(From itemRequest, To itemResponse);
}
