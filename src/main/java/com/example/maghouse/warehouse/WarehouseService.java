package com.example.maghouse.warehouse;

import com.example.maghouse.auth.registration.user.UserRepository;
import com.example.maghouse.item.Item;
import com.example.maghouse.item.ItemRepository;
import com.example.maghouse.mapper.WarehouseRequestToWarehouseMapper;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class WarehouseService {

    private final WarehouseRequestToWarehouseMapper warehouseRequestToWarehouseMapper;
    private final UserRepository userRepository;
    private final WarehouseRepository warehouseRepository;
    private final ItemRepository itemRepository;


    public Warehouse createWarehouse(WarehouseRequest warehouseRequest, WarehouseResponse warehouseResponse) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()){
            throw new SecurityException("User is not authenticated!");
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        var user = userRepository.findUserByEmail(userDetails.getUsername())
                .orElseThrow( () -> new IllegalArgumentException("User with email not found!"));
        return null;
    }


    private List<Item> assignLocationCode(WarehouseSpaceTypeRequest warehouseSpaceTypeRequest, List<Item> items){
        String baseLocation = generateBaseCodeSpaceType(warehouseSpaceTypeRequest.getWarehouseSpaceType());
        Map<String, Integer> spaceUsage= new HashMap<>();

        for(Item item: items) {
            String locationCode = findAvailableSpace("", baseLocation, spaceUsage);
            item.setLocationCode(locationCode);
            itemRepository.save(item);
        }
        return items;
    }

    private String findAvailableSpace(String locationPrefix,
                                      String baseLocation,
                                      Map<String, Integer> spaceUsage){
        char[] positions = {'A', 'B', 'C'};

        for(int index = 1; ; index++){
            for (char position: positions){
                String locationCode =  String.format("%s%s%02d%c", locationPrefix, baseLocation, spaceUsage);

                if(spaceUsage.getOrDefault(locationCode, 0) > 1) {
                    spaceUsage.put(locationCode, spaceUsage.getOrDefault(locationCode, 0) + 1);
                    return locationCode;
                }
            }
        }
    }

    private String generateBaseCodeSpaceType(WarehouseSpaceType warehouseSpaceType) {
        return switch (warehouseSpaceType){
            case SHELF -> "S";
            case DRAVER -> "D";
            case CONTAINER -> "C";
            default -> throw new IllegalArgumentException("Unknown warehouse space type!");
        };
    }

    private String generateLocationPrefix(WarehouseLocation warehouseLocation){
        return switch (warehouseLocation){
            case Warsaw -> "W";
            case Krakow -> "K";
            case Rzeszow -> "R";
            default -> throw new IllegalArgumentException("Unknown location!");
        };
    }
}
