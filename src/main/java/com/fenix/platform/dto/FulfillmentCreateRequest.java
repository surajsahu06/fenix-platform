package com.fenix.platform.dto;

import com.fenix.platform.model.FulfillmentStatus;
import jakarta.validation.constraints.NotBlank;

import java.time.OffsetDateTime;

import lombok.Data;

@Data
public class FulfillmentCreateRequest {
    @NotBlank
    private String externalFulfillmentId;

    private FulfillmentStatus status;

    private String carrier;

    private String serviceLevel;

    private OffsetDateTime shippedAt;

    private OffsetDateTime deliveredAt;
}
