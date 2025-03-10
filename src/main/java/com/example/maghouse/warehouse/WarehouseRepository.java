package com.example.maghouse.warehouse;

import com.example.maghouse.warehouse.location.WarehouseLocation;
import com.example.maghouse.warehouse.spacetype.WarehouseSpaceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
    Optional<Warehouse> findByWarehouseSpaceType(WarehouseSpaceType spaceType);
    Optional<Warehouse> findByWarehouseLocation(WarehouseLocation location);
}
