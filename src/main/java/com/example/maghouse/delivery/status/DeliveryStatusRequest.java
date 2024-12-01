package com.example.maghouse.delivery.status;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryStatusRequest {
    private DeliveryStatus deliveryStatus;
}
