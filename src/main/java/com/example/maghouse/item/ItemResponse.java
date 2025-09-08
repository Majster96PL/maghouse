package com.example.maghouse.item;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ItemResponse {
    private String name;
    private String itemCode;
    private int quantity;
    private String locationCode;
    private long userId;

}
