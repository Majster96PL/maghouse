package com.example.maghouse.warehouse;

import com.example.maghouse.auth.registration.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {

    Optional<Warehouse> findByLocationAndUser(String location, User user);
}
