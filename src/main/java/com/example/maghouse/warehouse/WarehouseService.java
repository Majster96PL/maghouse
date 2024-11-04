package com.example.maghouse.warehouse;

import com.example.maghouse.auth.registration.user.UserRepository;
import com.example.maghouse.mapper.WarehouseRequestToWarehouseMapper;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class WarehouseService {

    private final WarehouseRequestToWarehouseMapper warehouseRequestToWarehouseMapper;
    private final UserRepository userRepository;
    private final WarehouseRepository warehouseRepository;


    public Warehouse createWarehouse(WarehouseRequest warehouseRequest, WarehouseResponse warehouseResponse) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()){
            throw new SecurityException("User is not authenticated!");
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        var user = userRepository.findUserByEmail(userDetails.getUsername())
                .orElseThrow( () -> new IllegalArgumentException("User with email not found!"));
        var warehouse = warehouseRequestToWarehouseMapper.map(warehouseResponse);
        return null;
    }
}
