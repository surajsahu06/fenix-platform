package com.fenix.platform.controller;

import com.fenix.platform.dto.ErrorResponse;
import com.fenix.platform.dto.PagedResponse;
import com.fenix.platform.dto.TrackingCreateRequest;
import com.fenix.platform.dto.TrackingPatchRequest;
import com.fenix.platform.dto.TrackingResponse;
import com.fenix.platform.dto.TrackingUpdateRequest;
import com.fenix.platform.model.TrackingStatus;
import com.fenix.platform.service.TrackingService;
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
@RequestMapping("/fulfillments/{fulfillmentId}/tracking")
@RequiredArgsConstructor
@Validated
@Tag(name = "Tracking", description = "Manage shipment tracking numbers and events.")
public class TrackingController {
    private final TrackingService service;

    @PostMapping
    @Operation(summary = "Create tracking", description = "Creates a tracking record for the specified fulfillment.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tracking created",
                    content = @Content(schema = @Schema(implementation = TrackingResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Fulfillment not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TrackingResponse> create(@PathVariable UUID fulfillmentId,
                                                   @Valid @RequestBody TrackingCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(fulfillmentId, request));
    }

    @GetMapping
    @Operation(summary = "List tracking", description = "Lists tracking records for a fulfillment with filtering, pagination, and sorting.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tracking records retrieved",
                    content = @Content(schema = @Schema(implementation = PagedResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid query parameters",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public PagedResponse<TrackingResponse> list(
            @PathVariable UUID fulfillmentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
            @RequestParam(required = false) TrackingStatus status,
            @RequestParam(required = false) String carrier,
            @RequestParam(required = false) String trackingNumber,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort) {
        return service.list(fulfillmentId, from, to, status, carrier, trackingNumber, page, size, sort);
    }

    @GetMapping("/search")
    @Operation(summary = "Search tracking", description = "Searches tracking records by tracking number and optional carrier.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tracking records retrieved",
                    content = @Content(schema = @Schema(implementation = PagedResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid query parameters",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public PagedResponse<TrackingResponse> search(
            @PathVariable UUID fulfillmentId,
            @RequestParam String trackingNumber,
            @RequestParam(required = false) String carrier,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return service.search(fulfillmentId, trackingNumber, carrier, page, size);
    }

    @GetMapping("/{trackingId}")
    @Operation(summary = "Get tracking", description = "Retrieves a single tracking record by ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tracking record retrieved",
                    content = @Content(schema = @Schema(implementation = TrackingResponse.class))),
            @ApiResponse(responseCode = "404", description = "Tracking record not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public TrackingResponse get(@PathVariable UUID fulfillmentId, @PathVariable UUID trackingId) {
        return service.get(fulfillmentId, trackingId);
    }

    @PutMapping("/{trackingId}")
    @Operation(summary = "Update tracking", description = "Replaces tracking fields with provided values.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tracking updated",
                    content = @Content(schema = @Schema(implementation = TrackingResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Tracking record not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public TrackingResponse update(@PathVariable UUID fulfillmentId, @PathVariable UUID trackingId,
                                   @Valid @RequestBody TrackingUpdateRequest request) {
        return service.update(fulfillmentId, trackingId, request);
    }

    @PatchMapping("/{trackingId}")
    @Operation(summary = "Patch tracking", description = "Updates one or more tracking fields.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tracking updated",
                    content = @Content(schema = @Schema(implementation = TrackingResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Tracking record not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public TrackingResponse patch(@PathVariable UUID fulfillmentId, @PathVariable UUID trackingId,
                                  @RequestBody TrackingPatchRequest request) {
        return service.patch(fulfillmentId, trackingId, request);
    }

    @DeleteMapping("/{trackingId}")
    @Operation(summary = "Delete tracking", description = "Deletes a tracking record by ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Tracking deleted"),
            @ApiResponse(responseCode = "404", description = "Tracking record not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> delete(@PathVariable UUID fulfillmentId, @PathVariable UUID trackingId) {
        service.delete(fulfillmentId, trackingId);
        return ResponseEntity.noContent().build();
    }
}
