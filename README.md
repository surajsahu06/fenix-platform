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

