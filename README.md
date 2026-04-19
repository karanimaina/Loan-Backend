# Loan-Backend

Loan domain service: origination, installments, repayments, fees, state history, repayment allocations, scheduled sweeps, and **Kafka** events for the separate Notification service. Customer, Product, and Notification domains live in their own applications; this service integrates over **HTTP** (read Customer/Product) and **Kafka** (emit loan events).

## Prerequisites

- Java 17+ (project targets 17; CI/local may use a newer JDK)
- Maven 3.9+ (or `./mvnw`)
- **Customer** and **Product** HTTP APIs reachable when `loan.integration.stub=false`
- **Kafka** broker when `loan.notifications.enabled=true` (tests disable notifications by default)

## Run locally

```bash
./mvnw spring-boot:run
```

Defaults: in-memory **H2**, JPA `ddl-auto=update`. Configure Customer/Product base URLs and Kafka in `application.properties`.

- Health: `http://localhost:8080/actuator/health`

### Remote services

| Setting | Purpose |
|--------|---------|
| `loan.integration.customer-service-base-url` | Customer service root URL |
| `loan.integration.customer-by-id-path` | URI template with `{id}` (e.g. `/api/v1/customers/{id}`) |
| `loan.integration.product-service-base-url` | Product service root URL |
| `loan.integration.product-by-id-path` | URI template with `{id}` (e.g. `/api/v1/products/{id}`) |
| `loan.integration.stub=true` | Use in-process stubs (no HTTP calls; useful for isolated dev/tests) |

**Expected JSON (illustrative):**

- Customer `GET …/customers/{id}` → `{ "id": 1, "loanLimitAmount": "50000.00" }`
- Product `GET …/products/{id}` → includes `tenureType`, `tenureValue`, `fixedTerm`, `numberOfInstallments`, `active`, and nested `feeConfiguration` compatible with `ProductFeeConfiguration` in this codebase.

## Loan API

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/loans` | Originate loan (loads customer + product from remote services, stores product **snapshot** on the loan) |
| GET | `/api/loans/{id}` | Loan details |
| POST | `/api/loans/{id}/cancel` | Cancel (when allowed) |
| POST | `/api/loans/{loanId}/repayments` | Record repayment |

DTOs: `com.ezra.loanbackend.web.dto.LoanDtos`, `RepaymentDtos`.

## Integration API (for Customer / Product services)

These endpoints exist so other services can query loan exposure and usage without duplicating loan data.

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/integration/v1/customers/{customerId}/exposure` | Total outstanding and active loan count |
| GET | `/api/integration/v1/customers/{customerId}/loans` | Loan summaries for a customer |
| GET | `/api/integration/v1/products/{productId}/active-loans/count` | Count of OPEN/OVERDUE loans for a product |

## Notifications (Kafka)

`LoanNotificationPublisher` sends JSON **`LoanNotificationMessage`** records to topic `loan.notifications` (configurable via `loan.notifications.topic`) using **`KafkaTemplate`**. The Notification service consumes from that topic.

- Disable outbound messages (e.g. tests): `loan.notifications.enabled=false`

### Spring Cloud Stream `StreamBridge` (optional)

This repository uses **`KafkaTemplate`** by default so it runs without Spring Cloud Stream on the classpath. If your platform standardizes on **Cloud Stream** and you want **`StreamBridge`**:

1. Add `spring-cloud-stream-binder-kafka` and your Spring Cloud BOM (aligned with your Boot version).
2. Define an output binding (for example `loanNotifications-out-0`) and destination topic.
3. Replace or delegate the `LoanNotificationPublisher` implementation to call `streamBridge.send("loanNotifications-out-0", MessageBuilder.withPayload(...).build())` with the same `LoanNotificationMessage` payload.

JSON serialization uses **Jackson 3** (`tools.jackson.databind.json.JsonMapper`) as configured by Spring Boot 4.

## Scheduled jobs

See `loan.sweep.*` in `application.properties` (overdue, daily fee on lump-sum loans, write-off).

## Tests

```bash
./mvnw test
```

`src/test/resources/application.properties` sets `loan.integration.stub=true` and `loan.notifications.enabled=false` so the context starts without remote services or Kafka.

## Data model note

`Loan` stores `customerId` and `productId` (external references) and an embedded **`OriginatedProductTerms`** snapshot (fees and tenure copied at origination) so fees and schedules do not depend on the Product service at runtime.
