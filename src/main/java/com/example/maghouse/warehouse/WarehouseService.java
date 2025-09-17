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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@AllArgsConstructor
public class WarehouseService {

    private final WarehouseResponseToWarehouseMapper warehouseResponseToWarehouseMapper;
    private final UserRepository userRepository;
    private final WarehouseRepository warehouseRepository;
    private final ItemRepository itemRepository;
    private static final Logger log = LoggerFactory.getLogger(WarehouseService.class);

    @Transactional
    public WarehouseEntity createWarehouse(WarehouseRequest warehouseRequest) {
        User user = getAthenticatedUser();
        log.info("Request to create warehouse: userId={}, location={}",
                user.getId(), warehouseRequest.getWarehouseLocation());
        String locationPrefix = generateLocationPrefix(warehouseRequest.getWarehouseLocation());
        log.debug("Generated location prefix={} for location={}",
                locationPrefix, warehouseRequest.getWarehouseLocation());
        List<ItemEntity> items = getItemsByPrefix(warehouseRequest, locationPrefix, user);
        log.debug("Found {} items for prefix={}", items.size(), locationPrefix);
        WarehouseEntity warehouse = buildWarehouseEntity(warehouseRequest, user, items);
        warehouseRepository.save(warehouse);
        itemRepository.saveAll(items);

        log.info("Warehouse created successfully: id={}, location={}, itemsCount={}, userId={}",
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
        log.info("Request to add space type to Item: itemId = {}, location = {}",
                itemId, warehouseLocationRequest.getWarehouseLocation());
        ItemEntity item = getItemById(itemId, user);
        String locationPrefix = generateLocationPrefix(warehouseLocationRequest.getWarehouseLocation());
        String newLocation = locationPrefix + item.getLocationCode();
        log.debug("Location transformation: {} + {} = {}",
                locationPrefix, item.getLocationCode(), newLocation);
        item.setLocationCode(newLocation);
        item.setUser(user);
        itemRepository.save(item);
        log.info("Successfully assigned location: {} to itemId: {}", newLocation, itemId);
        return item;
    }

    @Transactional
    public ItemEntity updatedItemsToWarehouseLocation(WarehouseLocationRequest warehouseLocationRequest, Long id) {
        User user = getAthenticatedUser();
        log.info("Request to update location to Item: itemId = {}, location = {}",
                id, warehouseLocationRequest.getWarehouseLocation());
        ItemEntity item = getItemById(id, user);
        String newLocationCode = updateItemLocationCode(item, warehouseLocationRequest);
        item.setLocationCode(newLocationCode);
        item.setUser(user);
        removeItemFromCurrentWarehouse(item);
        WarehouseEntity warehouse = getOrCreateWarehouseForLocation(warehouseLocationRequest, user);
        addItemToWarehouse(item, warehouse);
        log.info("Successfully updated itemId={} to newLocationCode={} in warehouseId={}",
                item.getId(), newLocationCode, warehouse.getId());
        itemRepository.save(item);
        return item;
    }

    @Transactional
    public ItemEntity assignWarehouseSpaceType(WarehouseSpaceTypeRequest warehouseSpaceTypeRequest, Long id) {
        User user = getAthenticatedUser();
        log.info("Request to add space type to Item: itemId = {}, spaceType = {}",
                id, warehouseSpaceTypeRequest.getWarehouseSpaceType());
        String baseLocation = generateBaseCodeSpaceType(warehouseSpaceTypeRequest.getWarehouseSpaceType());
        log.info("Assigning new location for itemId= {} using baseLocation= {} by userId= {}",
                id, baseLocation, user.getId());
        ItemEntity item = itemRepository.findUnassignedItem(id)
                .orElseThrow(() -> {
                    log.warn("Attempted to access item {} that already has location code", id);
                    return new IllegalArgumentException("Item already has a locationCode!");
                });
        log.debug("Found unassigned item: itemId={}", item.getId());
        Set<String> usedSpaces = new HashSet<>(itemRepository.findUsedLocationCodes(baseLocation));
        log.debug("Found {} used spaces for baseLocation: {}", usedSpaces.size(), baseLocation);
        String locationCode = generateNewLocationCode(baseLocation, usedSpaces);
        log.info("Generated location code: {} for itemId: {}", locationCode, id);
        item.setLocationCode(locationCode);
        item.setUser(user);
        itemRepository.save(item);
        log.debug("Item successfully saved with location code: {}", locationCode);
        log.info("Successfully assigned locationCode={} to itemId={} for userId={}",
                locationCode, item.getId(), user.getId());
        return item;
    }

    private User getAthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Authentication failed - user not authenticated!");
            throw new SecurityException("User is not authenticated!");
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        log.debug("User authenticated : {}", userDetails.getUsername());
        return userRepository.findUserByEmail(userDetails.getUsername())
                .orElseThrow(() -> {
                    log.warn("User not found in database! Email: {}", userDetails.getUsername());
                    return new IllegalArgumentException("User with email not found: " + userDetails.getUsername());

                });
    }

