package com.example.maghouse.item;

import com.example.maghouse.auth.registration.role.Role;
import com.example.maghouse.auth.registration.user.User;
import com.example.maghouse.mapper.ItemResponseToItemMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ItemResponseToItemMapper itemResponseToItemMapper;

    @Mock
    private ItemCodeGenerator itemCodeGenerator;

    @InjectMocks
    private ItemService itemService;

    private ItemEntity item;
    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .firstname("John")
                .lastname("Doe")
                .email("test@example.com")
                .password("password")
                .role(Role.USER)
                .build();

        item = ItemEntity.builder()
                .name("ItemName")
                .quantity(123)
                .build();
    }

    @Test
    void shouldCreateItemSuccessfully() {
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setName("Test Item");
        itemRequest.setQuantity(10);

        when(itemCodeGenerator.generateItemCode()).thenReturn("ITEM123");

        ItemResponse itemResponse = new ItemResponse();
        itemResponse.setName(itemRequest.getName());
        itemResponse.setQuantity(itemRequest.getQuantity());
        itemResponse.setUserId(user.getId());
        itemResponse.setItemCode("ITEM123");

        when(itemResponseToItemMapper.mapToItemResponseFromRequest(
                eq(itemRequest), eq("ITEM123"), isNull(), eq(user.getId()))
        ).thenReturn(itemResponse);

        ItemEntity mappedItem = ItemEntity.builder()
                .name("ItemName")
                .quantity(123)
                .build();
        when(itemResponseToItemMapper.mapToEntityFromResponse(itemResponse)).thenReturn(mappedItem);
        when(itemRepository.save(mappedItem)).thenReturn(mappedItem);

        ItemEntity createdItem = itemService.createItem(itemRequest, user);

        assertNotNull(createdItem);
        assertEquals("ItemName", createdItem.getName());
        assertEquals(123, createdItem.getQuantity());

        verify(itemCodeGenerator).generateItemCode();
        verify(itemResponseToItemMapper).mapToItemResponseFromRequest(
                eq(itemRequest), eq("ITEM123"), isNull(), eq(user.getId()));
        verify(itemResponseToItemMapper).mapToEntityFromResponse(itemResponse);
        verify(itemRepository).save(mappedItem);
    }

    @Test
    void shouldThrowSecurityExceptionWhenUserNotAuthenticatedOnCreate() {
        User nullUser = null;
        ItemRequest itemRequest = new ItemRequest();

        assertThrows(IllegalArgumentException.class, () -> itemService.createItem(itemRequest, nullUser));
    }

    @Test
    void shouldUpdateItemQuantitySuccessfully() {
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setQuantity(20);

        ItemEntity item = new ItemEntity();
        item.setId(1L);
        item.setQuantity(10);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepository.save(item)).thenReturn(item);

        ItemEntity updatedItem = itemService.updateItemQuantity(1L, itemRequest, user);

        assertNotNull(updatedItem);
        assertEquals(20, updatedItem.getQuantity());
        verify(itemRepository).findById(1L);
        verify(itemRepository).save(item);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenItemNotFoundOnUpdate() {
        ItemRequest itemRequest = new ItemRequest();
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> itemService.updateItemQuantity(1L, itemRequest, user));
    }

    @Test
    void shouldDeleteItemSuccessfully() {
        itemService.deleteItem(1L, user);

        verify(itemRepository).deleteById(1L);
    }

    @Test
    void shouldThrowSecurityExceptionWhenUserNotAuthenticatedOnDelete() {
        User nullUser = null;

        assertDoesNotThrow(() -> itemService.deleteItem(1L, nullUser));
        verify(itemRepository).deleteById(1L);
    }
}