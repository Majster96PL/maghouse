package com.example.maghouse.auth.controller;

import com.example.maghouse.auth.registration.user.User;
import com.example.maghouse.auth.registration.user.UserService;
import com.example.maghouse.delivery.DeliveryEntity;
import com.example.maghouse.delivery.DeliveryRequest;
import com.example.maghouse.delivery.DeliveryResponse;
import com.example.maghouse.delivery.DeliveryService;
import com.example.maghouse.delivery.status.DeliveryStatus;
import com.example.maghouse.delivery.status.DeliveryStatusRequest;
import com.example.maghouse.mapper.DeliveryResponseToDeliveryMapper;
import com.example.maghouse.warehouse.location.WarehouseLocation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/deliveries/")
@Tag(name = "Delivery Management", description = "Endpoints for creating and updating delivery status.")
@SecurityRequirement(name = "bearerAuth")
public class DeliveryController extends BaseController {

    private final DeliveryService deliveryService;
    private final DeliveryResponseToDeliveryMapper deliveryResponseToDeliveryMapper;

    public DeliveryController(UserService userService,
                              DeliveryService deliveryService,
                              DeliveryResponseToDeliveryMapper deliveryResponseToDeliveryMapper) {
        super(userService);
        this.deliveryService = deliveryService;
        this.deliveryResponseToDeliveryMapper = deliveryResponseToDeliveryMapper;
    }

    @GetMapping
    @Operation(summary = "Get all deliveries", description = "Retrieves a list of all deliveries.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of deliveries"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    public ResponseEntity<List<DeliveryResponse>> getAllDeliveries() {
        User user = getAuthenticatedUser();
        List<DeliveryEntity> deliveries = deliveryService.getAllDeliveries();
        List<DeliveryResponse> responses = deliveries.stream()
                .map(deliveryResponseToDeliveryMapper::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get deliveries by status", description = "Retrieves deliveries filtered by delivery status.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved deliveries by status"),
            @ApiResponse(responseCode = "404", description = "No deliveries found for the given status"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    public ResponseEntity<List<DeliveryResponse>> getDeliveriesByStatus(@PathVariable DeliveryStatus status) {
        User user = getAuthenticatedUser();
        List<DeliveryEntity> deliveries = deliveryService.getDeliveriesByStatus(status);
        if (deliveries.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        List<DeliveryResponse> responses = deliveries.stream()
                .map(deliveryResponseToDeliveryMapper::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/number/{deliveryNumber}")
    @Operation(summary = "Get delivery by delivery number", description = "Retrieves a specific delivery by its unique delivery number.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved delivery"),
            @ApiResponse(responseCode = "404", description = "Delivery with the given number not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    public ResponseEntity<DeliveryResponse> getDeliveryByNumber(@PathVariable String deliveryNumber) {
        User user = getAuthenticatedUser();
        Optional<DeliveryEntity> delivery = deliveryService.getDeliveryByNumber(deliveryNumber);
        if (delivery.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        DeliveryResponse response = deliveryResponseToDeliveryMapper.mapToResponse(delivery.get());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/supplier/{supplierName}")
    @Operation(summary = "Get deliveries by supplier", description = "Retrieves deliveries filtered by supplier name.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved deliveries by supplier"),
            @ApiResponse(responseCode = "404", description = "No deliveries found for the given supplier"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    public ResponseEntity<List<DeliveryResponse>> getDeliveriesBySupplier(@PathVariable String supplierName) {
        User user = getAuthenticatedUser();
        List<DeliveryEntity> deliveries = deliveryService.getDeliveriesBySupplier(supplierName);
        if (deliveries.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        List<DeliveryResponse> responses = deliveries.stream()
                .map(deliveryResponseToDeliveryMapper::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/location/{warehouseLocation}")
    @Operation(summary = "Get deliveries by warehouse location", description = "Retrieves deliveries filtered by warehouse location.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved deliveries by location"),
            @ApiResponse(responseCode = "404", description = "No deliveries found for the given location"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    public ResponseEntity<List<DeliveryResponse>> getDeliveriesByLocation(@PathVariable WarehouseLocation warehouseLocation) {
        User user = getAuthenticatedUser();
        List<DeliveryEntity> deliveries = deliveryService.getDeliveriesByLocation(warehouseLocation);
        if (deliveries.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        List<DeliveryResponse> responses = deliveries.stream()
                .map(deliveryResponseToDeliveryMapper::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/item/{itemCode}")
    @Operation(summary = "Get deliveries by item code", description = "Retrieves deliveries filtered by item code.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved deliveries by item code"),
            @ApiResponse(responseCode = "404", description = "No deliveries found for the given item code"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    public ResponseEntity<List<DeliveryResponse>> getDeliveriesByItemCode(@PathVariable String itemCode) {
        User user = getAuthenticatedUser();
        List<DeliveryEntity> deliveries = deliveryService.getDeliveriesByItemCode(itemCode);
        if (deliveries.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        List<DeliveryResponse> responses = deliveries.stream()
                .map(deliveryResponseToDeliveryMapper::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get delivery by ID", description = "Retrieves a specific delivery by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved delivery"),
            @ApiResponse(responseCode = "404", description = "Delivery with the given ID not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    public ResponseEntity<DeliveryResponse> getDeliveryById(@PathVariable Long id) {
        User user = getAuthenticatedUser();
        Optional<DeliveryEntity> delivery = deliveryService.getDeliveryById(id);
        if (delivery.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        DeliveryResponse response = deliveryResponseToDeliveryMapper.mapToResponse(delivery.get());
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @Operation(summary = "Create a new delivery",
            description = "Creates a new delivery order based on the provided request data.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Delivery successfully created",
                    content = @Content(schema = @Schema(implementation = DeliveryResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data or validation error",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized access (missing or invalid token)",
                    content = @Content)
    })
    public ResponseEntity<DeliveryResponse> create(@RequestBody  DeliveryRequest deliveryRequest){
        User user = getAuthenticatedUser();
        if (deliveryRequest == null) {
            throw new IllegalArgumentException("Delivery request cannot be null");
        }
        DeliveryEntity deliveryEntity = deliveryService.createDelivery(deliveryRequest, user );
        DeliveryResponse deliveryResponse = deliveryResponseToDeliveryMapper.mapToResponse(deliveryEntity);
        return ResponseEntity.status(HttpStatus.CREATED).body(deliveryResponse);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update delivery status",
            description = "Updates the status of a specific delivery by ID.")
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
        User user = getAuthenticatedUser();
        if(deliveryStatusRequest == null || id == null) {
            throw new IllegalArgumentException("Delivery status request cannot be null");
        }
        DeliveryEntity updatedDelivery = deliveryService.updateDeliveryStatus(deliveryStatusRequest, id);
        DeliveryResponse deliveryResponse = deliveryResponseToDeliveryMapper.mapToResponse(updatedDelivery);
        return ResponseEntity.ok(deliveryResponse);
    }
}
