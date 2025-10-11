package com.example.maghouse.warehouse;

import com.example.maghouse.auth.registration.user.User;
import com.example.maghouse.auth.registration.user.UserRepository;
import com.example.maghouse.item.ItemEntity;
import com.example.maghouse.item.ItemRepository;
import com.example.maghouse.mapper.WarehouseResponseToWarehouseMapper;
import com.example.maghouse.warehouse.location.WarehouseLocation;
import com.example.maghouse.warehouse.location.WarehouseLocationRequest;
import com.example.maghouse.warehouse.spacetype.WarehouseSpaceType;
import com.example.maghouse.warehouse.spacetype.WarehouseSpaceTypeRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@AllArgsConstructor
public class WarehouseService {

    public static final String STARTING_LOCATION_PREFIX = "";
    private final WarehouseResponseToWarehouseMapper warehouseResponseToWarehouseMapper;
    private final UserRepository userRepository;
    private final WarehouseRepository warehouseRepository;
    private final ItemRepository itemRepository;
    private static final Logger LOGGER = LoggerFactory.getLogger(WarehouseService.class);

    public List<WarehouseEntity> getAllWarehouses() {
        return warehouseRepository.findAll();
    }

    public List<ItemEntity> getAllItemsByLocationCodePrefix(WarehouseLocation warehouseLocation){
        String prefix = generateLocationPrefix(warehouseLocation);
        return itemRepository.findByItemLocationStartingWith(prefix);

    }

    @Transactional
    public WarehouseEntity createWarehouse(WarehouseRequest warehouseRequest) {
        User user = getAthenticatedUser();
        LOGGER.info("Request to create warehouse: userId={}, location={}",
                user.getId(), warehouseRequest.getWarehouseLocation());
        String locationPrefix = generateLocationPrefix(warehouseRequest.getWarehouseLocation());
        LOGGER.debug("Generated location prefix={} for location={}",
                locationPrefix, warehouseRequest.getWarehouseLocation());
        List<ItemEntity> items = getItemsByPrefix(warehouseRequest, locationPrefix, user);
        LOGGER.debug("Found {} items for prefix={}", items.size(), locationPrefix);
        WarehouseEntity warehouse = buildWarehouseEntity(warehouseRequest, user, items);
        warehouseRepository.save(warehouse);
        itemRepository.saveAll(items);

        LOGGER.info("Warehouse created successfully: id={}, location={}, itemsCount={}, userId={}",
                warehouse.getId(),
                warehouseRequest.getWarehouseLocation(),
                items.size(),
                user.getId()
        );

        return warehouse;
    }

    @Transactional
    public ItemEntity assignItemsToWarehouseLocation(WarehouseLocationRequest warehouseLocationRequest, Long itemId) {
        User user = getAthenticatedUser();
        LOGGER.info("Request to add space type to Item: itemId = {}, location = {}",
                itemId, warehouseLocationRequest.getWarehouseLocation());
        ItemEntity item = getItemById(itemId, user);
        String locationPrefix = generateLocationPrefix(warehouseLocationRequest.getWarehouseLocation());
        String newLocation = locationPrefix + item.getLocationCode();
        item.setLocationCode(newLocation);
        item.setUser(user);
        WarehouseEntity warehouse = getOrCreateWarehouseForLocation(warehouseLocationRequest, user);
        addItemToWarehouse(item, warehouse);
        LOGGER.info("Successfully assigned location: {} to itemId: {}", newLocation, itemId);
        return item;
    }

    @Transactional
    public ItemEntity updatedItemsToWarehouseLocation(WarehouseLocationRequest warehouseLocationRequest, Long id) {
        User user = getAthenticatedUser();
        LOGGER.info("Request to update location to Item: itemId = {}, location = {}",
                id, warehouseLocationRequest.getWarehouseLocation());
        ItemEntity item = getItemById(id, user);
        String newLocationCode = updateItemLocationCode(item, warehouseLocationRequest);
        item.setLocationCode(newLocationCode);
        item.setUser(user);
        removeItemFromCurrentWarehouse(item);
        WarehouseEntity warehouse = getOrCreateWarehouseForLocation(warehouseLocationRequest, user);
        addItemToWarehouse(item, warehouse);
        LOGGER.info("Successfully updated itemId={} to newLocationCode={} in warehouseId={}",
                item.getId(), newLocationCode, warehouse.getId());
        return item;
    }

