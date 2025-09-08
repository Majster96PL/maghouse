package com.example.maghouse.item;

import com.example.maghouse.auth.registration.user.User;
import com.example.maghouse.delivery.DeliveryEntity;
import com.example.maghouse.warehouse.WarehouseEntity;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Entity
@Builder
@Data
@Table(name = "items")
@AllArgsConstructor
@NoArgsConstructor
public class ItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank(message = "Name required!")
    @Size(max = 50)
    private String name;
    @Size(max = 50)
    @NotBlank(message = "Code required!")
    private String itemCode;
    @NotNull(message = "Quantity required!")
    private int quantity;
    @Size(max = 50)
    private String locationCode;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @ManyToOne
    @JoinColumn(name = "warehouse_id")
    private WarehouseEntity warehouseEntity;
    @OneToMany(mappedBy = "item")
    @JsonManagedReference
    private List<DeliveryEntity> deliveries;

}
