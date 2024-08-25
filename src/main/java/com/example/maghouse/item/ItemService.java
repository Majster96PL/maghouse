package com.example.maghouse.item;

import com.example.maghouse.auth.mapper.ItemRequestToItemMapper;
import com.example.maghouse.auth.registration.user.User;
import com.example.maghouse.auth.registration.user.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Random;

@Service
@AllArgsConstructor
public class ItemService {

    private final UserRepository userRepository;
    private final ItemRequestToItemMapper itemRequestToItemMapper;
    private final Random random;

    public Item createItem(ItemRequest itemRequest) {
        String itemCode = itemRequest.getItemCode();

        if(itemCode == null || itemCode.isEmpty()){
            itemCode = generatedItemCode();
        }

        Optional<User> user = userRepository.findById(itemRequest.getUser().getId());
        return itemRequestToItemMapper.map(itemRequest);
    }

    public String generatedItemCode(){
        String firstPart = String.format("%02d", random.nextInt(100));
        String secendPart = String.format("%03d", random.nextInt(1000));
        String thirdPart = String.format("%04d", random.nextInt(10000));
        return firstPart + secendPart + thirdPart;
    }
}
