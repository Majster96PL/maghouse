package com.example.maghouse.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<ItemEntity, Long> {
    Optional<ItemEntity> findByItemCode(String itemCode);

    @Query("SELECT i From Item i WHERE i.itemCode LIKE CONCAT(:prefix, '%')")
    List<Item> findByItemCodeStartingWith(@Param("prefix") String prefix);
}
