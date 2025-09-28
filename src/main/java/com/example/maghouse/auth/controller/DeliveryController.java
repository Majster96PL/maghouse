package com.example.maghouse.auth.controller;

import com.example.maghouse.delivery.DeliveryEntity;
import com.example.maghouse.delivery.DeliveryRequest;
import com.example.maghouse.delivery.DeliveryResponse;
import com.example.maghouse.delivery.DeliveryService;
import com.example.maghouse.delivery.status.DeliveryStatusRequest;
import com.example.maghouse.mapper.DeliveryResponseToDeliveryMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(path = "/deliveries/")
@RequiredArgsConstructor
@Tag(name = "Delivery Management", description = "Endpoints for creating and updating delivery status.")
@SecurityRequirement(name = "bearerAuth")
public class DeliveryController {

    private final DeliveryService deliveryService;
    private final DeliveryResponseToDeliveryMapper deliveryResponseToDeliveryMapper;

    @PostMapping
    @Operation(summary = "Create a new delivery",
            description = "Creates a new delivery order based on the provided request data.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Delivery successfully created",
                    content = @Content(schema = @Schema(implementation = DeliveryResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data or validation error",
                    content = @Content(schema = @Schema(implementation = Map.class))), // Map.class dla standardowego błędu
            @ApiResponse(responseCode = "401", description = "Unauthorized access (missing or invalid token)",
                    content = @Content)
    })
    public ResponseEntity<DeliveryResponse> create(@RequestBody  DeliveryRequest deliveryRequest){
        DeliveryEntity deliveryEntity = deliveryService.createDelivery(deliveryRequest);
        DeliveryResponse deliveryResponse = deliveryResponseToDeliveryMapper.mapToResponse(deliveryEntity);
        if (deliveryRequest == null) {
            throw new IllegalArgumentException("Delivery request cannot be null");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(deliveryResponse);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update delivery status",
            description = "Updates the status (e.g., 'SHIPPED', 'DELIVERED') of a specific delivery by ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Delivery status successfully updated",
                    content = @Content(schema = @Schema(implementation = DeliveryResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request or status code provided",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized access",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Delivery with the given ID was not found",
                    content = @Content(schema = @Schema(implementation = Map.class)))
    })
    public ResponseEntity<DeliveryResponse> updateDeliveryStatus(@RequestBody DeliveryStatusRequest deliveryStatusRequest,
                                         @PathVariable Long id){
        DeliveryEntity updatedDelivery = deliveryService.updateDeliveryStatus(deliveryStatusRequest, id);
        DeliveryResponse deliveryResponse = deliveryResponseToDeliveryMapper.mapToResponse(updatedDelivery);
        if(deliveryStatusRequest == null || id == null) {
            throw new IllegalArgumentException("Delivery status request cannot be null");
        }
        return ResponseEntity.ok(deliveryResponse);
    }
}
