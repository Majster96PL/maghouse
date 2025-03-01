package com.example.maghouse.auth.controller;

import com.example.maghouse.auth.registration.role.Role;
import com.example.maghouse.auth.registration.user.User;
import com.example.maghouse.item.Item;
import com.example.maghouse.item.ItemRequest;
import com.example.maghouse.item.ItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.*;

public class ItemControllerTest {

    @Mock
    private ItemService itemService;

    @InjectMocks
    private ItemController itemController;

    private Item item;
    private ItemRequest itemRequest;
    private User user;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);

        itemRequest = new ItemRequest("Test_Item", 100);

        item = Item.builder()
                .id(1L)
                .name("Test_Item")
                .itemCode("ITEM_CODE")
                .locationCode("Location_Code")
                .user(user)
                .warehouse(null)
                .deliveries(null)
                .build();

        user = User.builder()
                .firstname("John")
                .lastname("Kovalsky")
                .email("john.kovalsky@maghouse.com")
                .password("testPassword")
                .role(Role.USER)
                .build();

    }

    @Test
    void shouldCreateItemSuccessfully(){
        when(itemService.createItem(itemRequest)).thenReturn(item);

        ResponseEntity<Item> response = itemController.create(itemRequest);

        assertEquals(CREATED, response.getStatusCode());
        assertEquals(item, response.getBody());
    }

    @Test
    void shouldThrowExceptionWhenCreatingItemWithNullRequest(){
        when(itemService.createItem(null)).thenThrow(new IllegalArgumentException("ItemRequest can't be empty!"));

        assertThrows(IllegalArgumentException.class, () -> itemController.create(null));
    }

    @Test
    void shouldUpdatedItemQuantity(){
        ItemRequest updatedItemRequest = new ItemRequest("Test_Item", 140);

        when(itemService.updateItemQuantity(item.getId(),updatedItemRequest)).thenReturn(item);

        ResponseEntity<Item> response = itemController.updateItemQuantity(item.getId(), updatedItemRequest);

        assertEquals(OK , response.getStatusCode());
        assertEquals(item.getName(), Objects.requireNonNull(response.getBody()).getName());
        assertEquals(item.getQuantity(), response.getBody().getQuantity());

    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentItem(){
        ItemRequest wrongItemRequest = new ItemRequest("Item1", 35);

        when(itemService.updateItemQuantity(1L, wrongItemRequest)).thenThrow(new ResponseStatusException(NOT_FOUND, "Item not found!"));

        assertThrows(ResponseStatusException.class, () -> itemController.updateItemQuantity(1L, wrongItemRequest));

    }

    @Test
    void shouldDeleteItemSuccessfully(){
        doNothing().when(itemService).deleteItem(1L);

        ResponseEntity<Void> response = itemController.deleteItem(1L);

        assertEquals(NO_CONTENT, response.getStatusCode());
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentItem(){
        doThrow(new ResponseStatusException(NOT_FOUND, "Item not found")).when(itemService).deleteItem(1L);

        assertThrows(ResponseStatusException.class, () -> itemController.deleteItem(1L));
    }

}
