package com.example.maghouse.item;

import com.example.maghouse.auth.registration.user.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemResponse {
    private String name;
    private String itemCode;
    private int quantity;
    private String locationCode;
    private User user;

}
