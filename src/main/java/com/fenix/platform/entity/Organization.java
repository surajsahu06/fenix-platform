package com.fenix.platform.entity;

import com.fenix.platform.model.OrgStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;

import java.sql.Types;

@Entity
@Table(name = "tenant")
@Getter
@Setter
public class Organization extends AuditableEntity {
    @Id
    @UuidGenerator
    @JdbcTypeCode(Types.BINARY)
    @Column(name = "tenant_id", columnDefinition = "BINARY(16)", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "tenant_name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrgStatus status;
}
