# PayLite — Architecture & System Documentation

> A digital wallet platform where users sign up, top up a wallet, and send money
> to each other safely. Built as a portfolio project to model how a real payments
> backend handles money — atomicity, idempotency, event-driven notifications — with
> a polished React frontend on top.

- **Live frontend (Vercel):** https://paylite-web.vercel.app
- **Live API docs (Swagger on AWS):** http://paylite-env.eba-bgpzdh2n.ap-south-1.elasticbeanstalk.com/swagger-ui.html
- **Source:** https://github.com/MayankHarnotiya/payLite

---

## 1. What We Built (in one minute)

PayLite is a small but production-shaped **peer-to-peer payments system**. It is split
into independent pieces that each do one job:

| Piece | What it is | What it does |
|-------|-----------|--------------|
| **wallet-service** | Spring Boot REST API | The brain: auth, wallets, transfers, transaction history. Owns the database. |
| **notification-service** | Spring Boot Kafka consumer | Reacts to completed transfers (e.g. "you received ₹500") asynchronously. |
| **paylite-events** | Shared Java library | The contract (event classes + topic names) both services agree on. |
| **paylite-web** | React 19 + TypeScript SPA | The user-facing app — dashboard, transfers, history, live notifications. |

The backend is a **Maven multi-module monorepo** (one `pom.xml` parent, three Java
modules). The frontend is a separate Vite project in the same Git repo.

```
payLite/
├── pom.xml                 ← parent (ties the 3 Java modules together)
├── docker-compose.yml      ← local infra: MySQL + Redis + Kafka + UIs
├── Dockerfile              ← builds the wallet-service production image
│
├── paylite-events/         ← shared event contract (TransferCompletedEvent, topics)
├── wallet-service/         ← main REST API (owns MySQL + Redis)
├── notification-service/   ← Kafka consumer (notifications)
└── paylite-web/            ← React frontend (deployed to Vercel)
```

---

## 2. The Big Picture (how it all connects)

```
                          ┌─────────────────────────────┐
                          │   Browser (paylite-web SPA)  │
                          │   React 19 · TS · Tailwind   │
                          └──────────────┬──────────────┘
                                         │ HTTPS
                                         ▼
                          ┌─────────────────────────────┐
                          │      Vercel (static host)    │
                          │  serves the SPA + rewrites   │
                          │  /api/* → Elastic Beanstalk  │
                          └──────────────┬──────────────┘
                                         │ HTTP  /api/*
                                         ▼
        ┌────────────────────────────────────────────────────────┐
        │              AWS Elastic Beanstalk (EC2)                │
        │  ┌──────────────────────────────────────────────────┐  │
        │  │              wallet-service (Spring Boot)         │  │
        │  │  Auth · Wallets · Transfers · History · Swagger   │  │
        │  └───────┬───────────────┬───────────────┬──────────┘  │
        └──────────┼───────────────┼───────────────┼─────────────┘
                   │               │               │
          JDBC     ▼        Redis  ▼     Kafka publish (fire-and-forget)
        ┌──────────────┐  ┌──────────────┐  ┌────────────────────────┐
        │  MySQL (RDS) │  │ Redis(Upstash)│  │  Kafka topic:          │
        │  source of   │  │ idempotency  │  │  paylite.transfer.     │
        │  truth ledger│  │ keys (24h)   │  │  completed             │
        └──────────────┘  └──────────────┘  └───────────┬────────────┘
                                                         │ consume
                                                         ▼
                                            ┌────────────────────────┐
                                            │  notification-service  │
                                            │  logs/sends notice      │
                                            └────────────────────────┘
```

**Two communication styles on purpose:**
- **Synchronous (REST/JDBC):** the request path the user waits on — must be correct and atomic.
- **Asynchronous (Kafka):** side-effects that shouldn't slow down or break a transfer — published *after* the DB commits, fire-and-forget.

---

## 3. The Services in Detail

### 3.1 wallet-service — the core API

The heart of the system. A layered Spring Boot 3.3.5 / Java 17 application.

**Package layout** (`com.paylite.wallet`):

