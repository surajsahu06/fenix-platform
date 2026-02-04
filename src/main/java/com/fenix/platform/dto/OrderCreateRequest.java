package com.fenix.platform.dto;

import com.fenix.platform.model.FinancialStatus;
import com.fenix.platform.model.FulfillmentOverallStatus;
import com.fenix.platform.model.OrderStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.Data;

@Data
public class OrderCreateRequest {
    @NotNull
    private UUID orgId;

    @NotNull
    private UUID websiteId;

    @NotBlank
    private String externalOrderId;

    private String externalOrderNumber;

    private OrderStatus status;

    private FinancialStatus financialStatus;

    private FulfillmentOverallStatus fulfillmentStatus;

    private String customerEmail;

    private BigDecimal orderTotal;

    private String currency;

    private OffsetDateTime orderCreatedAt;

    private OffsetDateTime orderUpdatedAt;
}
