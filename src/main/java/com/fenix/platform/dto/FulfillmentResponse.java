package com.fenix.platform.dto;

import com.fenix.platform.model.FulfillmentStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FulfillmentResponse {
    private UUID id;
    private UUID orderId;
    private String externalFulfillmentId;
    private FulfillmentStatus status;
    private String carrier;
    private String serviceLevel;
    private OffsetDateTime shippedAt;
    private OffsetDateTime deliveredAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
