package com.fenix.platform.mapper;

import com.fenix.platform.dto.TrackingCreateRequest;
import com.fenix.platform.dto.TrackingPatchRequest;
import com.fenix.platform.dto.TrackingResponse;
import com.fenix.platform.dto.TrackingUpdateRequest;
import com.fenix.platform.entity.Fulfillment;
import com.fenix.platform.entity.Tracking;
import com.fenix.platform.model.TrackingStatus;

public final class TrackingMapper {
    private TrackingMapper() {
    }

    public static TrackingResponse toResponse(Tracking entity) {
        return TrackingResponse.builder()
                .id(entity.getId())
                .fulfillmentId(entity.getFulfillment().getId())
                .trackingNumber(entity.getTrackingNumber())
                .carrier(entity.getCarrier())
                .trackingUrl(entity.getTrackingUrl())
                .status(entity.getStatus())
                .isPrimary(entity.isPrimary())
                .lastEventAt(entity.getLastEventAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static void applyCreate(Tracking entity, Fulfillment fulfillment, TrackingCreateRequest request) {
        entity.setFulfillment(fulfillment);
        entity.setOrganization(fulfillment.getOrganization());
        entity.setTrackingNumber(request.getTrackingNumber());
        entity.setCarrier(request.getCarrier());
        entity.setTrackingUrl(request.getTrackingUrl());
        entity.setStatus(request.getStatus() != null ? request.getStatus() : TrackingStatus.UNKNOWN);
        entity.setPrimary(request.getIsPrimary() != null ? request.getIsPrimary() : Boolean.FALSE);
        entity.setLastEventAt(request.getLastEventAt());
    }

    public static void applyUpdate(Tracking entity, Fulfillment fulfillment, TrackingUpdateRequest request) {
        entity.setFulfillment(fulfillment);
        entity.setOrganization(fulfillment.getOrganization());
        entity.setTrackingNumber(request.getTrackingNumber());
        entity.setCarrier(request.getCarrier());
        entity.setTrackingUrl(request.getTrackingUrl());
        entity.setStatus(request.getStatus() != null ? request.getStatus() : TrackingStatus.UNKNOWN);
        entity.setPrimary(request.getIsPrimary() != null ? request.getIsPrimary() : Boolean.FALSE);
        entity.setLastEventAt(request.getLastEventAt());
    }

    public static void applyPatch(Tracking entity, TrackingPatchRequest request) {
        if (request.getCarrier() != null) {
            entity.setCarrier(request.getCarrier());
        }
        if (request.getTrackingUrl() != null) {
            entity.setTrackingUrl(request.getTrackingUrl());
        }
        if (request.getStatus() != null) {
            entity.setStatus(request.getStatus());
        }
        if (request.getIsPrimary() != null) {
            entity.setPrimary(request.getIsPrimary());
        }
        if (request.getLastEventAt() != null) {
            entity.setLastEventAt(request.getLastEventAt());
        }
    }
}
