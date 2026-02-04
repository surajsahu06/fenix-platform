package com.fenix.platform.mapper;

import com.fenix.platform.dto.FulfillmentCreateRequest;
import com.fenix.platform.dto.FulfillmentPatchRequest;
import com.fenix.platform.dto.FulfillmentResponse;
import com.fenix.platform.dto.FulfillmentUpdateRequest;
import com.fenix.platform.entity.Fulfillment;
import com.fenix.platform.entity.Order;
import com.fenix.platform.model.FulfillmentStatus;

public final class FulfillmentMapper {
    private FulfillmentMapper() {
    }

    public static FulfillmentResponse toResponse(Fulfillment entity) {
        return FulfillmentResponse.builder()
                .id(entity.getId())
                .orderId(entity.getOrder().getId())
                .externalFulfillmentId(entity.getExternalFulfillmentId())
                .status(entity.getStatus())
                .carrier(entity.getCarrier())
                .serviceLevel(entity.getServiceLevel())
                .shippedAt(entity.getShippedAt())
                .deliveredAt(entity.getDeliveredAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static void applyCreate(Fulfillment entity, Order order, FulfillmentCreateRequest request) {
        entity.setOrder(order);
        entity.setOrganization(order.getOrganization());
        entity.setExternalFulfillmentId(request.getExternalFulfillmentId());
        entity.setStatus(request.getStatus() != null ? request.getStatus() : FulfillmentStatus.CREATED);
        entity.setCarrier(request.getCarrier());
        entity.setServiceLevel(request.getServiceLevel());
        entity.setShippedAt(request.getShippedAt());
        entity.setDeliveredAt(request.getDeliveredAt());
    }

    public static void applyUpdate(Fulfillment entity, Order order, FulfillmentUpdateRequest request) {
        entity.setOrder(order);
        entity.setOrganization(order.getOrganization());
        entity.setExternalFulfillmentId(request.getExternalFulfillmentId());
        entity.setStatus(request.getStatus() != null ? request.getStatus() : FulfillmentStatus.CREATED);
        entity.setCarrier(request.getCarrier());
        entity.setServiceLevel(request.getServiceLevel());
        entity.setShippedAt(request.getShippedAt());
        entity.setDeliveredAt(request.getDeliveredAt());
    }

    public static void applyPatch(Fulfillment entity, FulfillmentPatchRequest request) {
        if (request.getStatus() != null) {
            entity.setStatus(request.getStatus());
        }
        if (request.getCarrier() != null) {
            entity.setCarrier(request.getCarrier());
        }
        if (request.getServiceLevel() != null) {
            entity.setServiceLevel(request.getServiceLevel());
        }
        if (request.getShippedAt() != null) {
            entity.setShippedAt(request.getShippedAt());
        }
        if (request.getDeliveredAt() != null) {
            entity.setDeliveredAt(request.getDeliveredAt());
        }
    }
}
