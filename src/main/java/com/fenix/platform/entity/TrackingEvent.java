package com.fenix.platform.entity;

import com.fenix.platform.model.TrackingEventSource;
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

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;

import java.sql.Types;

@Entity
@Table(name = "tracking_events")
@Getter
@Setter
public class TrackingEvent {
    @Id
    @UuidGenerator
    @JdbcTypeCode(Types.BINARY)
    @Column(name = "tracking_event_id", columnDefinition = "BINARY(16)", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", columnDefinition = "BINARY(16)", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tracking_id", columnDefinition = "BINARY(16)", nullable = false)
    private Tracking tracking;

    @Column(name = "event_time", nullable = false)
    private OffsetDateTime eventTime;

    @Column(name = "event_code", nullable = false)
    private String eventCode;

    @Column(name = "event_description")
    private String eventDescription;

    @Column(name = "event_city")
    private String eventCity;

    @Column(name = "event_state")
    private String eventState;

    @Column(name = "event_country")
    private String eventCountry;

    @Column(name = "event_zip")
    private String eventZip;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false)
    private TrackingEventSource source;

    @Column(name = "event_hash", nullable = false, length = 64)
    private String eventHash;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now(ZoneOffset.UTC);
        }
    }
}
