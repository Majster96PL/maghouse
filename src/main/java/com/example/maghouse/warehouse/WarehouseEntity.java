package com.example.maghouse.warehouse;

import com.example.maghouse.auth.registration.user.User;
import com.example.maghouse.item.ItemEntity;
import com.example.maghouse.warehouse.location.WarehouseLocation;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@Table(name = "warehouse")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull(message = "Location required!")
    @Enumerated(EnumType.STRING)
    private WarehouseLocation warehouseLocation;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    @OneToMany(mappedBy = "warehouseEntity", cascade = CascadeType.ALL)
    private List<ItemEntity> items;
}
