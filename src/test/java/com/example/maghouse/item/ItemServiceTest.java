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

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.isAuthenticated()).thenReturn(true);
        lenient().when(authentication.getPrincipal()).thenReturn(userDetails);
        User user = User.builder()
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
        // Arrange
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setName("Test Item");
        itemRequest.setQuantity(10);

        User user = new User();
        user.setEmail("test@example.com");

        when(userDetails.getUsername()).thenReturn("test@example.com");
        when(userRepository.findUserByEmail(anyString())).thenReturn(Optional.of(user));
        when(itemCodeGenerator.generateItemCode()).thenReturn("ITEM123");
        ItemResponse itemResponse = new ItemResponse();
        itemResponse.setName("Test Item");
        itemResponse.setQuantity(10);
        itemResponse.setUser(user);
        itemResponse.setItemCode("ITEM123");

        Item item = new Item();
        when(itemResponseToItemMapper.mapToItem(any(ItemResponse.class))).thenReturn(item);
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        // Act
        Item createdItem = itemService.createItem(itemRequest);

        // Assert
        assertNotNull(createdItem);
        verify(userRepository).findUserByEmail("test@example.com");
        verify(itemCodeGenerator).generateItemCode();
        verify(itemResponseToItemMapper).mapToItem(any(ItemResponse.class));
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void shouldThrowSecurityExceptionWhenUserNotAuthenticatedOnCreate() {
        // Arrange
        when(authentication.isAuthenticated()).thenReturn(false);

        ItemRequest itemRequest = new ItemRequest();

        // Act & Assert
        assertThrows(SecurityException.class, () -> itemService.createItem(itemRequest));
    }

    @Test
    void shouldUpdateItemQuantitySuccessfully() {
        // Arrange
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setQuantity(20);

        User user = new User();
        user.setEmail("test@example.com");

        Item item = new Item();
        item.setId(1L);
        item.setQuantity(10);

        when(userDetails.getUsername()).thenReturn("test@example.com");
        when(userRepository.findUserByEmail(anyString())).thenReturn(Optional.of(user));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        // Act
        Item updatedItem = itemService.updateItemQuantity(1L, itemRequest);

        // Assert
        assertNotNull(updatedItem);
        assertEquals(20, updatedItem.getQuantity());
        verify(itemRepository).findById(1L);
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenItemNotFoundOnUpdate() {
        // Arrange
        ItemRequest itemRequest = new ItemRequest();
        when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> itemService.updateItemQuantity(1L, itemRequest));
    }

    @Test
    void shouldDeleteItemSuccessfully() {
        // Arrange
        User user = new User();
        user.setEmail("test@example.com");

        when(userDetails.getUsername()).thenReturn("test@example.com");
        when(userRepository.findUserByEmail(anyString())).thenReturn(Optional.of(user));

        // Act
        itemService.deleteItem(1L);

        // Assert
        verify(itemRepository).deleteById(1L);
    }

    @Test
    void shouldThrowSecurityExceptionWhenUserNotAuthenticatedOnDelete() {
       when(authentication.isAuthenticated()).thenReturn(false);

        assertThrows(SecurityException.class, () -> itemService.deleteItem(1L));
    }
}