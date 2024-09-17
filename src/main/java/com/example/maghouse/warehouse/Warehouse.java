package com.example.maghouse.warehouse;

import com.example.maghouse.item.Item;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
public class Warehouse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank(message = "Type required!")
    @Enumerated(EnumType.STRING)
    private WarehouseSpaceType warehouseSpaceType;
    @NotBlank(message = "Location required!")
    @Size(max = 50)
    private String location;
    @OneToMany(mappedBy = "warehouse")
    private List<Item> items;
}
