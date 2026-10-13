# Grocery Store Microservices (`grocery-store-microservices`)

**Grocery Store Microservices** is a distributed, event-driven grocery management system built with  **Spring Boot 3.3***. It implements Domain-Driven Design (DDD), asynchronous inter-service communication via **RabbitMQ**, and distributed transactions using a **Choreography-based Saga Pattern** across Authentication, Product & Inventory, Order, and Supplier microservices.

---

## 🏗 System Architecture

### High-Level Design
The system follows a **Choreography-based Saga** and event-driven architecture to ensure eventual consistency across decoupled services:

```text
[ External Client ] -> [ API Gateway (8000 -> 8080) ]
                             |
   +-----------------+-------+-------+-----------------+
   |                 |               |                 |
[Auth Svc]     [Product Svc]    [Order Svc]     [Supplier Svc]
  (8081)          (8082)          (8083)            (8084)
   |                 |               |                 |
   |                 +-------+-------+-----------------+
   |                         |
   |                [ RabbitMQ (5672) ]
   |            (Topic: epicerie.main.topic)
   |
[ PostgreSQL (5432): auth_db, product_db, order_db, supplier_db ]
```

### Microservices & Ports
1. **API Gateway (`8000` host / `8080` container)**: Central entry point with Eureka service discovery routing, CORS support, and Resilience4j.
2. **Auth Service (`8081`)**: Handles user registration, authentication, JWT issuance/validation, and role-based access (`ADMIN`, `MANAGER`, `EMPLOYEE`, `CUSTOMER`).
3. **Product Service (`8082`)**: Manages the product catalog, stock levels, inventory reservations, and stock replenishment.
4. **Order Service (`8083`)**: Orchestrates the order lifecycle (`PENDING` -> `VALIDATED` / `CANCELLED`) and handles Saga events.
5. **Supplier Service (`8084`)**: Manages vendor relationships, ratings, stock supply dispatches, and low-stock alerts.
6. **Discovery Server (`8761`)**: Netflix Eureka Server for dynamic service registration and discovery.
7. **Config Server (`8888`)**: Centralized Spring Cloud Config Server (`native` profile).

---

## 🐇 RabbitMQ Event Flows

All services communicate asynchronously over the topic exchange **`epicerie.main.topic`**:

| Event | Publisher | Routing Key | Consumer Queue | Consumer Service | Description |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `OrderCreatedEvent` | `order-service` | `order.order.created` | `q.product.order.created` | `product-service` | Reserves product stock for a newly created order |
| `StockReservedEvent` | `product-service` | `inventory.stock.reserved` | `q.order.stock.reserved` | `order-service` | Confirms stock reservation; transitions order to `VALIDATED` |
| `InsufficientStockEvent` | `product-service` | `inventory.stock.insufficient` | `q.order.stock.insufficient` | `order-service` | Signals insufficient stock; transitions order to `CANCELLED` |
| `OrderCancelledEvent` | `order-service` | `order.order.cancelled` | `q.product.order.cancelled` | `product-service` | Releases reserved stock back to available inventory |
| `StockSuppliedEvent` | `supplier-service` | `supplier.stock.supplied` | `q.product.stock.supplied` | `product-service` | Increments product available stock upon supplier delivery |
| `LowStockEvent` | `product-service` | `inventory.stock.low` | `q.supplier.stock.low` | `supplier-service` | Alerts suppliers when product stock drops below threshold (`<= 10`) |

---

##  Getting Started

### Prerequisites
- **Docker & Docker Compose**
- **Java 21 JDK** (Optional, for local development)


### 1. Run with Docker Compose
Spin up PostgreSQL (with automatic multi-DB initialization), RabbitMQ, Redis, and all 7 Spring Boot microservices:
```bash
docker compose up --build -d
```

### 2. Infrastructure Dashboards
- **API Gateway:** `http://localhost:8000`
- **Eureka Dashboard:** `http://localhost:8761`
- **Config Server:** `http://localhost:8888`
- **RabbitMQ Management:** `http://localhost:15672` (`guest` / `guest`)

---

##  REST API Reference 

### Auth Service (`/api/v1/auth`)
- `POST /api/v1/auth/register` — Register a new user
- `POST /api/v1/auth/login` — Authenticate and receive a JWT token (Seeded accounts: `admin`/`admin123`, `customer1`/`password123`)
- `GET /api/v1/auth/validate` — Validate `Authorization: Bearer <token>`

### Product Service (`/api/v1/products`)
- `GET /api/v1/products` — List all products with available & reserved stock (Pre-seeded with grocery items)
- `GET /api/v1/products/{id}` — Get product by ID
- `GET /api/v1/products/sku/{sku}` — Get product by SKU
- `POST /api/v1/products` — Create a new product with initial stock
- `PUT /api/v1/products/{id}` — Update product details
- `POST /api/v1/products/{id}/stock` — Manually adjust product stock
- `DELETE /api/v1/products/{id}` — Delete a product

### Order Service (`/api/v1/orders`)
- `POST /api/v1/orders` — Create an order (`PENDING`) and trigger the RabbitMQ reservation Saga
- `GET /api/v1/orders` — List all orders
- `GET /api/v1/orders/{id}` — Get order status & items by ID
- `GET /api/v1/orders/customer/{customerId}` — Get orders for a customer
- `PUT /api/v1/orders/{id}/cancel` — Cancel an order and publish `OrderCancelledEvent` to restore stock

### Supplier Service (`/api/v1/suppliers`)
- `GET /api/v1/suppliers` — List all suppliers (Pre-seeded)
- `GET /api/v1/suppliers/{id}` — Get supplier by ID
- `POST /api/v1/suppliers` — Create a new supplier
- `GET /api/v1/suppliers/stats/average-rating` — Get average supplier rating
- `POST /api/v1/suppliers/{id}/supply` — Supply stock for a product and publish `StockSuppliedEvent` to RabbitMQ

---