    @Transactional
    public ItemEntity assignWarehouseSpaceType(WarehouseSpaceTypeRequest warehouseSpaceTypeRequest, Long id) {
        User user = getAthenticatedUser();
        LOGGER.info("Request to add space type to Item: itemId = {}, spaceType = {}",
                id, warehouseSpaceTypeRequest.getWarehouseSpaceType());

        ItemEntity item = itemRepository.findUnassignedItem(id)
                .orElseThrow(() -> {
                  LOGGER.warn("Attempted to access item {} that already has location code", id);
                  return new IllegalArgumentException("Item already has a locationCode!");
                });

        String baseLocation = generateBaseCodeSpaceType(warehouseSpaceTypeRequest.getWarehouseSpaceType());
        Set<String> usedSpaces = new HashSet<>(itemRepository.findUsedLocationCodes(baseLocation));
        Optional<String> locationCode = findAvailableSpace(STARTING_LOCATION_PREFIX, baseLocation, usedSpaces);

        return locationCode
            .map(lc -> {
               item.setLocationCode(lc);
               item.setUser(user);
               LOGGER.info("Successfully assigned locationCode={} to itemId={} for userId={}",
                   locationCode, item.getId(), user.getId());
               return item;
            })
           .orElseThrow(() -> {
              LOGGER.info("Not implemented yet. TODO | FIXME");
              return new IllegalArgumentException("Unable to assign location code!");
            });
    }

    public WarehouseLocation getWarehouseLocationByPrefix(String prefix) {
        for(WarehouseLocation location : WarehouseLocation.values()) {
            String locationPrefix = generateLocationPrefix(location);
            if (locationPrefix.equals(prefix.toUpperCase())) {
                return location;
            }
        }
        throw new IllegalArgumentException("Unknow location prefix: " + prefix);
    }

