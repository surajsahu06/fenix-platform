-- ============================================================
-- Outbox table for event-based processing
-- ============================================================

CREATE TABLE IF NOT EXISTS event_outbox (
  outbox_id BINARY(16) NOT NULL,
  aggregate_type VARCHAR(64) NOT NULL,
  aggregate_id BINARY(16) NOT NULL,
  event_type VARCHAR(128) NOT NULL,
  payload_json JSON NULL,
  status ENUM('PENDING','IN_PROGRESS','PUBLISHED','FAILED') NOT NULL DEFAULT 'PENDING',
  attempts INT UNSIGNED NOT NULL DEFAULT 0,
  last_error VARCHAR(1024) NULL,
  locked_at DATETIME NULL,
  locked_by VARCHAR(128) NULL,
  published_at DATETIME NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  PRIMARY KEY (outbox_id),
  KEY idx_outbox_status_created (status, created_at),
  KEY idx_outbox_aggregate (aggregate_type, aggregate_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
