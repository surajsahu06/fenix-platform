package com.fenix.platform.entity;

import com.fenix.platform.model.FinancialStatus;
import com.fenix.platform.model.FulfillmentOverallStatus;
import com.fenix.platform.model.OrderStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;

import java.sql.Types;

import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "orders")
@Getter
@Setter
public class Order {
    @Id
    @UuidGenerator
    @JdbcTypeCode(Types.BINARY)
    @Column(name = "order_id", columnDefinition = "BINARY(16)", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", columnDefinition = "BINARY(16)", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", columnDefinition = "BINARY(16)", nullable = false)
    private Website website;

    @Column(name = "external_order_id", nullable = false)
    private String externalOrderId;

    @Column(name = "external_order_number")
    private String externalOrderNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false)
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "financial_status", nullable = false)
    private FinancialStatus financialStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "fulfillment_status", nullable = false)
    private FulfillmentOverallStatus fulfillmentStatus;

    @Column(name = "customer_email")
    private String customerEmail;

    @Column(name = "order_total_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal orderTotal;

    @Column(name = "currency", columnDefinition = "CHAR(3)")
    private String currency;

    @Column(name = "order_created_at")
    private OffsetDateTime orderCreatedAt;

    @Column(name = "order_updated_at")
    private OffsetDateTime orderUpdatedAt;

    @Column(name = "ingested_at", nullable = false)
    private OffsetDateTime ingestedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "raw_payload_json", columnDefinition = "JSON")
    private String rawPayloadJson;

    @PrePersist
    protected void onCreate() {
        if (ingestedAt == null) {
            ingestedAt = OffsetDateTime.now(ZoneOffset.UTC);
        }
    }
}
