package com.fenix.platform.repository;

import com.fenix.platform.entity.OutboxEvent;
import com.fenix.platform.model.OutboxStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {
    Page<OutboxEvent> findByStatusOrderByCreatedAtAsc(OutboxStatus status, Pageable pageable);

    @Modifying
    @Query("""
            update OutboxEvent e
            set e.status = :newStatus,
                e.lockedAt = :lockedAt,
                e.lockedBy = :lockedBy,
                e.attempts = e.attempts + 1
            where e.id = :id and e.status = :expectedStatus
            """)
    int claimEvent(@Param("id") UUID id,
                   @Param("expectedStatus") OutboxStatus expectedStatus,
                   @Param("newStatus") OutboxStatus newStatus,
                   @Param("lockedAt") OffsetDateTime lockedAt,
                   @Param("lockedBy") String lockedBy);

    @Modifying
    @Query("""
            update OutboxEvent e
            set e.status = :status,
                e.publishedAt = :publishedAt,
                e.lastError = null,
                e.updatedAt = :updatedAt
            where e.id = :id
            """)
    int markPublished(@Param("id") UUID id,
                      @Param("status") OutboxStatus status,
                      @Param("publishedAt") OffsetDateTime publishedAt,
                      @Param("updatedAt") OffsetDateTime updatedAt);

    @Modifying
    @Query("""
            update OutboxEvent e
            set e.status = :status,
                e.lastError = :error,
                e.updatedAt = :updatedAt
            where e.id = :id
            """)
    int markFailed(@Param("id") UUID id,
                   @Param("status") OutboxStatus status,
                   @Param("error") String error,
                   @Param("updatedAt") OffsetDateTime updatedAt);
}
