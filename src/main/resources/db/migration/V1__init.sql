-- ============================================================
-- Multi-tenant Orders / Fulfillment / Tracking (MySQL 8.x)
-- UUID PKs stored as BINARY(16) using UUID_TO_BIN(..., 1)
-- ============================================================
-- Requirements:
--  - MySQL 8.0.16+ for CHECK constraint enforcement
--  - InnoDB + utf8mb4
-- ============================================================

-- Optional: create and use a database
-- CREATE DATABASE IF NOT EXISTS logistics_platform
--   CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
-- USE logistics_platform;

SET
sql_mode = 'STRICT_TRANS_TABLES,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';
SET
time_zone = '+00:00';

-- ------------------------------------------------------------
-- Drop in dependency order (safe reset)
-- ------------------------------------------------------------
DROP TABLE IF EXISTS tracking_events;
DROP TABLE IF EXISTS tracking;
DROP TABLE IF EXISTS fulfillments;
DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS store;
DROP TABLE IF EXISTS tenant;

-- ------------------------------------------------------------
-- tenant
-- ------------------------------------------------------------
CREATE TABLE tenant
(
    tenant_id   BINARY(16) NOT NULL,
    tenant_name VARCHAR(255) NOT NULL,
    status      ENUM('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (tenant_id),
    UNIQUE KEY uk_tenant_name (tenant_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ------------------------------------------------------------
-- store
-- ------------------------------------------------------------
CREATE TABLE store
(
    store_id   BINARY(16) NOT NULL,
    tenant_id  BINARY(16) NOT NULL,
    store_code VARCHAR(100) NOT NULL,
    store_name VARCHAR(255) NOT NULL,
    platform   ENUM('SHOPIFY','NETSUITE','CUSTOM','MAGENTO','OTHER') NOT NULL DEFAULT 'OTHER',
    timezone   VARCHAR(64) NULL,
    currency   CHAR(3) NULL,
    status     ENUM('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (store_id),
    UNIQUE KEY uk_store_code_per_tenant (tenant_id, store_code),
    KEY        idx_store_tenant (tenant_id),

    CONSTRAINT fk_store_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenant (tenant_id)
            ON DELETE RESTRICT ON UPDATE CASCADE,

    CONSTRAINT chk_store_currency
        CHECK (currency IS NULL OR currency REGEXP '^[A-Z]{3}$'
)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ------------------------------------------------------------
-- orders
-- ------------------------------------------------------------
CREATE TABLE orders
(
    order_id              BINARY(16) NOT NULL,
    tenant_id             BINARY(16) NOT NULL,
    store_id              BINARY(16) NOT NULL,

    external_order_id     VARCHAR(128)   NOT NULL,
    external_order_number VARCHAR(128) NULL,

    order_status          ENUM('CREATED','CANCELLED','CLOSED') NOT NULL DEFAULT 'CREATED',
    financial_status      ENUM('UNKNOWN','PENDING','PAID','PARTIALLY_PAID','REFUNDED','PARTIALLY_REFUNDED','VOIDED')
    NOT NULL DEFAULT 'UNKNOWN',
    fulfillment_status    ENUM('UNFULFILLED','PARTIAL','FULFILLED','CANCELLED','UNKNOWN')
    NOT NULL DEFAULT 'UNKNOWN',

    customer_email        VARCHAR(320) NULL,

    order_total_amount    DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    currency              CHAR(3) NULL,

    order_created_at      DATETIME NULL,
    order_updated_at      DATETIME NULL,
    ingested_at           TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,

    raw_payload_json      JSON NULL,

    PRIMARY KEY (order_id),

    UNIQUE KEY uk_order_external (tenant_id, store_id, external_order_id),
    KEY                   idx_orders_tenant_updated (tenant_id, order_updated_at),
    KEY                   idx_orders_store_updated (store_id, order_updated_at),
    KEY                   idx_orders_tenant_number (tenant_id, external_order_number),

    CONSTRAINT fk_orders_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenant (tenant_id)
            ON DELETE RESTRICT ON UPDATE CASCADE,

    CONSTRAINT fk_orders_store
        FOREIGN KEY (store_id) REFERENCES store (store_id)
            ON DELETE RESTRICT ON UPDATE CASCADE,

    CONSTRAINT chk_orders_currency
        CHECK (currency IS NULL OR currency REGEXP '^[A-Z]{3}$'
) ,

  CONSTRAINT chk_orders_amount
    CHECK (order_total_amount >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ------------------------------------------------------------
-- order_items (optional but recommended)
-- ------------------------------------------------------------
CREATE TABLE order_items
(
    order_item_id         BINARY(16) NOT NULL,
    tenant_id             BINARY(16) NOT NULL,
    order_id              BINARY(16) NOT NULL,

    external_line_item_id VARCHAR(128) NULL,
    sku                   VARCHAR(128) NULL,
    title                 VARCHAR(512) NULL,

    quantity_ordered      INT UNSIGNED NOT NULL DEFAULT 0,
    unit_price            DECIMAL(12, 2) NOT NULL DEFAULT 0.00,

    created_at            TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (order_item_id),
    UNIQUE KEY uk_order_line (tenant_id, order_id, external_line_item_id),
    KEY                   idx_items_tenant_order (tenant_id, order_id),
    KEY                   idx_items_sku (tenant_id, sku),

    CONSTRAINT fk_order_items_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenant (tenant_id)
            ON DELETE RESTRICT ON UPDATE CASCADE,

    CONSTRAINT fk_order_items_order
        FOREIGN KEY (order_id) REFERENCES orders (order_id)
            ON DELETE CASCADE ON UPDATE CASCADE,

    CONSTRAINT chk_items_qty
        CHECK (quantity_ordered >= 0),

    CONSTRAINT chk_items_price
        CHECK (unit_price >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ------------------------------------------------------------
-- fulfillments
-- ------------------------------------------------------------
CREATE TABLE fulfillments
(
    fulfillment_id          BINARY(16) NOT NULL,
    tenant_id               BINARY(16) NOT NULL,
    order_id                BINARY(16) NOT NULL,

    external_fulfillment_id VARCHAR(128) NOT NULL,

    fulfillment_status      ENUM('CREATED','SHIPPED','DELIVERED','CANCELLED','FAILED','UNKNOWN')
    NOT NULL DEFAULT 'UNKNOWN',

    carrier                 VARCHAR(64) NULL,
    service_level           VARCHAR(64) NULL,
    ship_from_location      VARCHAR(255) NULL,

    shipped_at              DATETIME NULL,
    delivered_at            DATETIME NULL,

    created_at              TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    raw_payload_json        JSON NULL,

    PRIMARY KEY (fulfillment_id),

    UNIQUE KEY uk_fulfillment_external (tenant_id, order_id, external_fulfillment_id),
    KEY                     idx_fulfillments_tenant_order (tenant_id, order_id),
    KEY                     idx_fulfillments_tenant_updated (tenant_id, updated_at),

    CONSTRAINT fk_fulfillments_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenant (tenant_id)
            ON DELETE RESTRICT ON UPDATE CASCADE,

    CONSTRAINT fk_fulfillments_order
        FOREIGN KEY (order_id) REFERENCES orders (order_id)
            ON DELETE CASCADE ON UPDATE CASCADE,

    CONSTRAINT chk_ship_delivery_order
        CHECK (delivered_at IS NULL OR shipped_at IS NULL OR delivered_at >= shipped_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ------------------------------------------------------------
-- tracking
-- ------------------------------------------------------------
CREATE TABLE tracking
(
    tracking_id     BINARY(16) NOT NULL,
    tenant_id       BINARY(16) NOT NULL,
    fulfillment_id  BINARY(16) NOT NULL,

    tracking_number VARCHAR(128) NOT NULL,
    tracking_url    VARCHAR(1024) NULL,
    carrier         VARCHAR(64) NULL,

    tracking_status ENUM('LABEL_CREATED','IN_TRANSIT','OUT_FOR_DELIVERY','DELIVERED','EXCEPTION','UNKNOWN')
    NOT NULL DEFAULT 'UNKNOWN',

    is_primary      BOOLEAN      NOT NULL DEFAULT FALSE,
    last_event_at   DATETIME NULL,

    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (tracking_id),

    -- If you can have same tracking number across carriers, change to (tenant_id, carrier, tracking_number)
    UNIQUE KEY uk_tracking_number (tenant_id, tracking_number),
    KEY             idx_tracking_tenant_fulfillment (tenant_id, fulfillment_id),
    KEY             idx_tracking_tenant_status (tenant_id, tracking_status),

    CONSTRAINT fk_tracking_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenant (tenant_id)
            ON DELETE RESTRICT ON UPDATE CASCADE,

    CONSTRAINT fk_tracking_fulfillment
        FOREIGN KEY (fulfillment_id) REFERENCES fulfillments (fulfillment_id)
            ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ------------------------------------------------------------
-- tracking_events (idempotent via event_hash)
-- ------------------------------------------------------------
CREATE TABLE tracking_events
(
    tracking_event_id BINARY(16) NOT NULL,
    tenant_id         BINARY(16) NOT NULL,
    tracking_id       BINARY(16) NOT NULL,

    event_time        DATETIME    NOT NULL,
    event_code        VARCHAR(64) NOT NULL,
    event_description VARCHAR(512) NULL,

    event_city        VARCHAR(128) NULL,
    event_state       VARCHAR(128) NULL,
    event_country     VARCHAR(128) NULL,
    event_zip         VARCHAR(32) NULL,

    source            ENUM('CARRIER','SHOPIFY','FENIX','OTHER') NOT NULL DEFAULT 'OTHER',

    -- App-generated SHA-256 hex: 64 chars
    event_hash        CHAR(64)    NOT NULL,

    created_at        TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (tracking_event_id),

    UNIQUE KEY uk_event_hash (tenant_id, event_hash),
    KEY               idx_events_tenant_tracking_time (tenant_id, tracking_id, event_time),

    CONSTRAINT fk_events_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenant (tenant_id)
            ON DELETE RESTRICT ON UPDATE CASCADE,

    CONSTRAINT fk_events_tracking
        FOREIGN KEY (tracking_id) REFERENCES tracking (tracking_id)
            ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ============================================================
-- 2) SAMPLE UPSERT STATEMENTS
-- ============================================================

-- ------------------------------------------------------------
-- A) ORDER UPDATED UPSERT
-- Natural key: (tenant_id, store_id, external_order_id)
-- Note: order_id is generated for inserts only.
-- ------------------------------------------------------------
-- Parameters (in order):
--  tenant_uuid_str, store_uuid_str, external_order_id, external_order_number,
--  order_status, financial_status, fulfillment_status,
--  customer_email, order_total_amount, currency,
--  order_created_at, order_updated_at, raw_payload_json
--
-- Example execution:
-- INSERT ... VALUES (UUID_TO_BIN(UUID(),1), UUID_TO_BIN(? ,1), UUID_TO_BIN(? ,1), ...)
--
-- Recommended: after UPSERT, SELECT order_id back using the natural key.
-- ------------------------------------------------------------
/*
INSERT INTO orders (
  order_id,
  tenant_id, store_id,
  external_order_id, external_order_number,
  order_status, financial_status, fulfillment_status,
  customer_email,
  order_total_amount, currency,
  order_created_at, order_updated_at,
  raw_payload_json
) VALUES (
  UUID_TO_BIN(UUID(), 1),
  UUID_TO_BIN(?, 1), UUID_TO_BIN(?, 1),
  ?, ?,
  ?, ?, ?,
  ?,
  ?, ?,
  ?, ?,
  ?
)
ON DUPLICATE KEY UPDATE
  external_order_number = VALUES(external_order_number),
  order_status         = VALUES(order_status),
  financial_status     = VALUES(financial_status),
  fulfillment_status   = VALUES(fulfillment_status),
  customer_email       = VALUES(customer_email),
  order_total_amount   = VALUES(order_total_amount),
  currency             = VALUES(currency),
  order_created_at     = COALESCE(orders.order_created_at, VALUES(order_created_at)),
  order_updated_at     = GREATEST(COALESCE(orders.order_updated_at, '1970-01-01'),
                                 COALESCE(VALUES(order_updated_at), '1970-01-01')),
  raw_payload_json     = VALUES(raw_payload_json),
  ingested_at          = CURRENT_TIMESTAMP;

-- Fetch internal order_id (as UUID string) after upsert:
SELECT BIN_TO_UUID(order_id, 1) AS order_uuid
FROM orders
WHERE tenant_id = UUID_TO_BIN(?, 1)
  AND store_id  = UUID_TO_BIN(?, 1)
  AND external_order_id = ?;
*/

-- ------------------------------------------------------------
-- B) TRACKING EVENT INGESTION (idempotent)
-- Uses unique (tenant_id, event_hash)
-- ------------------------------------------------------------
-- Parameters:
--  tenant_uuid_str, tracking_uuid_str,
--  event_time, event_code, event_description,
--  event_city, event_state, event_country, event_zip,
--  source, event_hash
--
-- Note: ON DUPLICATE does nothing (idempotent)
-- ------------------------------------------------------------
/*
INSERT INTO tracking_events (
  tracking_event_id,
  tenant_id, tracking_id,
  event_time, event_code, event_description,
  event_city, event_state, event_country, event_zip,
  source,
  event_hash
) VALUES (
  UUID_TO_BIN(UUID(), 1),
  UUID_TO_BIN(?, 1), UUID_TO_BIN(?, 1),
  ?, ?, ?,
  ?, ?, ?, ?,
  ?,
  ?
)
ON DUPLICATE KEY UPDATE
  tracking_event_id = tracking_event_id;

-- After event insert, update tracking rollup:
UPDATE tracking
SET
  last_event_at = GREATEST(COALESCE(last_event_at, '1970-01-01'), ?),
  tracking_status = ?,
  updated_at = CURRENT_TIMESTAMP
WHERE tenant_id = UUID_TO_BIN(?, 1)
  AND tracking_id = UUID_TO_BIN(?, 1);
*/

-- ============================================================
-- End of script
-- ============================================================