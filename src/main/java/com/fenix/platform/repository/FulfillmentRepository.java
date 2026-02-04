package com.fenix.platform.repository;

import com.fenix.platform.entity.Fulfillment;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface FulfillmentRepository extends JpaRepository<Fulfillment, UUID>, JpaSpecificationExecutor<Fulfillment> {
}
