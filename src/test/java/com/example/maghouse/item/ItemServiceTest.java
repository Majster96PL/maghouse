package com.example.maghouse.item;

import com.example.maghouse.auth.registration.role.Role;
import com.example.maghouse.auth.registration.user.User;
import com.example.maghouse.auth.registration.user.UserRepository;
import com.example.maghouse.mapper.ItemResponseToItemMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ItemServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ItemResponseToItemMapper itemResponseToItemMapper;

    @Mock
    private ItemCodeGenerator itemCodeGenerator;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private ItemService itemService;

    private User user;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.isAuthenticated()).thenReturn(true);
        lenient().when(authentication.getPrincipal()).thenReturn(userDetails);
        user = User.builder()
                .id(1L)
                .firstname("John")
                .lastname("Doe")
                .email("test@example.com")
                .password("password")
                .role(Role.USER)
                .build();

        lenient().when(userDetails.getUsername()).thenReturn(user.getEmail());
        lenient().when(userRepository.findUserByEmail(user.getEmail())).thenReturn(Optional.of(user));
    }

    @Test
    void shouldCreateItemSuccessfully() {
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setName("Test Item");
        itemRequest.setQuantity(10);

        user.setEmail("test@example.com");

        when(userDetails.getUsername()).thenReturn("test@example.com");
        when(userRepository.findUserByEmail(anyString())).thenReturn(Optional.of(user));
        when(itemCodeGenerator.generateItemCode()).thenReturn("ITEM123");
        ItemResponse itemResponse = new ItemResponse();
        itemResponse.setName("Test Item");
        itemResponse.setQuantity(10);
        itemResponse.setUserId(user.getId());
        itemResponse.setItemCode("ITEM123");

        ItemEntity item = new ItemEntity();
        when(itemResponseToItemMapper.mapToItem(any(ItemRequest.class), "ITEM123","locationCode" , user.getId())).thenReturn(new ItemResponse());
        when(itemRepository.save(any(ItemEntity.class))).thenReturn(item);

        ItemEntity createdItem = itemService.createItem(itemRequest);

        assertNotNull(createdItem);
        verify(userRepository).findUserByEmail("test@example.com");
        verify(itemCodeGenerator).generateItemCode();
        verify(itemResponseToItemMapper).mapToItem(any(ItemRequest.class),"ITEM123","locationCode" , user.getId());
        verify(itemRepository).save(any(ItemEntity.class));
    }

    @Test
    void shouldThrowSecurityExceptionWhenUserNotAuthenticatedOnCreate() {
        when(authentication.isAuthenticated()).thenReturn(false);

        ItemRequest itemRequest = new ItemRequest();

        assertThrows(SecurityException.class, () -> itemService.createItem(itemRequest));
    }

    @Test
    void shouldUpdateItemQuantitySuccessfully() {
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setQuantity(20);

        user.setEmail("test@example.com");

        ItemEntity item = new ItemEntity();
        item.setId(1L);
        item.setQuantity(10);

        when(userDetails.getUsername()).thenReturn("test@example.com");
        when(userRepository.findUserByEmail(anyString())).thenReturn(Optional.of(user));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(itemRepository.save(any(ItemEntity.class))).thenReturn(item);

        ItemEntity updatedItem = itemService.updateItemQuantity(1L, itemRequest);

        assertNotNull(updatedItem);
        assertEquals(20, updatedItem.getQuantity());
        verify(itemRepository).findById(1L);
        verify(itemRepository).save(any(ItemEntity.class));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenItemNotFoundOnUpdate() {
        ItemRequest itemRequest = new ItemRequest();
        when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> itemService.updateItemQuantity(1L, itemRequest));
    }

    @Test
    void shouldDeleteItemSuccessfully() {
        user.setEmail("test@example.com");

        when(userDetails.getUsername()).thenReturn("test@example.com");
        when(userRepository.findUserByEmail(anyString())).thenReturn(Optional.of(user));

        itemService.deleteItem(1L);

        verify(itemRepository).deleteById(1L);
    }

    @Test
    void shouldThrowSecurityExceptionWhenUserNotAuthenticatedOnDelete() {
       when(authentication.isAuthenticated()).thenReturn(false);

       assertThrows(SecurityException.class, () -> itemService.deleteItem(1L));
    }
}