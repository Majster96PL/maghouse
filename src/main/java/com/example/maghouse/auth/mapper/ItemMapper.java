package com.example.maghouse.auth.mapper;


public interface ItemMapper <From, To>{
    To map(From itemRequest);
}
