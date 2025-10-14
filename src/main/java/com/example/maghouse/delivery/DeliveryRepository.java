package com.example.maghouse.delivery;

import com.example.maghouse.delivery.status.DeliveryStatus;
import com.example.maghouse.warehouse.location.WarehouseLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryRepository extends JpaRepository<DeliveryEntity, Long> {

    @Query("SELECT COUNT(d) FROM DeliveryEntity d WHERE EXTRACT(MONTH FROM d.date) = :month AND EXTRACT(YEAR FROM d.date) = :year")
    int countByMonthAndYear(@Param("month") int month, @Param("year") int year);

    List<DeliveryEntity> findByDeliveryStatus(DeliveryStatus deliveryStatus);

    Optional<DeliveryEntity> findByNumberDelivery(String numberDelivery);

    List<DeliveryEntity> findBySupplierContainingIgnoreCase(String supplier);

    List<DeliveryEntity> findByWarehouseLocation(WarehouseLocation warehouseLocation);

    List<DeliveryEntity> findByItemCode(String itemCode);
}
