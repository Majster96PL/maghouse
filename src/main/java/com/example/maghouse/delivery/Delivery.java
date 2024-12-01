package com.example.maghouse.delivery;

import com.example.maghouse.auth.registration.user.User;
import com.example.maghouse.item.Item;
import com.example.maghouse.warehouse.Warehouse;
import com.example.maghouse.warehouse.location.WarehouseLocation;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "delivery" )
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank(message = "Supplier required!")
    @Size(max = 50)
    private String supplier;
    @Temporal(TemporalType.DATE)
    private Date date;
    private String numberDelivery;
    private String itemName;
    private String itemCode;
    private int quantity;
    @Enumerated(EnumType.STRING)
    private DeliveryStatus deliveryStatus;
    private WarehouseLocation warehouseLocation;
    private User user;

}
