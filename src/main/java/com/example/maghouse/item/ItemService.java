package com.example.maghouse.item;

import com.example.maghouse.mapper.ItemResponseToItemMapper;
import com.example.maghouse.auth.registration.user.User;
import com.example.maghouse.auth.registration.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@AllArgsConstructor
public class ItemService {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ItemResponseToItemMapper itemResponseToItemMapper;
    private final ItemCodeGenerator itemCodeGenerator;
    private static final ItemResponse itemResponse = new ItemResponse();

    public List<ItemEntity> getAllItems() {
        return itemRepository.findAll();
    }

    public ItemEntity getItemByItemCode(String itemCode ) {
        return itemRepository.findByItemCode(itemCode).
                orElseThrow(() -> new NoSuchElementException("Item not found!"));
    }

    @Transactional
    public ItemEntity createItem(ItemRequest itemRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("User is not authenticated");
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findUserByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User with email not found"));
        String code = itemCodeGenerator.generateItemCode();

        ItemResponse itemResponse = itemResponseToItemMapper.mapToItemResponseFromRequest(itemRequest, code, null, user.getId() );
        ItemEntity item = itemResponseToItemMapper.mapToEntityFromResponse(itemResponse);
        item.setUser(user);
        return itemRepository.save(item);
    }

    public ItemEntity updateItemQuantity(Long itemId, ItemRequest itemRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("User is not authenticated");
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findUserByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User with email not found"));

        ItemEntity item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found"));

        item.setQuantity(itemRequest.getQuantity());
        item.setUser(user);
        return itemRepository.save(item);
    }

    public void deleteItem(Long itemId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("User is not authenticated");
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        userRepository.findUserByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User with email not found"));

        itemRepository.deleteById(itemId);
    }
}