    private ItemEntity getItemById(Long id, User user) {
        return itemRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Item not found for id= {} by userId= {}",
                            id, user.getId());
                    return new IllegalArgumentException("Item not found for id: " + id);
                });
    }

    private String generateNewLocationCode(String baseLocation, Set<String> usedSpaces) {
        return findAvailableSpace("", baseLocation, usedSpaces);
    }

    private List<ItemEntity> getItemsByPrefix(WarehouseRequest warehouseRequest,
                                              String locationPrefix, User user) {
        List<ItemEntity> items = itemRepository.findByItemCodeStartingWith(locationPrefix);
        if (items.isEmpty()) {
            log.warn("No items found for location Prefix while creating warehouse." +
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
            log.error("Invalid locationCode for itemId={}: {}", item.getId(), currentLocation);
            throw new IllegalArgumentException("item location code is invalid or too short for update!");
        }
        String restOfString = currentLocation.substring(1);
        String newPrefix = generateLocationPrefix(warehouseLocationRequest.getWarehouseLocation());
        String newLocationCode = newPrefix + restOfString;
        log.debug("Changed locationCode from {} -> {} for itemId={}",
                currentLocation, newLocationCode, item.getId());
        return newLocationCode;
    }

    private void removeItemFromCurrentWarehouse(ItemEntity item) {
        WarehouseEntity currentWarehouse = item.getWarehouseEntity();
        if (currentWarehouse != null) {
            currentWarehouse.getItems().remove(item);
            item.setWarehouseEntity(null);
            warehouseRepository.save(currentWarehouse);
            log.debug("Removed itemId={} from warehouseId={}", item.getId(),
                    currentWarehouse.getId());
        } else {
            log.debug("ItemId={} was not assigned to any warehouse", item.getId());
        }
    }

    private WarehouseEntity getOrCreateWarehouseForLocation(WarehouseLocationRequest warehouseLocationRequest,
                                                            User user) {
        String prefix = generateLocationPrefix(warehouseLocationRequest.getWarehouseLocation());
        log.debug("Searching for warehouse with prefix={} and location={}", prefix,
                warehouseLocationRequest.getWarehouseLocation());
        return warehouseRepository.findByWarehouseLocation(warehouseLocationRequest.getWarehouseLocation())
                .orElseGet(() -> {
                    log.info("No warehouse found for location={}, creating new one",
                            warehouseLocationRequest.getWarehouseLocation());
                    WarehouseRequest warehouseRequest = WarehouseRequest.builder()
                            .warehouseLocation(warehouseLocationRequest.getWarehouseLocation())
                            .build();
                    List<ItemEntity> items = new ArrayList<>();
                    WarehouseEntity newWarehouse = buildWarehouseEntity(warehouseRequest, user, items);
                    WarehouseEntity savedWarehouse = warehouseRepository.save(newWarehouse);
                    log.info("Creating new warehouseId={} for location={}", savedWarehouse.getId(),
                            warehouseLocationRequest.getWarehouseLocation());
                    return savedWarehouse;
                });
    }

    private void addItemToWarehouse(ItemEntity item, WarehouseEntity warehouseEntity) {
        item.setWarehouseEntity(warehouseEntity);
        warehouseEntity.getItems().add(item);
        warehouseRepository.save(warehouseEntity);
        log.debug("Assigned itemId={} to warehouseId={}", item.getId(), warehouseEntity.getId());
    }

    private String findAvailableSpace(String locationPrefix,
                                      String baseLocation,
                                      Set<String> spaceUsage) {
        char[] positions = {'A', 'B', 'C'};

        for (int index = 1; ; index++) {
            for (char position : positions) {
                String locationCode = String.format("%s%s%02d%c", locationPrefix, baseLocation, index, position);
                if (!spaceUsage.contains(locationCode)) {
                    spaceUsage.add(locationCode);
                    log.debug("Allocated new location code={} for baseLocation={}",
                            locationCode, baseLocation);
                    return locationCode;
                }
            }
        }
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