| Layer | Responsibility | Key classes |
|-------|---------------|-------------|
| `controller/` | HTTP endpoints, request validation | `AuthController`, `WalletController` |
| `service/` | Business logic & rules | `TransferService`, `WalletService`, `AuthService`, `IdempotencyService`, `UserService` |
| `repository/` | Database access (Spring Data JPA) | `UserRepository`, `WalletRepository`, `TransactionRepository` |
| `entity/` | DB tables as Java objects | `User`, `Wallet`, `Transaction` |
| `dto/` | Request/response JSON shapes | `TransferRequest/Response`, `AuthResponse`, etc. |
| `security/` | JWT auth | `JwtService`, `JwtAuthenticationFilter`, `CustomUserDetailsService` |
| `config/` | Bean wiring | `SecurityConfig`, `KafkaProducerConfig`, `OpenApiConfig`, `JwtProperties` |
| `messaging/` | Kafka producer | `TransferEventPublisher` |
| `exception/` | Typed errors + one global handler | `GlobalExceptionHandler` + 8 domain exceptions |

**REST API:**

| Method | Endpoint | Auth | Purpose |
|--------|----------|------|---------|
| `POST` | `/api/auth/signup` | public | Create user (returns 201). Auto-creates an empty wallet. |
| `POST` | `/api/auth/login` | public | Verify credentials → return JWT + user info |
| `GET` | `/api/wallets/me` | JWT | Current user's wallet balance |
| `GET` | `/api/wallets/me/transactions?page=&size=` | JWT | Paginated history (sent + received) |
| `POST` | `/api/wallets/add-money` | JWT | Credit own wallet (stands in for a payment-gateway webhook) |
| `POST` | `/api/wallets/transfer` | JWT + `Idempotency-Key` header | Send money to another user |

> **Security note:** the sender/current user is **always** read from the JWT, never
> from the URL or request body — this closes IDOR (one user acting as another) holes.

**Security model:** stateless JWT.
- Passwords stored as BCrypt hashes.
- On login, `JwtService` issues a signed token (HS512, 15-min expiry, issuer `paylite-wallet-service`).
- Every protected request passes through `JwtAuthenticationFilter`, which validates the
  token and loads the user via `CustomUserDetailsService`. No server-side sessions.

### 3.2 notification-service — the async reactor

A small Spring Boot app that does **not** own a database or expose business endpoints.
It only listens to Kafka.

- `TransferCompletedListener` is a `@KafkaListener` on topic `paylite.transfer.completed`,
  consumer group `notification-service`.
- **Manual acknowledgement** (`enable-auto-commit: false`): the offset is committed only
  after `NotificationService` successfully processes the message. If processing throws, the
  message is **not** acked and gets redelivered → **at-least-once** delivery semantics.
- Scaling: because it's a consumer group, you can run multiple instances and Kafka spreads
  partitions across them.

### 3.3 paylite-events — the shared contract

A tiny library with **no logic**, depended on by both services so they can't disagree on
the message format:
- `TransferCompletedEvent` — the payload (transactionId, sender/recipient email, amount,
  currency, idempotencyKey, completedAt). It's a **fact** ("a transfer happened"), not a
  command ("send an email") — consumers decide what to do with it.
- `TransferTopics` — topic name constants.

---

## 4. The Money-Transfer Flow (the most important part)

A transfer must never lose money, double-spend, or go negative — even under retries and
concurrency. Here's how the code guarantees that.

