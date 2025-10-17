package com.example.maghouse.item;

import com.example.maghouse.mapper.ItemResponseToItemMapper;
import com.example.maghouse.auth.registration.user.User;
import com.example.maghouse.auth.registration.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@AllArgsConstructor
@Slf4j
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
    public ItemEntity createItem(ItemRequest itemRequest, User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        String code = itemCodeGenerator.generateItemCode();

        ItemResponse itemResponse = itemResponseToItemMapper.mapToItemResponseFromRequest(itemRequest, code, null, user.getId() );
        ItemEntity item = itemResponseToItemMapper.mapToEntityFromResponse(itemResponse);
        item.setUser(user);
        return itemRepository.save(item);
    }

    public ItemEntity updateItemQuantity(Long itemId, ItemRequest itemRequest, User user) {
        ItemEntity item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found"));

        item.setQuantity(itemRequest.getQuantity());
        item.setUser(user);
        return itemRepository.save(item);
    }

    public void deleteItem(Long itemId, User user) {
        itemRepository.deleteById(itemId);
    }
}
