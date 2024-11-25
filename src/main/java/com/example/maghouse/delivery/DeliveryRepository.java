package com.example.maghouse.delivery;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    @Query("SELECT d FROM Delivery d WHERE FUNCTION('MONTH', d.date) = :month AND FUNCTION('YEAR', d.date) = :year ")
    List<Delivery> findByMonthAndYear(@Param("month") int month, @Param("year") int year);
}
