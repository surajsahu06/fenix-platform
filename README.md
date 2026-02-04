# Fenix Platform Backend

Java Spring Boot backend for multi-organization (tenant) eCommerce order, fulfillment, and tracking workflows. The service exposes REST APIs, uses MySQL for persistence, and implements a transactional outbox for event-based processing.

**Tech Stack**
- Java 21, Spring Boot 4.x, Spring Data JPA
- MySQL 8
- Flyway migrations
- Maven

**Default Ports**
- App: `8080`
- MySQL: `3306`

**Authentication**
All endpoints are protected with Basic Auth.
- Username: `fenix`
- Password: `fenix123`

You can override via env vars `SPRING_SECURITY_USER_NAME` and `SPRING_SECURITY_USER_PASSWORD`.

## Run With Docker
```bash
docker compose up -d --build db app
```

Check logs:
```bash
docker compose logs -f app
```

Stop:
```bash
docker compose down
```

## Run Tests With Docker
```bash
docker compose run --rm tests
```

## Local Endpoint Test Runner
This repo includes a simple endpoint smoke test that exercises all API flows.

Start the app:
```bash
docker compose up -d db app
```

Run the script:
```bash
python3 scripts/test_endpoints.py
```

Results are written to:
- `reports/endpoint-test-report.json`

## Run Locally (No Docker)
You need a local MySQL instance. Configure env vars before running.
```bash
export SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/logistics_platform
export SPRING_DATASOURCE_USERNAME=root
export SPRING_DATASOURCE_PASSWORD=password
export SPRING_SECURITY_USER_NAME=fenix
export SPRING_SECURITY_USER_PASSWORD=fenix123

mvn clean spring-boot:run
```

## API Docs
Swagger UI:
- `http://localhost:8080/swagger-ui.html`

OpenAPI JSON:
- `http://localhost:8080/v3/api-docs`

## Postman Collection
Import `postman_collection_fenix_platform.json` and set collection variables:
- `baseUrl` = `http://localhost:8080`
- `username` = `fenix`
- `password` = `fenix123`

## Outbox (Event-Based Processing)
Transactional outbox rows are written on create/update/patch/delete of organizations, websites, orders, fulfillments, and tracking records.
- Table: `event_outbox`
- Polling worker runs on a fixed delay and logs published events.

Config:
```yaml
outbox:
  polling:
    enabled: true
    fixed-delay: 2000
    initial-delay: 1000
    batch-size: 50
```

## Scaling and Performance Settings
The app uses HikariCP and Hibernate batching. You can tune via env vars or `application.yml`:

```yaml
spring:
  datasource:
    hikari:
      minimum-idle: ${DB_POOL_MIN_IDLE:5}
      maximum-pool-size: ${DB_POOL_MAX:20}
      connection-timeout: ${DB_POOL_CONN_TIMEOUT_MS:30000}
      idle-timeout: ${DB_POOL_IDLE_TIMEOUT_MS:600000}
      max-lifetime: ${DB_POOL_MAX_LIFETIME_MS:1800000}
  jpa:
    open-in-view: false
    properties:
      hibernate:
        order_inserts: true
        order_updates: true
        jdbc:
          batch_size: 50
          batch_versioned_data: true

app:
  paging:
    default-size: 50
    max-size: 200
    default-sort: updatedAt,desc
```

## Tenant Isolation Behavior
Requests that reference another organization’s resources return **404** to avoid leaking cross-tenant data.

## Migrations
Flyway runs automatically on startup.
Migrations live in `src/main/resources/db/migration`.

## Request Examples
Create organization:
```bash
curl -u fenix:fenix123 -H "Content-Type: application/json" \
  -d '{"name":"Acme Org","status":"ACTIVE"}' \
  http://localhost:8080/organizations
```

Create website:
```bash
curl -u fenix:fenix123 -H "Content-Type: application/json" \
  -d '{"code":"STORE-1","name":"Main Store","platform":"SHOPIFY"}' \
  http://localhost:8080/organizations/{orgId}/websites
```

Create order:
```bash
curl -u fenix:fenix123 -H "Content-Type: application/json" \
  -d '{"orgId":"{orgId}","websiteId":"{websiteId}","externalOrderId":"EXT-100","orderTotal":42.50,"currency":"USD"}' \
  http://localhost:8080/orders
```

## Submission Notes
- This project satisfies the required CRUD flows for organizations, websites, orders, fulfillments, and tracking.
- REST layering follows Controller → Service → Repository.
- Data isolation is enforced at the organization level in service logic.
- Basic authentication is enabled for all endpoints.
- Integration tests and service unit tests are included.
- Event driven architecture using outbox pattern.

## Production-Grade Checklist
Below is a practical checklist to harden this service for production:

**Infrastructure**
1. Run multiple app instances behind a load balancer.
2. Use managed MySQL with HA + read replicas.
3. Add autoscaling based on CPU/RPS/latency.
4. Persist DB data with durable volumes/backups.

**Security**
1. Replace Basic Auth with OAuth2/JWT or mTLS.
2. Enforce TLS at the edge and between services.
3. Add request rate limiting and IP allow/deny rules.
4. Store secrets in a vault (not env vars in plain text).

**Observability**
1. Add Spring Boot Actuator endpoints.
2. Emit structured logs (JSON) with request IDs.
3. Add metrics (Prometheus/OpenTelemetry).
4. Centralize logs + traces (ELK/Datadog/etc).

**Data and Performance**
1. Add caching for read-heavy endpoints (Redis).
2. Move outbox publishing to a queue/stream (Kafka/SQS/Rabbit).
3. Add DB indexes for all high-cardinality filters.
4. Enforce pagination limits (already added).

**Reliability**
1. Health and readiness probes.
2. Graceful shutdown with request draining.
3. Circuit breakers/timeouts for external calls.
4. Zero-downtime deploys (rolling/blue‑green).

**Testing & Release**
1. Contract tests for API changes.
2. Load tests for target traffic and p95 latency.
3. CI/CD with automated migrations and rollback plan.
