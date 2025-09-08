package com.example.maghouse.delivery;

import com.example.maghouse.auth.registration.user.User;
import com.example.maghouse.delivery.status.DeliveryStatus;
import com.example.maghouse.item.ItemEntity;
import com.example.maghouse.warehouse.location.WarehouseLocation;
import com.fasterxml.jackson.annotation.JsonBackReference;
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
    @Enumerated(EnumType.STRING)
    private WarehouseLocation warehouseLocation;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    @ManyToOne
    @JoinColumn(name = "item_id")
    @JsonBackReference
    private ItemEntity item;


}
