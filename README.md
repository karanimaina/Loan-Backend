# Loan-Backend

Loan domain service responsible for loan origination, repayment, fees, state transitions, and integration exposure endpoints.

## Table of Contents
- Setup
- Architecture and Process Flow
- Entity Mapping
- API Endpoints
- Sample Requests and Responses
- Swagger / OpenAPI
- Run and Verify

## Setup

- Java 17+
- Maven 3.9+ (`./mvnw` included)
- Customer Service running (unless `loan.integration.stub=true`)
- Product Service running (unless `loan.integration.stub=true`)
- Kafka broker for notifications

Key config in `src/main/resources/application.yml`:

- `loan.integration.customer-service-base-url`
- `loan.integration.customer-by-id-path`
- `loan.integration.product-service-base-url`
- `loan.integration.product-by-id-path`
- `loan.integration.stub`
- `spring.cloud.stream.bindings.loan-notifications-out-0.destination`

## Architecture and Process Flow

### 1) Originate Loan
1. Client calls `POST /api/v1/loans/create`
2. Loan service fetches customer from Customer Service
3. Loan service fetches product from Product Service
4. Loan limit and product state are validated
5. Loan and (if needed) installments are persisted
6. Service/origination fee is applied per product terms
7. Loan state history is recorded
8. Notification events are published asynchronously to Kafka worker thread
9. API returns `UniversalResponse`

### 2) Repayment
1. Client calls `POST /api/loans/{loanId}/repayments`
2. Loan state and amount are validated
3. Repayment allocations are applied (installments first when installment loan)
4. Loan state is recalculated (`OPEN`/`CLOSED`)
5. Repayment or close notification is published asynchronously
6. API returns `UniversalResponse`

### 3) Scheduled Operations
1. Overdue sweep marks overdue loans and applies late fees
2. Daily fee sweep accrues daily fees on eligible loans
3. Write-off sweep marks long-overdue loans as written off
4. Relevant notifications are published asynchronously

## Entity Mapping

Core persistent entities and what they represent:

- `Loan`: primary loan aggregate (state, balances, due date, structure, links to customer/product IDs)
- `OriginatedProductTerms`: embedded product snapshot at origination time
- `LoanInstallment`: schedule rows for installment-based loans
- `LoanRepayment`: repayment records
- `RepaymentAllocation`: allocation of repayment amount to loan/installment
- `LoanFeeCharge`: service/daily/late fee charges applied to a loan
- `LoanStateHistory`: loan lifecycle trail (OPEN, OVERDUE, CLOSED, etc.)
- `ConsolidatedBillingGroup`: optional grouping for consolidated billing dates

## API Endpoints

### Loan endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/loans/create` | Originate loan |
| GET | `/api/v1/loans/{id}` | Get loan details |
| POST | `/api/v1/loans/{id}/cancel` | Cancel loan |
| POST | `/api/loans/{loanId}/repayments` | Record repayment |

### Integration endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/integration/v1/customers/{customerId}/exposure` | Customer loan exposure summary |
| GET | `/api/integration/v1/customers/{customerId}/loans` | Customer loan list summary |
| GET | `/api/integration/v1/products/{productId}/active-loans/count` | Active loans count by product |

## Sample Requests and Responses

All responses are wrapped in:

```json
{
  "status": 200,
  "message": "Success",
  "data": {}
}
```

### Create Loan

**Request** `POST /api/v1/loans/create`

```json
{
  "customerId": 1,
  "productId": 1,
  "principalAmount": 1000.00,
  "loanStructure": "LUMP_SUM",
  "billingCycleType": "INDIVIDUAL",
  "disbursementDate": "2026-04-26",
  "consolidatedBillingGroupId": null,
  "newConsolidatedGroupNextDueDate": null,
  "consolidatedGroupLabel": null
}
```

**Response**

```json
{
  "status": 201,
  "message": "Loan created",
  "data": {
    "id": 10,
    "customerId": 1,
    "productId": 1,
    "principalAmount": 1000.0000,
    "outstandingBalance": 1000.0000,
    "disbursementDate": "2026-04-26",
    "dueDate": "2026-04-28",
    "loanStructure": "LUMP_SUM",
    "billingCycleType": "INDIVIDUAL",
    "state": "OPEN",
    "cancellationReason": null,
    "consolidatedBillingGroupId": null
  }
}
```

### Get Loan

**Request** `GET /api/v1/loans/10`

**Response**

```json
{
  "status": 200,
  "message": "Success",
  "data": {
    "id": 10,
    "customerId": 1,
    "productId": 1,
    "principalAmount": 1000.0000,
    "outstandingBalance": 1000.0000,
    "disbursementDate": "2026-04-26",
    "dueDate": "2026-04-28",
    "loanStructure": "LUMP_SUM",
    "billingCycleType": "INDIVIDUAL",
    "state": "OPEN",
    "cancellationReason": null,
    "consolidatedBillingGroupId": null
  }
}
```

### Cancel Loan

**Request** `POST /api/v1/loans/10/cancel`

```json
{
  "reason": "Customer requested cancellation"
}
```

**Response**

```json
{
  "status": 200,
  "message": "Loan cancelled",
  "data": {
    "id": 10,
    "state": "CANCELLED",
    "cancellationReason": "Customer requested cancellation"
  }
}
```

### Record Repayment

**Request** `POST /api/loans/10/repayments`

```json
{
  "amount": 250.00,
  "paymentDate": "2026-04-27",
  "channel": "BANK_TRANSFER",
  "externalReference": "TXN-000123"
}
```

**Response**

```json
{
  "status": 201,
  "message": "Repayment recorded",
  "data": {
    "id": 5,
    "loanId": 10,
    "amount": 250.0000,
    "paymentDate": "2026-04-27",
    "channel": "BANK_TRANSFER",
    "externalReference": "TXN-000123"
  }
}
```

### Customer Exposure

**Request** `GET /api/integration/v1/customers/1/exposure`

**Response**

```json
{
  "status": 200,
  "message": "Success",
  "data": {
    "customerId": 1,
    "totalOutstanding": 750.0000,
    "activeLoanCount": 1
  }
}
```

### Common Error Responses

```json
{
  "status": 409,
  "message": "Principal exceeds customer loan limit",
  "data": null
}
```

```json
{
  "status": 404,
  "message": "Loan not found: 999",
  "data": null
}
```

```json
{
  "status": 500,
  "message": "An unexpected error occurred",
  "data": null
}
```

## Swagger / OpenAPI

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Run and Verify

### Start service

```bash
./mvnw spring-boot:run
```

### Verify

- Health: `http://localhost:8080/actuator/health`
- Swagger UI loads and lists endpoints
- Sample create-loan request returns `UniversalResponse`

### Run tests

```bash
./mvnw test
```
