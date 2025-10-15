package com.example.maghouse.auth.controller;

import com.example.maghouse.auth.registration.role.Role;
import com.example.maghouse.auth.registration.user.User;
import com.example.maghouse.item.ItemEntity;
import com.example.maghouse.item.ItemRequest;
import com.example.maghouse.item.ItemResponse;
import com.example.maghouse.item.ItemService;
import com.example.maghouse.mapper.ItemResponseToItemMapper;
import com.example.maghouse.security.AuthenticationHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.*;

@ExtendWith(MockitoExtension.class)
class ItemControllerTest {

    @Mock
    private ItemResponseToItemMapper itemResponseToItemMapper;

    @Mock
    private ItemService itemService;

    @Mock
    private AuthenticationHelper authenticationHelper;

    @InjectMocks
    private ItemController itemController;

    private ItemEntity item;
    private ItemRequest itemRequest;
    private User user;
    private Authentication authentication;

    @BeforeEach
    void setUp(){
        user = User.builder()
                .firstname("John")
                .lastname("Kovalsky")
                .email("john.kovalsky@maghouse.com")
                .password("testPassword")
                .role(Role.USER)
                .build();

        authentication = mock(Authentication.class);
        lenient().when(authentication.getName()).thenReturn(user.getEmail());
        lenient().when(authentication.isAuthenticated()).thenReturn(true);
        lenient().when(authenticationHelper.getAuthenticatedUser(authentication)).thenReturn(user);

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

        ResponseEntity<ItemResponse> response = itemController.create(itemRequest, authentication);

        assertNotNull(response);
        assertEquals(CREATED, response.getStatusCode());
        assertEquals(new ItemResponse(), response.getBody());
        verify(authenticationHelper).getAuthenticatedUser(authentication);
        verify(itemService).createItem(itemRequest, user);
    }

    @Test
    void shouldThrowExceptionWhenCreatingItemWithNullRequest(){
        when(authenticationHelper.getAuthenticatedUser(authentication)).thenReturn(user);
        when(itemService.createItem(null, user)).thenThrow(new IllegalArgumentException("ItemRequest can't be empty!"));

        assertThrows(IllegalArgumentException.class, () -> itemController.create(null, authentication));
        verify(authenticationHelper).getAuthenticatedUser(authentication);
    }

    @Test
    void shouldUpdatedItemQuantity(){
        ItemRequest updatedItemRequest = new ItemRequest("Test_Item", 140);

        when(authenticationHelper.getAuthenticatedUser(authentication)).thenReturn(user);
        when(itemService.updateItemQuantity(item.getId(), updatedItemRequest, user)).thenReturn(item);
        when(itemResponseToItemMapper.mapToItem(item)).thenReturn(new ItemResponse());

        ResponseEntity<ItemResponse> response = itemController.updateItemQuantity(item.getId(), updatedItemRequest, authentication);

        assertNotNull(response);
        assertEquals(OK , response.getStatusCode());
        assertEquals(new ItemResponse(), response.getBody());
        verify(authenticationHelper).getAuthenticatedUser(authentication);
        verify(itemService).updateItemQuantity(item.getId(), updatedItemRequest, user);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentItem(){
        ItemRequest wrongItemRequest = new ItemRequest("Item1", 35);

        when(authenticationHelper.getAuthenticatedUser(authentication)).thenReturn(user);
        when(itemService.updateItemQuantity(1L, wrongItemRequest, user)).thenThrow(new ResponseStatusException(NOT_FOUND, "Item not found!"));

        assertThrows(ResponseStatusException.class, () -> itemController.updateItemQuantity(1L, wrongItemRequest, authentication));
        verify(authenticationHelper).getAuthenticatedUser(authentication);
    }

    @Test
    void shouldDeleteItemSuccessfully(){
        when(authenticationHelper.getAuthenticatedUser(authentication)).thenReturn(user);
        doNothing().when(itemService).deleteItem(1L, user);

        ResponseEntity<Void> response = itemController.deleteItem(1L, authentication);

        assertEquals(NO_CONTENT, response.getStatusCode());
        verify(authenticationHelper).getAuthenticatedUser(authentication);
        verify(itemService).deleteItem(1L, user);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentItem(){
        when(authenticationHelper.getAuthenticatedUser(authentication)).thenReturn(user);
        doThrow(new ResponseStatusException(NOT_FOUND, "Item not found")).when(itemService).deleteItem(1L, user);

        assertThrows(ResponseStatusException.class, () -> itemController.deleteItem(1L, authentication));
        verify(authenticationHelper).getAuthenticatedUser(authentication);
    }
}
