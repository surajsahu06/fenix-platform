package com.fenix.platform.repository;

import com.fenix.platform.entity.Order;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface OrderRepository extends JpaRepository<Order, UUID>, JpaSpecificationExecutor<Order> {
    Optional<Order> findByOrganizationIdAndWebsiteIdAndExternalOrderId(UUID orgId, UUID websiteId, String externalOrderId);
}
