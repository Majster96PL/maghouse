package com.example.maghouse.warehouse;

import com.example.maghouse.auth.registration.user.UserRepository;
import com.example.maghouse.item.Item;
import com.example.maghouse.item.ItemRepository;
import com.example.maghouse.mapper.WarehouseResponseToWarehouseMapper;
import com.example.maghouse.warehouse.location.WarehouseLocation;
import com.example.maghouse.warehouse.location.WarehouseLocationRequest;
import com.example.maghouse.warehouse.spacetype.WarehouseSpaceType;
import com.example.maghouse.warehouse.spacetype.WarehouseSpaceTypeRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@AllArgsConstructor
public class WarehouseService {

    private final WarehouseResponseToWarehouseMapper warehouseResponseToWarehouseMapper;
    private final UserRepository userRepository;
    private final WarehouseRepository warehouseRepository;
    private final ItemRepository itemRepository;
    private static final WarehouseResponse warehouseResponse = new WarehouseResponse();


    @Transactional
    public Warehouse createWarehouse(WarehouseRequest warehouseRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()){
            throw new SecurityException("User is not authenticated!");
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        var user = userRepository.findUserByEmail(userDetails.getUsername())
                .orElseThrow( () -> new IllegalArgumentException("User with email not found!"));

        warehouseResponse.setWarehouseSpaceType(warehouseRequest.getWarehouseSpaceType());
        warehouseResponse.setWarehouseLocation(warehouseRequest.getWarehouseLocation());
        warehouseResponse.setUser(user);
        var warehouse = warehouseResponseToWarehouseMapper.mapToEntity(warehouseResponse);
        return warehouseRepository.save(warehouse);
    }

    public Item assignItemsToWarehouseLocation( WarehouseLocationRequest warehouseLocationRequest, Long itemId){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("User is not authenticated!");
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        var user = userRepository.findUserByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User with email not found!"));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found!"));

        Warehouse warehouse = warehouseRepository.findByWarehouseLocation(warehouseLocationRequest.getWarehouseLocation())
                .orElseThrow(() -> new IllegalArgumentException("Warehouse not found!"));

        String locationPrefix = generateLocationPrefix(warehouseLocationRequest.getWarehouseLocation());


        String newLocation = locationPrefix + item.getLocationCode();
        item.setLocationCode(newLocation);
        item.setUser(user);
        itemRepository.save(item);

        return item;
    }

    public Item updatedItemsToWarehouseLocation(WarehouseLocationRequest warehouseLocationRequest, Long id){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()){
            throw new SecurityException("User is not authenticated!");
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        var user = userRepository.findUserByEmail(userDetails.getUsername())
                .orElseThrow( () -> new IllegalArgumentException("User with email not found!"));

        var item = itemRepository.findById(id)
                .orElseThrow( () -> new IllegalArgumentException("Item not found!"));

        String currentLocation = item.getLocationCode();
        String restOfString = currentLocation.substring(1);
        String newPrefix = generateLocationPrefix(warehouseLocationRequest.getWarehouseLocation());
        String newLocation = newPrefix + restOfString;
        item.setLocationCode(newLocation);
        item.setUser(user);
        itemRepository.save(item);
        return item;
    }

    public Item assignLocationCode(WarehouseSpaceTypeRequest warehouseSpaceTypeRequest, Long id){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()){
            throw new SecurityException("User is not authenticated!");
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        var user = userRepository.findUserByEmail(userDetails.getUsername())
                .orElseThrow( () -> new IllegalArgumentException("User with email not found!"));
        String baseLocation = generateBaseCodeSpaceType(warehouseSpaceTypeRequest.getWarehouseSpaceType());
        Map<String, Integer> spaceUsage = new HashMap<>();
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Item not found!"));

        String locationCode = findAvailableSpace("", baseLocation, spaceUsage);
        item.setUser(user);
        item.setLocationCode(locationCode);
        itemRepository.save(item);

        return item;
    }

    private String findAvailableSpace(String locationPrefix,
                                      String baseLocation,
                                      Map<String, Integer> spaceUsage){
        char[] positions = {'A', 'B', 'C'};

        for(int index = 1; ; index++){
            for (char position: positions){
                String locationCode =  String.format("%s%s%02d%c", locationPrefix, baseLocation, index, position);

                if(spaceUsage.getOrDefault(locationCode, 0) == 0) {
                    spaceUsage.put(locationCode, 1);
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