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

    @Query("SELECT i FROM ItemEntity i WHERE i.locationCode LIKE CONCAT(:prefix, '%')")
    List<ItemEntity> findByItemLocationStartingWith(@Param("prefix") String prefix);

    @Query("SELECT i.locationCode FROM ItemEntity i " +
            "WHERE i.locationCode LIKE CONCAT(:prefix, '%') AND i.locationCode IS NOT NULL")
    List<String> findUsedLocationCodes(@Param("prefix") String prefix);

    @Query("SELECT i FROM ItemEntity i WHERE i.id = :id AND i.locationCode IS NULL ")
    Optional<ItemEntity> findUnassignedItem(@Param("id") Long id);

}
