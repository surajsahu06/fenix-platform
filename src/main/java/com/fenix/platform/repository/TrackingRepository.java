package com.fenix.platform.repository;

import com.fenix.platform.entity.Tracking;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TrackingRepository extends JpaRepository<Tracking, UUID>, JpaSpecificationExecutor<Tracking> {
    Optional<Tracking> findByIdAndOrganizationId(UUID id, UUID orgId);

    boolean existsByIdAndOrganizationId(UUID id, UUID orgId);
}
