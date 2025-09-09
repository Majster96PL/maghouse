package com.example.maghouse.delivery;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DeliveryRepository extends JpaRepository<DeliveryEntity, Long> {

    @Query("SELECT COUNT(d) FROM DeliveryEntity d WHERE EXTRACT(MONTH FROM d.date) = :month AND EXTRACT(YEAR FROM d.date) = :year")
    int countByMonthAndYear(@Param("month") int month, @Param("year") int year);
}
