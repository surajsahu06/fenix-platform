package com.fenix.platform.controller;

import com.fenix.platform.dto.ErrorResponse;
import com.fenix.platform.dto.FulfillmentCreateRequest;
import com.fenix.platform.dto.FulfillmentPatchRequest;
import com.fenix.platform.dto.FulfillmentResponse;
import com.fenix.platform.dto.FulfillmentUpdateRequest;
import com.fenix.platform.dto.PagedResponse;
import com.fenix.platform.model.FulfillmentStatus;
import com.fenix.platform.service.FulfillmentService;
import jakarta.validation.Valid;

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
@RequestMapping("/orders/{orderId}/fulfillments")
@RequiredArgsConstructor
@Validated
@Tag(name = "Fulfillments", description = "Manage fulfillment records for an order.")
public class FulfillmentController {
    private final FulfillmentService service;

    @PostMapping
    @Operation(summary = "Create fulfillment", description = "Creates a fulfillment under the specified order.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Fulfillment created",
                    content = @Content(schema = @Schema(implementation = FulfillmentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Order not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<FulfillmentResponse> create(@PathVariable UUID orderId,
                                                      @Valid @RequestBody FulfillmentCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(orderId, request));
    }

    @GetMapping
    @Operation(summary = "List fulfillments", description = "Lists fulfillments for an order with filtering, pagination, and sorting.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Fulfillments retrieved",
                    content = @Content(schema = @Schema(implementation = PagedResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid query parameters",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public PagedResponse<FulfillmentResponse> list(
            @PathVariable UUID orderId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
            @RequestParam(required = false) FulfillmentStatus status,
            @RequestParam(required = false) String carrier,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort) {
        return service.list(orderId, from, to, status, carrier, page, size, sort);
    }

    @GetMapping("/search")
    @Operation(summary = "Search fulfillments", description = "Searches fulfillments by external fulfillment ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Fulfillments retrieved",
                    content = @Content(schema = @Schema(implementation = PagedResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid query parameters",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public PagedResponse<FulfillmentResponse> search(
            @PathVariable UUID orderId,
            @RequestParam String externalFulfillmentId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return service.search(orderId, externalFulfillmentId, page, size);
    }

    @GetMapping("/{fulfillmentId}")
    @Operation(summary = "Get fulfillment", description = "Retrieves a single fulfillment by ID for the order.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Fulfillment retrieved",
                    content = @Content(schema = @Schema(implementation = FulfillmentResponse.class))),
            @ApiResponse(responseCode = "404", description = "Fulfillment or order not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public FulfillmentResponse get(@PathVariable UUID orderId, @PathVariable UUID fulfillmentId) {
        return service.get(orderId, fulfillmentId);
    }

    @PutMapping("/{fulfillmentId}")
    @Operation(summary = "Update fulfillment", description = "Replaces fulfillment fields with provided values.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Fulfillment updated",
                    content = @Content(schema = @Schema(implementation = FulfillmentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Fulfillment or order not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public FulfillmentResponse update(@PathVariable UUID orderId, @PathVariable UUID fulfillmentId,
                                      @Valid @RequestBody FulfillmentUpdateRequest request) {
        return service.update(orderId, fulfillmentId, request);
    }

    @PatchMapping("/{fulfillmentId}")
    @Operation(summary = "Patch fulfillment", description = "Updates one or more fulfillment fields.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Fulfillment updated",
                    content = @Content(schema = @Schema(implementation = FulfillmentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Fulfillment or order not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public FulfillmentResponse patch(@PathVariable UUID orderId, @PathVariable UUID fulfillmentId,
                                     @RequestBody FulfillmentPatchRequest request) {
        return service.patch(orderId, fulfillmentId, request);
    }

    @DeleteMapping("/{fulfillmentId}")
    @Operation(summary = "Delete fulfillment", description = "Deletes a fulfillment by ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Fulfillment deleted"),
            @ApiResponse(responseCode = "404", description = "Fulfillment or order not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> delete(@PathVariable UUID orderId, @PathVariable UUID fulfillmentId) {
        service.delete(orderId, fulfillmentId);
        return ResponseEntity.noContent().build();
    }
}
