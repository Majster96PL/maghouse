package com.example.maghouse.auth.controller;

import com.example.maghouse.auth.registration.role.Role;
import com.example.maghouse.auth.registration.user.User;
import com.example.maghouse.item.ItemEntity;
import com.example.maghouse.item.ItemRequest;
import com.example.maghouse.item.ItemResponse;
import com.example.maghouse.item.ItemService;
import com.example.maghouse.mapper.ItemResponseToItemMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.*;

public class ItemControllerTest {

    @Mock
    private ItemResponseToItemMapper itemResponseToItemMapper;

    @Mock
    private ItemService itemService;

    @InjectMocks
    private ItemController itemController;

    private ItemEntity item;
    private ItemRequest itemRequest;
    private User user;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);

        user = User.builder()
                .firstname("John")
                .lastname("Kovalsky")
                .email("john.kovalsky@maghouse.com")
                .password("testPassword")
                .role(Role.USER)
                .build();

        itemRequest = new ItemRequest("Test_Item", 100);

        item = ItemEntity.builder()
                .id(1L)
                .name("Test_Item")
                .itemCode("ITEM_CODE")
                .locationCode("Location_Code")
                .quantity(100)
                .user(user)
                .warehouseEntity(null)
                .deliveries(null)
                .build();

    }

    @Test
    void shouldCreateItemSuccessfully(){
        when(itemService.createItem(itemRequest, user)).thenReturn(item);
        when(itemResponseToItemMapper.mapToItem(item)).thenReturn(new ItemResponse());

        ResponseEntity<ItemResponse> response = itemController.create(itemRequest);

        assertNotNull(response);
        assertEquals(CREATED, response.getStatusCode());
        assertEquals(new ItemResponse(), response.getBody());
    }

    @Test
    void shouldThrowExceptionWhenCreatingItemWithNullRequest(){
        when(itemService.createItem(null, user)).thenThrow(new IllegalArgumentException("ItemRequest can't be empty!"));

        assertThrows(IllegalArgumentException.class, () -> itemController.create(null));
    }

    @Test
    void shouldUpdatedItemQuantity(){
        ItemRequest updatedItemRequest = new ItemRequest("Test_Item", 140);

        when(itemService.updateItemQuantity(item.getId(),updatedItemRequest, user)).thenReturn(item);
        when(itemResponseToItemMapper.mapToItem(item)).thenReturn(new ItemResponse());

        ResponseEntity<ItemResponse> response = itemController.updateItemQuantity(item.getId(), updatedItemRequest);


        assertNotNull(response);
        assertEquals(OK , response.getStatusCode());
        assertEquals(new ItemResponse(), response.getBody());

    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentItem(){
        ItemRequest wrongItemRequest = new ItemRequest("Item1", 35);

        when(itemService.updateItemQuantity(1L, wrongItemRequest, user)).thenThrow(new ResponseStatusException(NOT_FOUND, "Item not found!"));

        assertThrows(ResponseStatusException.class, () -> itemController.updateItemQuantity(1L, wrongItemRequest));

    }

    @Test
    void shouldDeleteItemSuccessfully(){
        doNothing().when(itemService).deleteItem(1L, user);

        ResponseEntity<Void> response = itemController.deleteItem(1L);

        assertEquals(NO_CONTENT, response.getStatusCode());
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentItem(){
        doThrow(new ResponseStatusException(NOT_FOUND, "Item not found")).when(itemService).deleteItem(1L, user);

        assertThrows(ResponseStatusException.class, () -> itemController.deleteItem(1L));
    }

}
