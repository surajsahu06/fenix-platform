package com.fenix.platform.mapper;

import com.fenix.platform.dto.OrderCreateRequest;
import com.fenix.platform.dto.OrderPatchRequest;
import com.fenix.platform.dto.OrderResponse;
import com.fenix.platform.dto.OrderUpdateRequest;
import com.fenix.platform.entity.Order;
import com.fenix.platform.entity.Organization;
import com.fenix.platform.entity.Website;
import com.fenix.platform.model.FinancialStatus;
import com.fenix.platform.model.FulfillmentOverallStatus;
import com.fenix.platform.model.OrderStatus;

import java.math.BigDecimal;

public final class OrderMapper {
    private static final BigDecimal ZERO_AMOUNT = new BigDecimal("0.00");

    private OrderMapper() {
    }

    public static OrderResponse toResponse(Order entity) {
        return OrderResponse.builder()
                .id(entity.getId())
                .orgId(entity.getOrganization().getId())
                .websiteId(entity.getWebsite().getId())
                .externalOrderId(entity.getExternalOrderId())
                .externalOrderNumber(entity.getExternalOrderNumber())
                .status(entity.getStatus())
                .financialStatus(entity.getFinancialStatus())
                .fulfillmentStatus(entity.getFulfillmentStatus())
                .customerEmail(entity.getCustomerEmail())
                .orderTotal(entity.getOrderTotal())
                .currency(entity.getCurrency())
                .orderCreatedAt(entity.getOrderCreatedAt())
                .orderUpdatedAt(entity.getOrderUpdatedAt())
                .ingestedAt(entity.getIngestedAt())
                .createdAt(entity.getIngestedAt())
                .updatedAt(entity.getOrderUpdatedAt() != null ? entity.getOrderUpdatedAt() : entity.getIngestedAt())
                .build();
    }

    public static void applyCreate(Order entity, Organization organization, Website website, OrderCreateRequest request) {
        entity.setOrganization(organization);
        entity.setWebsite(website);
        entity.setExternalOrderId(request.getExternalOrderId());
        entity.setExternalOrderNumber(request.getExternalOrderNumber());
        entity.setStatus(request.getStatus() != null ? request.getStatus() : OrderStatus.CREATED);
        entity.setFinancialStatus(request.getFinancialStatus() != null ? request.getFinancialStatus() : FinancialStatus.UNKNOWN);
        entity.setFulfillmentStatus(request.getFulfillmentStatus() != null ? request.getFulfillmentStatus() : FulfillmentOverallStatus.UNKNOWN);
        entity.setCustomerEmail(request.getCustomerEmail());
        entity.setOrderTotal(request.getOrderTotal() != null ? request.getOrderTotal() : ZERO_AMOUNT);
        entity.setCurrency(request.getCurrency());
        entity.setOrderCreatedAt(request.getOrderCreatedAt());
        entity.setOrderUpdatedAt(request.getOrderUpdatedAt());
    }

    public static void applyUpdate(Order entity, Organization organization, Website website, OrderUpdateRequest request) {
        entity.setOrganization(organization);
        entity.setWebsite(website);
        entity.setExternalOrderId(request.getExternalOrderId());
        entity.setExternalOrderNumber(request.getExternalOrderNumber());
        entity.setStatus(request.getStatus() != null ? request.getStatus() : OrderStatus.CREATED);
        entity.setFinancialStatus(request.getFinancialStatus() != null ? request.getFinancialStatus() : FinancialStatus.UNKNOWN);
        entity.setFulfillmentStatus(request.getFulfillmentStatus() != null ? request.getFulfillmentStatus() : FulfillmentOverallStatus.UNKNOWN);
        entity.setCustomerEmail(request.getCustomerEmail());
        entity.setOrderTotal(request.getOrderTotal() != null ? request.getOrderTotal() : ZERO_AMOUNT);
        entity.setCurrency(request.getCurrency());
        entity.setOrderCreatedAt(request.getOrderCreatedAt());
        entity.setOrderUpdatedAt(request.getOrderUpdatedAt());
    }

    public static void applyPatch(Order entity, OrderPatchRequest request) {
        if (request.getExternalOrderNumber() != null) {
            entity.setExternalOrderNumber(request.getExternalOrderNumber());
        }
        if (request.getStatus() != null) {
            entity.setStatus(request.getStatus());
        }
        if (request.getFinancialStatus() != null) {
            entity.setFinancialStatus(request.getFinancialStatus());
        }
        if (request.getFulfillmentStatus() != null) {
            entity.setFulfillmentStatus(request.getFulfillmentStatus());
        }
        if (request.getCustomerEmail() != null) {
            entity.setCustomerEmail(request.getCustomerEmail());
        }
        if (request.getOrderTotal() != null) {
            entity.setOrderTotal(request.getOrderTotal());
        }
        if (request.getCurrency() != null) {
            entity.setCurrency(request.getCurrency());
        }
        if (request.getOrderCreatedAt() != null) {
            entity.setOrderCreatedAt(request.getOrderCreatedAt());
        }
        if (request.getOrderUpdatedAt() != null) {
            entity.setOrderUpdatedAt(request.getOrderUpdatedAt());
        }
    }
}
