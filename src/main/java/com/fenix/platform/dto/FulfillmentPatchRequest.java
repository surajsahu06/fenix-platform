package com.fenix.platform.dto;

import com.fenix.platform.model.FulfillmentStatus;

import java.time.OffsetDateTime;

import lombok.Data;

@Data
public class FulfillmentPatchRequest {
    private FulfillmentStatus status;
    private String carrier;
    private String serviceLevel;
    private OffsetDateTime shippedAt;
    private OffsetDateTime deliveredAt;
}
