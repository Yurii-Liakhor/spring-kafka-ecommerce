# Spring Kafka E-Commerce

A pet project demonstrating a microservice e-commerce architecture with Apache Kafka as the backbone
for inter-service communication. The project implements the **Choreography-based Saga** pattern for
distributed transaction management and the **Outbox Pattern** to guarantee reliable event delivery
between services.

## Architecture

```
[Client]
   │
   ▼
[Order Service :8077]
   │  order.created ──────────────► [Inventory Service :8078]
   │                                        │
   │  ◄── inventory.out-of-stock ───────────┤
   │                                        │ inventory.reserved
   │                                        ▼
   │                               [Pay Service :8079]
   │                                        │
   │  ◄── payment.paid ─────────────────────┤
   │  ◄── payment.not-paid ─────────────────┘
```

## Microservices

### Order Service (port 8077)
The entry point of the system. Accepts orders from the client and orchestrates the full order lifecycle.

- Accepts REST requests to create an order (`POST /orders`)
- Publishes `order.created` event after an order is created
- Listens to `inventory.out-of-stock` — cancels the order when stock is insufficient
- Listens to `payment.paid` — confirms the order after successful payment
- Listens to `payment.not-paid` — cancels the order when payment is declined

**Order statuses:** `PENDING → INVENTORY_RESERVED → PAYMENT_PROCESSING → CONFIRMED / CANCELLED`

### Inventory Service (port 8078)
Manages product stock levels and reservations for orders.

- Listens to `order.created` — reserves products for the order
- Publishes `inventory.reserved` on successful reservation
- Publishes `inventory.out-of-stock` when stock is insufficient
- Manages reservations with states: `ACTIVE → CONSUMED / RELEASED`

### Pay Service (port 8079)
Handles payments through pluggable payment gateway providers.

- Listens to `inventory.reserved` — initiates the payment process
- Supports multiple providers: **Stripe**, **PayPal** (stub implementations)
- Publishes `payment.paid` on successful charge
- Publishes `payment.not-paid` on payment failure or rejection

**Payment statuses:** `PENDING → SUCCESS / FAILED`

### Shared Events
A shared Maven module containing event types and topic constants used across services:
- `OrderEvent` — order creation event
- `InventoryEvent` — inventory reservation event
- `PaymentEvent` — payment result event
- `KafkaTopics` — Kafka topic name constants

## Kafka Topics

| Topic                    | Producer          | Consumer          | Description                        |
|--------------------------|-------------------|-------------------|------------------------------------|
| `order.created`          | Order Service     | Inventory Service | A new order has been placed        |
| `order.canceled`         | Order Service     | —                 | Order has been cancelled           |
| `inventory.reserved`     | Inventory Service | Pay Service       | Products successfully reserved     |
| `inventory.out-of-stock` | Inventory Service | Order Service     | Insufficient stock for the order   |
| `inventory.restored`     | Inventory Service | —                 | Reservation released               |
| `payment.paid`           | Pay Service       | Order Service     | Payment completed successfully     |
| `payment.not-paid`       | Pay Service       | Order Service     | Payment declined or failed         |
| `shipping.created`       | —                 | —                 | Reserved for future use            |

## Choreography-based Saga

The order fulfillment flow is implemented as a **Choreography-based Saga** — a pattern for managing
distributed transactions across microservices without a central coordinator. Each service listens for
events, performs its local transaction, and publishes the next event. If any step fails, compensating
events trigger a rollback across the affected services.

### Happy path

```
[Order Service]  ──► order.created
                          │
                 [Inventory Service]  ──► inventory.reserved
                                               │
                                      [Pay Service]  ──► payment.paid
                                                              │
                                                    [Order Service]
                                                    status = CONFIRMED
```

### Compensation flows

