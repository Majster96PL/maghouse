package com.example.maghouse.warehouse;

import com.example.maghouse.warehouse.location.WarehouseLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
    Optional<Warehouse> findFirstByWarehouseLocation(WarehouseLocation location);
}
