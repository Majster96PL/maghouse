package com.example.maghouse.item;

import com.example.maghouse.mapper.ItemRequestToItemMapper;
import com.example.maghouse.auth.registration.user.User;
import com.example.maghouse.auth.registration.user.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@AllArgsConstructor
public class ItemService {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ItemRequestToItemMapper itemRequestToItemMapper;
    private final Random random;


    public Item createItem(ItemRequest itemRequest, ItemResponse itemResponse) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("User is not authenticated");
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findUserByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User with email not found"));
        itemResponse.setName(itemRequest.getName());
        String itemCode = itemResponse.getItemCode();

        if(itemCode == null || itemCode.isEmpty()){
            itemCode = generatedItemCode();
        }
        itemResponse.setItemCode(itemCode);
        itemResponse.setQuantity(itemRequest.getQuantity());
        itemResponse.setUser(user);
        Item item = itemRequestToItemMapper.mapToItem(itemResponse);
        return itemRepository.save(item) ;
    }

    public Item updateItemQuantity(Long itemId, ItemRequest itemRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("User is not authenticated");
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findUserByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User with email not found"));

        Item item = itemRepository.findById(itemId)
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
    public String generatedItemCode(){
        String firstPart = String.format("%02d", random.nextInt(100));
        String secondPart = String.format("%03d", random.nextInt(1000));
        String thirdPart = String.format("%04d", random.nextInt(10000));
        String sign = "-";
        return firstPart + sign + secondPart + sign + thirdPart;
    }
}
