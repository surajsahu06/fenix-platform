package com.fenix.platform.dto;

import com.fenix.platform.model.TrackingStatus;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

import lombok.Data;

@Data
public class TrackingPatchRequest {
    private String carrier;
    private String trackingUrl;
    private TrackingStatus status;
    @JsonProperty("isPrimary")
    private Boolean isPrimary;
    private OffsetDateTime lastEventAt;
}
