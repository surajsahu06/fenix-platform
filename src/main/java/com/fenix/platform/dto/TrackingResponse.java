package com.fenix.platform.dto;

import com.fenix.platform.model.TrackingStatus;
import com.fasterxml.jackson.annotation.JsonProperty;

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
public class TrackingResponse {
    private UUID id;
    private UUID fulfillmentId;
    private String trackingNumber;
    private String carrier;
    private String trackingUrl;
    private TrackingStatus status;
    @JsonProperty("isPrimary")
    private boolean isPrimary;
    private OffsetDateTime lastEventAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
