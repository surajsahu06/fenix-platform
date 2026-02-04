package com.fenix.platform.controller;

import com.fenix.platform.dto.ErrorResponse;
import com.fenix.platform.dto.OrganizationCreateRequest;
import com.fenix.platform.dto.OrganizationPatchRequest;
import com.fenix.platform.dto.OrganizationResponse;
import com.fenix.platform.dto.OrganizationUpdateRequest;
import com.fenix.platform.dto.PagedResponse;
import com.fenix.platform.model.OrgStatus;
import com.fenix.platform.service.OrganizationService;
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
@RequestMapping("/organizations")
@RequiredArgsConstructor
@Validated
@Tag(name = "Organizations", description = "Create, query, and manage tenant organizations.")
public class OrganizationController {
    private final OrganizationService service;

    @PostMapping
    @Operation(summary = "Create organization", description = "Creates a new organization (tenant) record.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Organization created",
                    content = @Content(schema = @Schema(implementation = OrganizationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<OrganizationResponse> create(@Valid @RequestBody OrganizationCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @GetMapping
    @Operation(summary = "List organizations", description = "Lists organizations with optional filtering, pagination, and sorting.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Organizations retrieved",
                    content = @Content(schema = @Schema(implementation = PagedResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid query parameters",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public PagedResponse<OrganizationResponse> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
            @RequestParam(required = false) OrgStatus status,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort) {
        return service.list(from, to, status, name, page, size, sort);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get organization", description = "Retrieves a single organization by ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Organization retrieved",
                    content = @Content(schema = @Schema(implementation = OrganizationResponse.class))),
            @ApiResponse(responseCode = "404", description = "Organization not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public OrganizationResponse get(@PathVariable UUID id) {
        return service.get(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update organization", description = "Replaces organization fields with provided values.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Organization updated",
                    content = @Content(schema = @Schema(implementation = OrganizationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Organization not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public OrganizationResponse update(@PathVariable UUID id, @Valid @RequestBody OrganizationUpdateRequest request) {
        return service.update(id, request);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Patch organization", description = "Updates one or more organization fields.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Organization updated",
                    content = @Content(schema = @Schema(implementation = OrganizationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Organization not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public OrganizationResponse patch(@PathVariable UUID id, @RequestBody OrganizationPatchRequest request) {
        return service.patch(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete organization", description = "Deletes an organization by ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Organization deleted"),
            @ApiResponse(responseCode = "404", description = "Organization not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
