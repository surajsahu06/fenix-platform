package com.fenix.platform.entity;

import com.fenix.platform.model.FulfillmentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;

import java.sql.Types;

import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "fulfillments")
@Getter
@Setter
public class Fulfillment extends AuditableEntity {
    @Id
    @UuidGenerator
    @JdbcTypeCode(Types.BINARY)
    @Column(name = "fulfillment_id", columnDefinition = "BINARY(16)", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", columnDefinition = "BINARY(16)", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", columnDefinition = "BINARY(16)", nullable = false)
    private Organization organization;

    @Column(name = "external_fulfillment_id", nullable = false)
    private String externalFulfillmentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "fulfillment_status", nullable = false)
    private FulfillmentStatus status;

    @Column(name = "carrier")
    private String carrier;

    @Column(name = "service_level")
    private String serviceLevel;

    @Column(name = "ship_from_location")
    private String shipFromLocation;

    @Column(name = "shipped_at")
    private OffsetDateTime shippedAt;

    @Column(name = "delivered_at")
    private OffsetDateTime deliveredAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "raw_payload_json", columnDefinition = "JSON")
    private String rawPayloadJson;
}
