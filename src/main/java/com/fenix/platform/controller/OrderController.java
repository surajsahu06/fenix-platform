package com.fenix.platform.controller;

import com.fenix.platform.dto.ErrorResponse;
import com.fenix.platform.dto.OrderCreateRequest;
import com.fenix.platform.dto.OrderPatchRequest;
import com.fenix.platform.dto.OrderResponse;
import com.fenix.platform.dto.OrderUpdateRequest;
import com.fenix.platform.dto.PagedResponse;
import com.fenix.platform.model.FinancialStatus;
import com.fenix.platform.model.FulfillmentOverallStatus;
import com.fenix.platform.model.OrderStatus;
import com.fenix.platform.service.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Validated
@Tag(name = "Orders", description = "Create and manage orders for a tenant.")
public class OrderController {
    private final OrderService service;

    @PostMapping
    @Operation(summary = "Create order", description = "Creates a new order for the specified organization and website.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Order created",
                    content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Organization or website not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody OrderCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @GetMapping
    @Operation(summary = "List orders", description = "Lists orders for an organization, optionally filtered by website and status.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Orders retrieved",
                    content = @Content(schema = @Schema(implementation = PagedResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid query parameters",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public PagedResponse<OrderResponse> list(
            @RequestParam @NotNull UUID orgId,
            @RequestParam(required = false) UUID websiteId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) FinancialStatus financialStatus,
            @RequestParam(required = false) FulfillmentOverallStatus fulfillmentStatus,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort) {
        return service.list(orgId, websiteId, from, to, status, financialStatus, fulfillmentStatus, page, size, sort);
    }

    @GetMapping("/search")
    @Operation(summary = "Search orders", description = "Searches orders by external order identifiers within an organization.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Orders retrieved",
                    content = @Content(schema = @Schema(implementation = PagedResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid query parameters",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public PagedResponse<OrderResponse> search(
            @RequestParam @NotNull UUID orgId,
            @RequestParam(required = false) UUID websiteId,
            @RequestParam(required = false) String externalOrderId,
            @RequestParam(required = false) String externalOrderNumber,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return service.search(orgId, websiteId, externalOrderId, externalOrderNumber, page, size);
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get order", description = "Retrieves a single order by ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order retrieved",
                    content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "404", description = "Order not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public OrderResponse get(@PathVariable UUID orderId) {
        return service.get(orderId);
    }

    @PutMapping("/{orderId}")
    @Operation(summary = "Update order", description = "Replaces order fields with provided values.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order updated",
                    content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Order not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public OrderResponse update(@PathVariable UUID orderId, @Valid @RequestBody OrderUpdateRequest request) {
        return service.update(orderId, request);
    }

    @PatchMapping("/{orderId}")
    @Operation(summary = "Patch order", description = "Updates one or more order fields.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order updated",
                    content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Order not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public OrderResponse patch(@PathVariable UUID orderId, @RequestBody OrderPatchRequest request) {
        return service.patch(orderId, request);
    }

    @DeleteMapping("/{orderId}")
    @Operation(summary = "Delete order", description = "Deletes an order by ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Order deleted"),
            @ApiResponse(responseCode = "404", description = "Order not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> delete(@PathVariable UUID orderId) {
        service.delete(orderId);
        return ResponseEntity.noContent().build();
    }
}
