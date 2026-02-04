package com.fenix.platform.entity;

import com.fenix.platform.model.Platform;
import com.fenix.platform.model.WebsiteStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import java.util.UUID;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;

import java.sql.Types;

@Entity
@Table(name = "store")
@Getter
@Setter
public class Website extends AuditableEntity {
    @Id
    @UuidGenerator
    @JdbcTypeCode(Types.BINARY)
    @Column(name = "store_id", columnDefinition = "BINARY(16)", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", columnDefinition = "BINARY(16)", nullable = false)
    private Organization organization;

    @Column(name = "store_code", nullable = false)
    private String code;

    @Column(name = "store_name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Platform platform;

    @Column(name = "timezone")
    private String timezone;

    @Column(name = "currency", columnDefinition = "CHAR(3)")
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WebsiteStatus status;
}