```
Insufficient stock:
  [Inventory Service]  ──► inventory.out-of-stock
                                    │
                          [Order Service]  status = CANCELLED

Payment declined:
  [Pay Service]  ──► payment.not-paid
                           │
                 [Order Service]  status = CANCELLED
                 [Inventory Service]  reservation = RELEASED
```

Each service owns its own aggregate and reacts only to events it cares about — there is no central
saga orchestrator. `order-service` is the **saga initiator** and also the final consumer that closes
the transaction by setting the terminal order status (`CONFIRMED` or `CANCELLED`).

## Outbox Pattern

The Outbox Pattern is implemented to guarantee reliable event delivery to Kafka. It solves the
**dual-write problem** — where a DB change and a Kafka publish can diverge on failure.

### How it works

```
[Service Business Logic]
        │
        ▼ (single transaction)
┌───────────────────────┐
│   DB Transaction      │
│  ┌─────────────────┐  │
│  │  Domain Entity  │  │   ──► (save Order / Payment / etc.)
│  └─────────────────┘  │
│  ┌─────────────────┐  │
│  │  OutboxEvent    │  │   ──► (save serialized event)
│  └─────────────────┘  │
└───────────────────────┘
        │
        ▼ (separate thread, @Scheduled)
[OutboxPoller]
  1. SELECT top 100 WHERE published = false ORDER BY created_at
  2. Deserialize payload → event object
  3. kafkaTemplate.send(topic, event)
  4. UPDATE outbox SET published = true WHERE id = ?
```

### `outbox` table structure

| Column         | Type      | Description                              |
|----------------|-----------|------------------------------------------|
| `id`           | BIGINT    | Primary key                              |
| `aggregate_id` | VARCHAR   | Order / payment UUID                     |
| `topic`        | VARCHAR   | Target Kafka topic                       |
| `payload`      | TEXT      | JSON-serialized event                    |
| `event_class`  | VARCHAR   | Full class name for deserialization      |
| `published`    | BOOLEAN   | Whether the event has been sent to Kafka |
| `created_at`   | TIMESTAMP | Event creation timestamp                 |

`OutboxPoller` runs every second (`outbox.polling-interval-ms=1000`) in each service.

## Tech Stack

| Technology           | Version | Purpose                              |
|----------------------|---------|--------------------------------------|
| Java                 | 21      | Language                             |
| Spring Boot          | 4.0.3   | Core framework                       |
| Apache Kafka         | —       | Async inter-service communication    |
| Spring Kafka         | —       | Kafka producer/consumer integration  |
| Spring Data JPA      | —       | Data persistence                     |
| H2 Database          | —       | In-memory DB (per service)           |
| Confluent Kafka      | 7.4.4   | Docker images for Kafka + Zookeeper  |
| Lombok               | —       | Boilerplate reduction                |
| Maven (multi-module) | —       | Build system                         |

## Running the Project

### 1. Start Kafka

```bash
cd docker/kafka
docker-compose up -d
```

Kafka will be available at `localhost:29092`.

### 2. Start the services

```bash
# From the project root
mvn clean install

# Run each service separately
cd order-service && mvn spring-boot:run
cd inventory-service && mvn spring-boot:run
cd pay-service && mvn spring-boot:run
```

### 3. Place an order

```bash
curl -X POST http://localhost:8077/orders \
  -H "Content-Type: application/json" \
  -d '{
    "items": [
      { "productId": 1, "quantity": 2 }
    ],
    "paymentData": {
      "amount": 9999,
      "currency": "USD",
      "paymentMethod": "card",
      "provider": "STRIPE"
    }
  }'
```

## Project Structure

```
spring-kafka-ecommerce/
├── order-service/          # Order management service
├── inventory-service/      # Stock & reservation service
├── pay-service/            # Payment processing service
├── shared-events/          # Shared DTOs and Kafka topic constants
├── docker/
│   └── kafka/              # Docker Compose for Kafka + Zookeeper
└── pom.xml                 # Root Maven POM (multi-module)
```