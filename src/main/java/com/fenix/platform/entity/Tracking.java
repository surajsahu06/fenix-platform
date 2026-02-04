package com.fenix.platform.entity;

import com.fenix.platform.model.TrackingStatus;
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

@Entity
@Table(name = "tracking")
@Getter
@Setter
public class Tracking extends AuditableEntity {
    @Id
    @UuidGenerator
    @JdbcTypeCode(Types.BINARY)
    @Column(name = "tracking_id", columnDefinition = "BINARY(16)", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fulfillment_id", columnDefinition = "BINARY(16)", nullable = false)
    private Fulfillment fulfillment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", columnDefinition = "BINARY(16)", nullable = false)
    private Organization organization;

    @Column(name = "tracking_number", nullable = false)
    private String trackingNumber;

    @Column(name = "carrier")
    private String carrier;

    @Column(name = "tracking_url")
    private String trackingUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "tracking_status", nullable = false)
    private TrackingStatus status;

    @Column(name = "is_primary", nullable = false)
    private boolean isPrimary;

    @Column(name = "last_event_at")
    private OffsetDateTime lastEventAt;
}