    private User getAthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            LOGGER.warn("Authentication failed - user not authenticated!");
            throw new SecurityException("User is not authenticated!");
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        LOGGER.debug("User authenticated : {}", userDetails.getUsername());
        return userRepository.findUserByEmail(userDetails.getUsername())
                .orElseThrow(() -> {
                    LOGGER.warn("User not found in database! Email: {}", userDetails.getUsername());
                    return new IllegalArgumentException("User with email not found: " + userDetails.getUsername());

                });
    }

    private ItemEntity getItemById(Long id, User user) {
        return itemRepository.findById(id)
                .orElseThrow(() -> {
                    LOGGER.warn("Item not found for id= {} by userId= {}",
                            id, user.getId());
                    return new IllegalArgumentException("Item not found for id: " + id);
                });
    }

    private List<ItemEntity> getItemsByPrefix(WarehouseRequest warehouseRequest,
                                              String locationPrefix, User user) {
        List<ItemEntity> items = itemRepository.findByItemLocationStartingWith(locationPrefix);
        if (items.isEmpty()) {
            LOGGER.warn("No items found for location Prefix while creating warehouse." +
                        "Location: {}, Prefix: {}, User: {}",
                    warehouseRequest.getWarehouseLocation(), locationPrefix, user.getId());
            throw new IllegalArgumentException("No items found for location prefix: " + locationPrefix);
        }
        return items;
    }

    private WarehouseEntity buildWarehouseEntity(WarehouseRequest warehouseRequest,
                                                 User user, List<ItemEntity> items) {
        WarehouseResponse warehouseResponse = warehouseResponseToWarehouseMapper.mapToWarehouseResponse(warehouseRequest);
        warehouseResponse.setUserId(user.getId());
        warehouseResponse.setItemsId(items.stream()
                .map(ItemEntity::getId)
                .toList());
        WarehouseEntity warehouse = warehouseResponseToWarehouseMapper.mapToEntityFromResponse(warehouseResponse);
        warehouse.setUser(user);

        items.forEach(item -> {
            item.setWarehouseEntity(warehouse);
            warehouse.getItems().add(item);
        });
        return warehouse;
    }

    private String updateItemLocationCode(ItemEntity item,
                                          WarehouseLocationRequest warehouseLocationRequest) {
        String currentLocation = item.getLocationCode();
        if (currentLocation == null || currentLocation.length() <= 1) {
            LOGGER.error("Invalid locationCode for itemId={}: {}", item.getId(), currentLocation);
            throw new IllegalArgumentException("item location code is invalid or too short for update!");
        }
        String restOfString = currentLocation.substring(1);
        String newPrefix = generateLocationPrefix(warehouseLocationRequest.getWarehouseLocation());
        String newLocationCode = newPrefix + restOfString;
        LOGGER.debug("Changed locationCode from {} -> {} for itemId={}",
                currentLocation, newLocationCode, item.getId());
        return newLocationCode;
    }

    private boolean removeItemFromCurrentWarehouse(ItemEntity item) {
        WarehouseEntity currentWarehouse = item.getWarehouseEntity();
        if (currentWarehouse != null) {
            currentWarehouse.getItems().remove(item);
            item.setWarehouseEntity(null);
            LOGGER.debug("Removed itemId={} from warehouseId={}", item.getId(),
                    currentWarehouse.getId());
            return true;
        }
        LOGGER.debug("ItemId={} was not assigned to any warehouse", item.getId());
        return false;
    }

    private WarehouseEntity getOrCreateWarehouseForLocation(WarehouseLocationRequest warehouseLocationRequest,
                                                            User user) {
        String prefix = generateLocationPrefix(warehouseLocationRequest.getWarehouseLocation());
        LOGGER.debug("Searching for warehouse with prefix={} and location={}", prefix,
                warehouseLocationRequest.getWarehouseLocation());
        return warehouseRepository.findByWarehouseLocation(warehouseLocationRequest.getWarehouseLocation())
                .orElseGet(() -> {
                    LOGGER.info("No warehouse found for location={}, creating new one",
                            warehouseLocationRequest.getWarehouseLocation());
                    WarehouseRequest warehouseRequest = WarehouseRequest.builder()
                            .warehouseLocation(warehouseLocationRequest.getWarehouseLocation())
                            .build();
                    WarehouseEntity newWarehouse = buildWarehouseEntity(warehouseRequest, user, new ArrayList<>());
                    LOGGER.info("Creating new warehouseId={} for location={}", newWarehouse.getId(),
                            warehouseLocationRequest.getWarehouseLocation());
                    return newWarehouse;
                });
    }

    private WarehouseEntity addItemToWarehouse(ItemEntity item, WarehouseEntity warehouseEntity) {
        item.setWarehouseEntity(warehouseEntity);
        warehouseEntity.getItems().add(item);
        warehouseRepository.save(warehouseEntity);
        LOGGER.debug("Assigned itemId={} to warehouseId={}", item.getId(), warehouseEntity.getId());
        return warehouseEntity;
    }

    private Optional<String> findAvailableSpace(String locationPrefix,
                                      String baseLocation,
                                      Set<String> spaceUsage) {
        char[] positions = {'A', 'B', 'C'};

        for (int index = 1; index <= 50 ; index++) {
            for (char position : positions) {
                String locationCode = String.format("%s%s%02d%c", locationPrefix, baseLocation, index, position);
                if (!spaceUsage.contains(locationCode)) {
                    spaceUsage.add(locationCode);
                    LOGGER.debug("Allocated new location code={} for baseLocation={}",
                            locationCode, baseLocation);
                    return Optional.of(locationCode);
                }
            }
        }
        return Optional.empty();
    }

    private String generateBaseCodeSpaceType(WarehouseSpaceType warehouseSpaceType) {
        return switch (warehouseSpaceType) {
            case SHELF -> "S";
            case DRAVER -> "D";
            case CONTAINER -> "C";
            default -> throw new IllegalArgumentException("Unknown warehouse space type!");
        };
    }

    private String generateLocationPrefix(WarehouseLocation warehouseLocation) {
        return switch (warehouseLocation) {
            case Warsaw -> "W";
            case Krakow -> "K";
            case Rzeszow -> "R";
            default -> throw new IllegalArgumentException("Unknown location!");
        };
    }
}