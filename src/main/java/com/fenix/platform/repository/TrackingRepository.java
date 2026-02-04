package com.fenix.platform.repository;

import com.fenix.platform.entity.Tracking;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TrackingRepository extends JpaRepository<Tracking, UUID>, JpaSpecificationExecutor<Tracking> {
}
