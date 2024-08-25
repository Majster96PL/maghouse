package com.example.maghouse.item;

import com.example.maghouse.auth.mapper.ItemRequestToItemMapper;
import com.example.maghouse.auth.registration.user.User;
import com.example.maghouse.auth.registration.user.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@AllArgsConstructor
public class ItemService {

    private final UserRepository userRepository;
    private final ItemRequestToItemMapper itemRequestToItemMapper;
    private final Random random;

    public Item createItem(ItemRequest itemRequest) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findUserByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User with email not found"));

        String itemCode = itemRequest.getItemCode();

        if(itemCode == null || itemCode.isEmpty()){
            itemCode = generatedItemCode();
        }
        itemRequest.setUser(user);
        return itemRequestToItemMapper.map(itemRequest);
    }

    public String generatedItemCode(){
        String firstPart = String.format("%02d", random.nextInt(100));
        String secondPart = String.format("%03d", random.nextInt(1000));
        String thirdPart = String.format("%04d", random.nextInt(10000));
        return firstPart + secondPart + thirdPart;
    }
}