```
Client sends POST /api/wallets/transfer
   Headers: Authorization: Bearer <JWT>, Idempotency-Key: <UUID>
   Body:    { recipientEmail, amount }
        │
        ▼
1. AUTH          JwtAuthenticationFilter validates token → sender = JWT subject
        │
        ▼
2. IDEMPOTENCY   IdempotencyService.executeIdempotent(key, …)   [Redis]
        │           • SET key=PENDING NX EX 24h  (atomic claim)
        │           • claim FAILS + value is JSON  → return cached response (no re-charge)
        │           • claim FAILS + value PENDING  → 409 ConcurrentRetry (first one still running)
        │           • claim SUCCEEDS               → we're first, continue ▼
        ▼
3. DB TRANSACTION  TransferService.performTransfer()  [one TransactionTemplate boundary]
        │           • load sender wallet, recipient user, recipient wallet
        │           • reject self-transfer (SelfTransferException)
        │           • reject insufficient balance (InsufficientBalanceException)
        │           • sender.balance -= amount ;  recipient.balance += amount
        │           • save Transaction row (status COMPLETED, unique idempotency_key)
        │           • COMMIT  (JPA dirty-checking flushes both wallet updates atomically)
        ▼
4. CACHE         store the serialized response in Redis (overwrite PENDING, 24h TTL)
        │
        ▼
5. PUBLISH       AFTER commit → TransferEventPublisher.send(TransferCompletedEvent)  [Kafka]
        │           fire-and-forget; failure is logged, transfer is NOT rolled back
        ▼
6. RESPONSE      200 OK { transactionId, amount, newSenderBalance, status, completedAt }
```

**Why this is safe — four independent guarantees:**

| Guarantee | How it's enforced |
|-----------|-------------------|
| **Atomic** | All wallet/ledger writes are in one `TransactionTemplate` boundary — commit all or roll back all. |
| **Retry-safe** | Redis idempotency key (`SET NX`). Same key + completed → cached response returned, no second charge. Unique `idempotency_key` column in MySQL is the last-line defense. |
| **Race-safe** | `wallets.version` column → JPA optimistic locking. Two concurrent transfers on the same wallet can't silently overwrite each other. |
| **Never negative** | DB-level `CHECK (balance >= 0)` on `wallets.balance` — rejects bad updates even if app logic has a bug. |

**A subtle but important design choice:** `transfer()` is deliberately **not**
`@Transactional`. The transaction boundary is opened *inside* the idempotency wrapper,
only after Redis confirms we're the first request — and the Kafka publish happens *after*
the commit, so we never announce a transfer that didn't actually persist.

---

## 5. The Database (MySQL — source of truth)

Schema is versioned with **Flyway** (`db/migration/V1…V3`), so the DB is rebuilt
identically everywhere. `ddl-auto: validate` means Hibernate checks the schema matches but
never modifies it — migrations are the only thing that changes structure.

| Table | Role | Notable columns / constraints |
|-------|------|-------------------------------|
| `users` | Accounts | `email` UNIQUE, `password_hash` (BCrypt) |
| `wallets` | Current balances (one per user) | `balance DECIMAL(15,2)`, `version` (optimistic lock), `UNIQUE(user_id)`, `CHECK(balance >= 0)` |
| `transactions` | Append-only ledger/history | `amount`, `idempotency_key` UNIQUE, `status IN (COMPLETED,FAILED,PENDING)`, `CHECK(amount > 0)`, indexed by `(sender_id, created_at)` and `(recipient_id, created_at)` |

Design principle: **`wallets` is the current state, `transactions` is the immutable
history.** Money uses `DECIMAL`, never floating point.

---

## 6. The Frontend (paylite-web)

A modern React 19 + TypeScript single-page app built with Vite and Tailwind CSS v4.

**Stack highlights:**
- **TanStack Query** — server-state, caching, and the polling that powers live updates.
- **React Hook Form + Zod** — typed forms and validation.
- **Axios** client (`lib/api-client.ts`) — auto-attaches the JWT, and on any `401` clears
  the session and redirects to `/login?session=expired`.
- **React Three Fiber / drei + Framer Motion** — the 3D hero/auth scenes and animations.
- **Sonner** — toast notifications.

**Routing** (`routes/AppRoutes.tsx`, all pages lazy-loaded):
- Public: `/` (landing), and `GuestRoute`-gated `/login`, `/register`.
- `ProtectedRoute` + `AppShell`: `/dashboard`, `/transfer`, `/history`, `/settings`.

**Live notifications without WebSockets/SSE — by design.** `useTransferWatcher` polls the
transactions endpoint every **15 seconds** via TanStack Query, tracks the highest
transaction id seen (persisted per-user in `localStorage`), and fires a toast + bell
notification for any new `RECEIVED` transfer. This is a deliberate trade-off: simple,
stateless, and works fine behind Vercel's rewrite without needing a persistent connection
to the backend.

---

## 7. How It's Deployed

