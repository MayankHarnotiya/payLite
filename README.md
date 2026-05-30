# 💸 PayLite

A live, full-stack digital wallet platform. Users sign up, top up their wallet, and send
money to each other safely — with real-time notifications when money arrives. Built with a
Spring Boot microservice backend, an event-driven Kafka pipeline, and a modern React
frontend. Deployed on AWS and Vercel.

🌐 **Live App (Vercel):** [paylite-web.vercel.app](https://paylite-web.vercel.app)
📖 **Live API Docs (Swagger on AWS):** [Open Swagger UI](http://paylite-env.eba-bgpzdh2n.ap-south-1.elasticbeanstalk.com/swagger-ui.html)
📦 **Stack:** Java 17 · Spring Boot 3 · MySQL · Redis · Kafka · React 19 · Docker · AWS · Vercel

---

## What It Does

PayLite is a stripped-down version of what powers a payments app (PhonePe, Paytm, etc.). It handles:

- 👤 **User signup & login** with secure password storage (BCrypt) and JWT tokens
- 💰 **Wallet management** — each user gets a wallet with a balance; top up via add-money
- 💸 **Money transfers** between two users, safely (no double-spending)
- 🔁 **Retry-safe transfers** — if the network fails and the client retries, the money won't be sent twice
- 📜 **Transaction history** for each user with pagination (sent + received)
- 🔔 **Real-time notifications** — get a toast + bell alert the moment you receive money
- ⚡ **Event-driven backend** — transfers publish Kafka events consumed by a separate notification service
- 🖥️ **Polished web app** — React 19 SPA with dark mode, 3D hero scenes, and responsive mobile layout

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend Language | Java 17 |
| Backend Framework | Spring Boot 3.3.5 |
| Security | Spring Security, JWT, BCrypt |
| Database | MySQL 8 (with Flyway migrations) |
| Cache | Redis (Upstash, TLS-enabled) — idempotency keys |
| Messaging | Apache Kafka (KRaft mode) |
| Frontend | React 19 · TypeScript · Vite · Tailwind CSS v4 |
| Frontend Libraries | TanStack Query · React Hook Form · Zod · Axios · React Three Fiber · Framer Motion |
| Container | Docker (multi-stage build) |
| Cloud | AWS (Elastic Beanstalk + RDS) · Vercel |
| Testing | JUnit 5, Mockito, AssertJ |
| API Docs | Swagger / OpenAPI |

---

## Architecture

PayLite is a **Maven multi-module monorepo** (three Java modules) plus a separate React
frontend, all in one Git repository.

```
                    ┌──────────────────────────┐
                    │   paylite-web (React SPA) │   ← Vercel
                    └────────────┬─────────────┘
                                 │  /api/*  (Vercel rewrite → Elastic Beanstalk)
                                 ▼
                    ┌──────────────────────────┐
                    │      wallet-service       │   ← AWS Elastic Beanstalk
                    │  Auth · Wallets · Transfers│
                    └───┬─────────┬─────────┬───┘
              JDBC      │  Redis  │  Kafka  │ publish (after commit)
                        ▼         ▼         ▼
                  ┌─────────┐ ┌────────┐ ┌──────────────────────┐
                  │MySQL(RDS)│ │ Upstash│ │ paylite.transfer.    │
                  │ ledger  │ │ Redis  │ │ completed (topic)    │
                  └─────────┘ └────────┘ └──────────┬───────────┘
                                                    ▼ consume
                                         ┌──────────────────────┐
                                         │  notification-service │
                                         └──────────────────────┘
```

```
payLite/
├── pom.xml                 ← Parent Maven config (ties the 3 Java modules)
├── docker-compose.yml      ← Local infra: MySQL + Redis + Kafka + UIs
├── Dockerfile              ← Multi-stage production image for wallet-service
│
├── paylite-events/         ← Shared event contract (TransferCompletedEvent, topics)
├── wallet-service/         ← Main REST API — owns MySQL + Redis, publishes to Kafka
├── notification-service/   ← Kafka consumer — reacts to completed transfers
├── paylite-web/            ← React 19 frontend (deployed to Vercel)
└── docs/
    └── ARCHITECTURE.md     ← Full system & architecture documentation
```

### The Modules

| Module | What it is | Job |
|--------|-----------|-----|
| **wallet-service** | Spring Boot REST API | Auth, wallets, transfers, history. Owns the database. |
| **notification-service** | Spring Boot Kafka consumer | Reacts to completed transfers asynchronously. |
| **paylite-events** | Shared Java library | The event contract both services agree on. |
| **paylite-web** | React + TypeScript SPA | The user-facing web app. |

### wallet-service Internal Layout (`com.paylite.wallet`)

| Folder | Purpose | Example |
|--------|---------|---------|
| `controller/` | Receives HTTP requests and returns responses | `WalletController`, `AuthController` |
| `service/` | Holds the actual logic (transfers, idempotency, auth) | `TransferService.transfer()` |
| `repository/` | Talks to the database (Spring Data JPA) | `walletRepository.findByUserEmail()` |
| `entity/` | Maps database tables to Java objects | `User`, `Wallet`, `Transaction` |
| `dto/` | Data shapes for JSON requests and responses | `TransferRequest`, `TransferResponse` |
| `security/` | JWT tokens, auth filter, user details | `JwtAuthenticationFilter`, `JwtService` |
| `config/` | Wires up Spring beans (security, Kafka, OpenAPI) | `SecurityConfig`, `KafkaProducerConfig` |
| `messaging/` | Publishes events to Kafka | `TransferEventPublisher` |
| `exception/` | Custom errors + global JSON error handler | `GlobalExceptionHandler` |
| `db/migration/` | Flyway SQL files that build the schema | `V1__create_users_table.sql` |

---

## How a Transfer Works

```
1. Client sends POST /api/wallets/transfer with a JWT token and an Idempotency-Key header.
2. Idempotency check (Redis SET NX):
   - Key seen + completed → returns the cached response (no duplicate transfer).
   - Key seen + still PENDING → 409, first request is still running.
   - New key → claims it and continues processing.
3. Inside one database transaction:
   - Loads sender wallet, recipient user, recipient wallet.
   - Rejects self-transfers and insufficient balance.
   - Subtracts from sender, adds to receiver, saves a transaction record. Commit.
4. The result is cached in Redis for 24 hours (in case of retries).
5. AFTER commit, a TransferCompletedEvent is published to Kafka (fire-and-forget).
6. Response is sent back to the client.
7. notification-service consumes the event and notifies the recipient.
```

Key safety guarantees:

- **Atomic:** Either both wallets update or neither does (single transaction boundary).
- **Race-condition-safe:** A `version` column (optimistic locking) prevents concurrent transfers from corrupting balances.
- **Retry-safe:** Redis idempotency key + a unique `idempotency_key` column mean the same request can't deduct money twice.
- **Negative-balance-safe:** A database `CHECK (balance >= 0)` rejects any update that would make a balance negative — even if the application code has a bug.

> The sender is always derived from the JWT, never from the URL or request body — closing IDOR holes.

---

## Database Tables

Schema is versioned with **Flyway** (`V1`–`V3`). `wallets` holds current state; `transactions` is an append-only ledger.

| Table | Stores | Key Columns / Constraints |
|-------|--------|---------------------------|
| `users` | User accounts | `id`, `email` (unique), `password_hash` (BCrypt) |
| `wallets` | Wallet balances (one per user) | `balance DECIMAL(15,2)`, `version` (optimistic lock), `UNIQUE(user_id)`, `CHECK(balance >= 0)` |
| `transactions` | Money transfer ledger | `amount`, `idempotency_key` (unique), `status`, indexed by sender & recipient |

---

## API Endpoints

| Method | Endpoint | Auth | What It Does |
|--------|----------|------|--------------|
| `POST` | `/api/auth/signup` | public | Sign up a new user (auto-creates an empty wallet) |
| `POST` | `/api/auth/login` | public | Log in and get a JWT token |
| `GET` | `/api/wallets/me` | JWT | Get your wallet balance |
| `GET` | `/api/wallets/me/transactions?page=&size=` | JWT | View your transaction history (paginated) |
| `POST` | `/api/wallets/add-money` | JWT | Top up your own wallet |
| `POST` | `/api/wallets/transfer` | JWT + `Idempotency-Key` header | Send money to another user |

Full request/response examples are in the [Swagger UI](http://paylite-env.eba-bgpzdh2n.ap-south-1.elasticbeanstalk.com/swagger-ui.html).

---

## Getting Started Locally

### Prerequisites
- Java 17
- Maven 3.9+
- Docker
- Node.js 20+ (for the frontend)

### Steps

```bash
# 1. Clone the repo
git clone https://github.com/MayankHarnotiya/payLite.git
cd payLite

# 2. Start infrastructure (MySQL :3307, Redis :6379, Kafka :9092, kafka-ui :8080, phpMyAdmin :5050)
docker-compose up -d

# 3. Run the API (Flyway builds the schema on first boot)
mvn -pl wallet-service -am spring-boot:run        # → http://localhost:8081

# 4. (optional) Run the notification consumer
mvn -pl notification-service -am spring-boot:run  # → http://localhost:8082

# 5. Run the frontend
cd paylite-web
npm install
npm run dev                                        # → http://localhost:5173

# Swagger UI: http://localhost:8081/swagger-ui.html
```

Sensible local defaults are baked into `application.yml`. Override via env vars when needed:
`SPRING_DATASOURCE_*`, `SPRING_DATA_REDIS_*`, `PAYLITE_JWT_SECRET`,
`SPRING_KAFKA_BOOTSTRAP_SERVERS`, `PAYLITE_KAFKA_ENABLED`.

### Run Tests

```bash
mvn clean verify
```

---

## Deployment

PayLite is fully deployed:

| Component | Platform | Notes |
|-----------|----------|-------|
| **Frontend** | **Vercel** | Static build of `paylite-web`; rewrites `/api/*` to Elastic Beanstalk |
| **API** | **AWS Elastic Beanstalk** (EC2) | Runs the wallet-service JAR; hosts Swagger UI |
| **Database** | **AWS RDS (MySQL 8)** | Managed, inside a private VPC |
| **Redis** | **Upstash** | External, TLS-encrypted — holds idempotency keys |

Supporting AWS setup: **IAM roles** give EC2 access without hardcoded keys, a **VPC** keeps
EC2 and RDS on a private network, and **AWS Budgets** emails on any unexpected cost.

**The HTTP/HTTPS bridge:** the frontend is HTTPS but Elastic Beanstalk is HTTP-only. The SPA
calls same-origin `/api/*`, and `vercel.json` rewrites those requests server-side to the
Elastic Beanstalk URL — avoiding mixed-content blocking.

For the full system breakdown, see [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md).

---

## Author

**Mayank Harnotiya**
Backend Software Engineer · Java · Spring Boot · AWS

- 📧 [mayankharnotiya25@gmail.com](mailto:mayankharnotiya25@gmail.com)
- 💼 [LinkedIn](https://www.linkedin.com/in/mayankharnotiya/)
- 💻 [GitHub](https://github.com/MayankHarnotiya)

Available for full-time opportunities · Immediate joiner

---

*Built as a personal project to learn how a real payments platform handles money safely — from atomic transfers to event-driven notifications to cloud deployment.*
