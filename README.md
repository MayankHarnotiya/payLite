# 💸 PayLite

A simple, live digital wallet API. Users can sign up, log in, and send money to each other — built with Spring Boot, MySQL, and Redis, deployed on AWS.

🌐 **Live Demo:** [Open Swagger UI](http://Paylite-env.eba-bgpzdh2n.ap-south-1.elasticbeanstalk.com/swagger-ui.html)
📦 **Stack:** Java 17 · Spring Boot 3 · MySQL · Redis · Docker · AWS

---

## What It Does

PayLite is a backend API for a digital wallet. Think of it like a stripped-down version of what powers a payments app (PhonePe, Paytm, etc.). It handles:

- 👤 **User signup & login** with secure password storage (BCrypt) and JWT tokens
- 💰 **Wallet management** — each user gets a wallet with a balance
- 💸 **Money transfers** between two users, safely (no double-spending)
- 🔁 **Retry-safe transfers** — if the network fails and the client retries, the money won't be sent twice
- 📜 **Transaction history** for each user with pagination

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 17 |
| Framework | Spring Boot 3.3.5 |
| Security | Spring Security, JWT, BCrypt |
| Database | MySQL 8 (with Flyway migrations) |
| Cache | Redis (Upstash, TLS-enabled) |
| Container | Docker (multi-stage build) |
| Cloud | AWS (Elastic Beanstalk + RDS) |
| Testing | JUnit 5, Mockito, AssertJ |
| API Docs | Swagger / OpenAPI |

---

## Project Structure

PayLite is a Maven multi-module project. Each module has a clear job:

```
payLite/
├── pom.xml                    ← Parent Maven config
├── docker-compose.yml         ← Spins up MySQL + Redis locally
├── Dockerfile                 ← Builds the production image
│
├── wallet-service/            ← Main Spring Boot application
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/paylite/wallet/
│       │   ├── controller/    ← REST endpoints (handles HTTP requests)
│       │   ├── service/       ← Business logic (transfer rules, validation)
│       │   ├── repository/    ← Database queries (Spring Data JPA)
│       │   ├── entity/        ← Database tables as Java classes
│       │   ├── dto/           ← Request/response data shapes
│       │   ├── security/      ← JWT filter + Spring Security setup
│       │   ├── config/        ← Beans, Redis config, CORS
│       │   └── exception/     ← Custom errors + global error handler
│       ├── main/resources/
│       │   ├── application.yml
│       │   └── db/migration/  ← Flyway SQL files (V1, V2, V3...)
│       └── test/              ← Unit + integration tests
│
└── docs/
    └── DEPLOYMENT.md          ← Full AWS deployment guide
```

### What Each Folder Does

| Folder | Purpose | Example |
|--------|---------|---------|
| `controller/` | Receives HTTP requests and returns responses | `POST /api/transfers` |
| `service/` | Holds the actual logic (what happens during a transfer) | `TransferService.transfer()` |
| `repository/` | Talks to the database (read/write queries) | `walletRepository.findByUserId()` |
| `entity/` | Maps database tables to Java objects | `User`, `Wallet`, `Transaction` |
| `dto/` | Data shapes for JSON requests and responses | `TransferRequest`, `TransferResponse` |
| `security/` | Handles login, JWT tokens, and protecting endpoints | `JwtAuthFilter` |
| `config/` | Wires up Spring beans (Redis, password encoder, etc.) | `RedisConfig`, `SecurityConfig` |
| `exception/` | Catches errors and returns clean JSON responses | `GlobalExceptionHandler` |
| `db/migration/` | SQL files that build the database schema | `V1__create_users.sql` |

---

## How a Transfer Works

```
1. Client sends a transfer request with a JWT token and an idempotency key.
2. The API checks if it has seen this idempotency key before (in Redis).
   - If yes: returns the old response (no duplicate transfer).
   - If no: continues processing.
3. Inside one database transaction:
   - Locks the sender's wallet (using a version field).
   - Locks the receiver's wallet.
   - Checks if the sender has enough balance.
   - Subtracts from sender, adds to receiver, saves a transaction record.
4. The result is stored in Redis for 24 hours (in case of retries).
5. Response is sent back to the client.
```

Key safety guarantees:

- **Atomic:** Either both wallets update or neither does.
- **Race-condition-safe:** Two transfers happening at the same time can't corrupt balances.
- **Retry-safe:** Same request sent twice won't deduct money twice.
- **Negative-balance-safe:** Database rejects any update that would make a balance negative.

---

## Database Tables

There are three main tables:

| Table | Stores | Key Columns |
|-------|--------|-------------|
| `users` | User accounts | `id`, `email` (unique), `password_hash` |
| `wallets` | Wallet balances | `id`, `user_id`, `balance`, `version` |
| `transactions` | Money transfer records | `id`, `sender_wallet_id`, `receiver_wallet_id`, `amount`, `idempotency_key` |

A `CHECK (balance >= 0)` constraint on `wallets.balance` makes sure no wallet can ever go negative — even if the application code has a bug.

---

## Getting Started Locally

### Prerequisites
- Java 17
- Maven 3.9+
- Docker

### Steps

```bash
# 1. Clone the repo
git clone https://github.com/MayankHarnotiya/payLite.git
cd payLite

# 2. Start MySQL and Redis with Docker
docker-compose up -d

# 3. Set environment variables (or use a .env file)
export SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3307/paylite
export SPRING_DATASOURCE_USERNAME=paylite
export SPRING_DATASOURCE_PASSWORD=local-pwd
export SPRING_REDIS_HOST=localhost
export SPRING_REDIS_PORT=6379
export JWT_SECRET=local-dev-secret-change-in-prod

# 4. Build and run
cd wallet-service
mvn clean package -DskipTests
mvn spring-boot:run

# 5. Open Swagger UI
# http://localhost:8080/swagger-ui.html
```

### Run Tests

```bash
mvn clean verify
```

Test coverage report: `wallet-service/target/site/jacoco/index.html`

---

## API Endpoints

| Method | Endpoint | What It Does |
|--------|----------|--------------|
| `POST` | `/api/auth/register` | Sign up a new user |
| `POST` | `/api/auth/login` | Log in and get a JWT token |
| `GET` | `/api/wallets/me` | Get your wallet balance |
| `POST` | `/api/transfers` | Send money to another user |
| `GET` | `/api/transactions/me` | View your transaction history |

Full request/response examples are in the [Swagger UI](http://Paylite-env.eba-bgpzdh2n.ap-south-1.elasticbeanstalk.com/swagger-ui.html).

---

## Deployment

PayLite is deployed on AWS using:

- **Elastic Beanstalk** runs the Spring Boot JAR on an EC2 instance.
- **RDS MySQL** stores all the data.
- **Upstash Redis** (external, TLS-encrypted) handles idempotency keys.
- **IAM roles** let the EC2 instance access AWS resources without hardcoded keys.
- **VPC** keeps EC2 and RDS on a private network.
- **AWS Budgets** sends an email if there's any unexpected cost.

For step-by-step deployment instructions, see [`docs/DEPLOYMENT.md`](docs/DEPLOYMENT.md).

---

## Roadmap

- [ ] Add **Apache Kafka** for sending notifications when a transfer happens
- [ ] Set up **GitHub Actions** for automated testing and deployment
- [ ] Build a **React frontend** and deploy it on Vercel
- [ ] Add **rate limiting** on the login endpoint

---

## Author

**Mayank Harnotiya**
Backend Software Engineer · Java · Spring Boot · AWS

- 📧 [mayankharnotiya25@gmail.com](mailto:mayankharnotiya25@gmail.com)
- 💼 [LinkedIn](https://www.linkedin.com/in/mayankharnotiya/)
- 💻 [GitHub](https://github.com/MayankHarnotiya)

Available for full-time opportunities · Immediate joiner

---

*Built as a personal project to learn how a real payments API handles money safely.*