| Component | Platform | Notes |
|-----------|----------|-------|
| **Frontend** | **Vercel** | Static build of `paylite-web`. |
| **API** | **AWS Elastic Beanstalk** (EC2) | Runs the wallet-service JAR. Hosts Swagger UI. |
| **Database** | **AWS RDS (MySQL 8)** | Managed, in a private VPC subnet. |
| **Redis** | **Upstash** | External, TLS-enabled — holds idempotency keys. |
| **Kafka** | local / optional | Toggle with `PAYLITE_KAFKA_ENABLED`. |

**The HTTP/HTTPS bridge (why `vercel.json` matters):** the frontend is served over HTTPS,
but Elastic Beanstalk is HTTP-only. Calling HTTP from an HTTPS page would be blocked as
mixed content. So the SPA calls **same-origin** `/api/*`, and `vercel.json` rewrites those
requests server-side to the Elastic Beanstalk URL:

```json
{ "source": "/api/:path*",
  "destination": "http://paylite-env.eba-bgpzdh2n.ap-south-1.elasticbeanstalk.com/api/:path*" }
```

The second rewrite (`/((?!assets/).*) → /index.html`) makes client-side routing work on
hard refresh. In production `VITE_API_URL` is left empty so the app uses relative `/api`.

**Build/run:** the root `Dockerfile` is a multi-stage build — Maven image compiles the
wallet-service JAR, then a slim JRE image runs it. The port is read from the `PORT` env var
(EB-friendly), defaulting to 8081.

---

## 8. Running It Locally

```bash
# 1. Start infrastructure (MySQL :3307, Redis :6379, Kafka :9092, kafka-ui :8080, phpMyAdmin :5050)
docker-compose up -d

# 2. Run the API (Flyway builds the schema on first boot)
mvn -pl wallet-service -am spring-boot:run     # → http://localhost:8081

# 3. (optional) Run the notification consumer
mvn -pl notification-service -am spring-boot:run

# 4. Run the frontend
cd paylite-web && npm install && npm run dev    # → http://localhost:5173

# Swagger UI: http://localhost:8081/swagger-ui.html
```

Sensible local defaults are baked into `application.yml`; override via env vars
(`SPRING_DATASOURCE_*`, `SPRING_DATA_REDIS_*`, `PAYLITE_JWT_SECRET`,
`SPRING_KAFKA_BOOTSTRAP_SERVERS`, `PAYLITE_KAFKA_ENABLED`).

Run the test suite (JUnit 5, Mockito, AssertJ, Testcontainers-style integration tests):

```bash
mvn clean verify
```

---

## 9. Key Engineering Decisions (the "why")

| Decision | Reasoning |
|----------|-----------|
| **Multi-module monorepo** | Shared event contract (`paylite-events`) used by two services without copy-paste; one build, one version. |
| **Idempotency in Redis, not just DB** | Sub-millisecond `SET NX` claim catches retries before they ever hit the database; the DB unique constraint is the safety net. |
| **`TransactionTemplate` over `@Transactional`** | Lets idempotency wrap the transaction and lets Kafka publish strictly *after* commit — impossible to express cleanly with method-level `@Transactional` + self-injection. |
| **Kafka publish is fire-and-forget, post-commit** | A flaky notification must never roll back or slow down a successful money transfer. |
| **Events are facts, not commands** | New consumers (SMS, analytics, push) can be added without touching wallet-service. |
| **Polling instead of WebSockets/SSE for notifications** | Stateless, trivially works behind Vercel rewrites, good enough at 15s cadence for this use case. |
| **DB-level `CHECK` constraints** | Defense in depth: the database refuses negative balances / non-positive amounts even if application code regresses. |

---

## 10. Roadmap / Known Gaps

- Notification-service currently logs/handles events; wiring real email/SMS/push delivery is the next step.
- Kafka publish uses fire-and-forget; a **transactional outbox** would give exactly-once-ish guarantees.
- No rate limiting on `/api/auth/login` yet.
- CI/CD (GitHub Actions) for automated test + deploy is planned.

---

*Maintainer: Mayank Harnotiya · Backend Engineer (Java · Spring Boot · AWS)*
