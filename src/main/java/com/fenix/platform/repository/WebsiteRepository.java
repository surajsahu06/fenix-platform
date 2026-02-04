package com.fenix.platform.repository;

import com.fenix.platform.entity.Website;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface WebsiteRepository extends JpaRepository<Website, UUID>, JpaSpecificationExecutor<Website> {
    Optional<Website> findByIdAndOrganizationId(UUID id, UUID orgId);

    boolean existsByIdAndOrganizationId(UUID id, UUID orgId);
}
