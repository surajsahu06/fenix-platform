package com.fenix.platform.controller;

import com.fenix.platform.dto.ErrorResponse;
import com.fenix.platform.dto.PagedResponse;
import com.fenix.platform.dto.WebsiteCreateRequest;
import com.fenix.platform.dto.WebsitePatchRequest;
import com.fenix.platform.dto.WebsiteResponse;
import com.fenix.platform.dto.WebsiteUpdateRequest;
import com.fenix.platform.model.Platform;
import com.fenix.platform.model.WebsiteStatus;
import com.fenix.platform.service.WebsiteService;
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
@RequestMapping("/organizations/{orgId}/websites")
@RequiredArgsConstructor
@Validated
@Tag(name = "Websites", description = "Manage websites (stores) scoped to an organization.")
public class WebsiteController {
    private final WebsiteService service;

    @PostMapping
    @Operation(summary = "Create website", description = "Creates a new website under the specified organization.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Website created",
                    content = @Content(schema = @Schema(implementation = WebsiteResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Organization not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<WebsiteResponse> create(@PathVariable UUID orgId,
                                                  @Valid @RequestBody WebsiteCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(orgId, request));
    }

    @GetMapping
    @Operation(summary = "List websites", description = "Lists websites for an organization with filtering, pagination, and sorting.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Websites retrieved",
                    content = @Content(schema = @Schema(implementation = PagedResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid query parameters",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public PagedResponse<WebsiteResponse> list(
            @PathVariable UUID orgId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
            @RequestParam(required = false) WebsiteStatus status,
            @RequestParam(required = false) Platform platform,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort) {
        return service.list(orgId, from, to, status, platform, code, page, size, sort);
    }

    @GetMapping("/search")
    @Operation(summary = "Search websites", description = "Searches websites by ID, code, or domain within an organization.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Websites retrieved",
                    content = @Content(schema = @Schema(implementation = PagedResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid query parameters",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public PagedResponse<WebsiteResponse> search(
            @PathVariable UUID orgId,
            @RequestParam(required = false) UUID websiteId,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return service.search(orgId, websiteId, code, page, size);
    }

    @GetMapping("/{websiteId}")
    @Operation(summary = "Get website", description = "Retrieves a single website by ID within an organization.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Website retrieved",
                    content = @Content(schema = @Schema(implementation = WebsiteResponse.class))),
            @ApiResponse(responseCode = "404", description = "Website or organization not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public WebsiteResponse get(@PathVariable UUID orgId, @PathVariable UUID websiteId) {
        return service.get(orgId, websiteId);
    }

    @PutMapping("/{websiteId}")
    @Operation(summary = "Update website", description = "Replaces website fields with provided values.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Website updated",
                    content = @Content(schema = @Schema(implementation = WebsiteResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Website or organization not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public WebsiteResponse update(@PathVariable UUID orgId, @PathVariable UUID websiteId,
                                  @Valid @RequestBody WebsiteUpdateRequest request) {
        return service.update(orgId, websiteId, request);
    }

    @PatchMapping("/{websiteId}")
    @Operation(summary = "Patch website", description = "Updates one or more website fields.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Website updated",
                    content = @Content(schema = @Schema(implementation = WebsiteResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Website or organization not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public WebsiteResponse patch(@PathVariable UUID orgId, @PathVariable UUID websiteId,
                                 @RequestBody WebsitePatchRequest request) {
        return service.patch(orgId, websiteId, request);
    }

    @DeleteMapping("/{websiteId}")
    @Operation(summary = "Delete website", description = "Deletes a website by ID within an organization.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Website deleted"),
            @ApiResponse(responseCode = "404", description = "Website or organization not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> delete(@PathVariable UUID orgId, @PathVariable UUID websiteId) {
        service.delete(orgId, websiteId);
        return ResponseEntity.noContent().build();
    }
}
