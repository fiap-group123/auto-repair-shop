# Auto Repair Shop

Backend API for an auto repair shop MVP (FIAP Tech Challenge — Phase 1).

Kotlin, Spring Boot, PostgreSQL, and tactical DDD in a layered monolith. The API covers workshop users (JWT auth), customers (CPF/CNPJ), vehicles (Brazilian plates), service orders, and the services attached to each order.

## Contents

- [Tech stack](#tech-stack)
- [Architecture](#architecture)
- [Why PostgreSQL](#why-postgresql)
- [Prerequisites](#prerequisites)
- [Getting started](#getting-started)
- [API](#api)
- [Endpoints](#endpoints)
- [Testing](#testing)
- [Code quality](#code-quality)
- [CI](#ci)
- [Configuration](#configuration)
- [Troubleshooting](#troubleshooting)

## Tech stack

| | |
|---|---|
| Language | Kotlin 2.4, JDK 17 |
| Framework | Spring Boot 4.1 (Web MVC, Security, JPA, Validation, Actuator) |
| Auth | JWT (OAuth2 resource server), BCrypt passwords |
| Database | PostgreSQL 16, Flyway migrations, Hibernate `validate` |
| API docs | SpringDoc OpenAPI / Swagger UI |
| Tests | JUnit 5, MockK, Testcontainers, Kover (min. 98% line coverage on domain, application, persistence, and HTTP adapters) |
| Quality | Detekt + ktlint, Git pre-commit hook |
| Build | Gradle 9.5 (wrapper included) |
| Runtime | Dockerfile + docker-compose (API and PostgreSQL) |

## Architecture

Four bounded contexts in a single deployable:

- **Authentication** — users, roles, login, JWT issuance
- **Customer** — customers and vehicles
- **Service Order** — OS lifecycle (received → delivered) and budget total
- **Catalog** — services requested on an OS (line items, duration, average time)

Each context follows `domain` → `application` → `infrastructure`. HTTP adapters, JWT security, and OpenAPI live under `api`.

```
src/main/kotlin/br/com/autorepairshop/
├── api/                  # Controllers, security, exception handlers, OpenAPI
├── authentication/       # Users, roles, login
├── customer/             # Customers and vehicles
├── catalog/              # Services on a service order
├── serviceorder/         # Service order lifecycle and budget
└── shared/               # AggregateRoot, UseCase, domain events
```

Schema is owned by Flyway (`src/main/resources/db/migration/`).

## Why PostgreSQL

The MVP is a relational domain: customers own vehicles, a service order belongs to one customer and one vehicle, and services (and later parts) hang off that order with money and timestamps. PostgreSQL 16 gives ACID transactions for the budget recalculation that runs in the same commit as a service change, constraints (unique plate/document, foreign keys), and `TIMESTAMPTZ` for the OS timeline. Flyway owns the schema; Hibernate only validates it. A document store would push those invariants into application code.

## Prerequisites

- **JDK 17**
- **Docker Desktop** running (engine up)
- Git

The Gradle Wrapper is in the repo (`gradlew` / `gradlew.bat`). You do not need a local Gradle install.

## Getting started

From the repository root, the full stack (API + PostgreSQL) is:

```bash
docker compose up --build
```

This builds the `Dockerfile` and starts the API on `http://localhost:8080`, Postgres on `localhost:5432`, and Mailpit (SMTP UI) on `http://localhost:8025`. Wait until `api` is up (`docker compose ps`). Swagger: http://localhost:8080/swagger-ui.html

Copy [`.env.example`](.env.example) to `.env` to override credentials and `JWT_SECRET`. Compose reads it automatically.

### API only (Gradle on the host)

```bash
docker compose up -d postgres
./gradlew bootRun
```

On Windows CMD/PowerShell use `gradlew.bat bootRun`. When you see `Tomcat started on port 8080`, the app is up.

### IDE

1. `docker compose up -d postgres`
2. Run `src/main/kotlin/br/com/autorepairshop/AutoRepairShopApplication.kt`

Do not use `bootTestRun` unless Docker is running: that profile starts Postgres via Testcontainers and fails if the engine is down.

### Stop

```bash
docker compose down
```

`docker compose down -v` also drops the database volume (local data is lost).

## API

| Resource | URL |
|---|---|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |
| Auth requests | [`http/auth.http`](http/auth.http) |
| Customer / vehicle requests | [`http/customer.http`](http/customer.http) |
| Service order / catalog requests | [`http/service-order.http`](http/service-order.http) |

Authorize Swagger (or `.http` files) with `Authorization: Bearer <accessToken>` after login. Swagger UI, OpenAPI, and `/error` are public; everything else requires a JWT except first-user registration, login, refresh, logout, and invite completion.

### Auth

1. Register the **first** user as `MANAGER` (`POST /auth/users`, public). Later staff is created the same way with a **MANAGER** Bearer token. A `CLIENT` is created only from the invite email.
2. `POST /auth/login` returns `{ "accessToken", "refreshToken", "tokenType": "Bearer", "expiresIn" }`. Access lasts **15 minutes** by default; refresh lasts **14 days**. Rotate with `POST /auth/refresh`; revoke with `POST /auth/logout`.
3. The invite link uses `INVITE_BASE_URL`. If the base has no query string, the mailer appends `/{token}` (do not log the full URL). `GET /auth/invites/{token}` previews it; `POST /auth/invites/{token}` sets the login email and password.

Ready-made flow: run [`http/auth.http`](http/auth.http) in order. The first requests persist tokens and ids for the rest.

### Roles

| Role | Typical access |
|---|---|
| `MANAGER` | Full customer/vehicle management, including deactivate/reactivate |
| `RECEPTIONIST` | Register and update customers and vehicles; cannot deactivate |
| `MECHANIC` | Read customers (by document/id) and vehicles |
| `CLIENT` | Read **their own** customer, vehicles, and service orders; approve their budget |

## Endpoints

Base URL: `http://localhost:8080`. Send `Authorization: Bearer <accessToken>` unless the endpoint is public. A `CLIENT` may only read **their own** customer and vehicles.

### Auth

| Method | Path | Auth | Status | Description |
|---|---|---|---|---|
| `POST` | `/auth/users` | Public (empty DB) or `MANAGER` | `201` | Register a staff user. First user must be `MANAGER`. `CLIENT` is rejected. |
| `POST` | `/auth/login` | Public | `200` | Issue access + refresh tokens. |
| `POST` | `/auth/refresh` | Public | `200` | Rotate the refresh token and issue a new access JWT. |
| `POST` | `/auth/logout` | Public | `204` | Revoke the refresh session. |
| `GET` | `/auth/invites/{token}` | Public | `200` | Preview a login invite (`customerName`, `expiresAt`). |
| `POST` | `/auth/invites/{token}` | Public | `201` | Create a `CLIENT` login (`email` + `password`). |
| `POST` | `/auth/invites/customer/{customerId}` | `RECEPTIONIST`, `MANAGER` | `204` | Resend the login invite if the customer has no user yet. |

**`POST /auth/users` body**

```json
{
  "email": "gerente@oficina.com",
  "password": "senha123",
  "role": "MANAGER",
  "customerId": null
}
```

`role`: `MANAGER` \| `RECEPTIONIST` \| `MECHANIC`. Staff must omit `customerId`. `CLIENT` accounts come from `POST /auth/invites/{token}`.

**`POST /auth/login` body**

```json
{
  "email": "gerente@oficina.com",
  "password": "senha123"
}
```

### Customers

| Method | Path | Roles | Status | Description |
|---|---|---|---|---|
| `POST` | `/customers` | `RECEPTIONIST`, `MANAGER` | `201` | Register a customer (CPF or CNPJ). Sends a login invite to the contact email. |
| `GET` | `/customers` | `RECEPTIONIST`, `MANAGER` | `200` | List all customers. |
| `GET` | `/customers/document/{document}` | `RECEPTIONIST`, `MECHANIC`, `MANAGER` | `200` | Find by CPF/CNPJ (formatted or digits only). |
| `GET` | `/customers/{id}` | `CLIENT`, `RECEPTIONIST`, `MECHANIC`, `MANAGER` | `200` | Find by id. |
| `PUT` | `/customers/{id}` | `RECEPTIONIST`, `MANAGER` | `200` | Update name and/or contact (`email`, `phone`). |
| `DELETE` | `/customers/{id}` | `MANAGER` | `204` | Deactivate (soft delete; history is kept). |
| `POST` | `/customers/{id}` | `MANAGER` | `204` | Reactivate a customer. |

**`POST /customers` body**

```json
{
  "documentId": "529.982.247-25",
  "name": "Ana Souza",
  "email": "ana.souza@email.com",
  "phone": "11987654321"
}
```

**`PUT /customers/{id}` body** (all fields optional)

```json
{
  "name": "Ana Souza Silva",
  "email": "ana.silva@email.com",
  "phone": "11988887777"
}
```

### Vehicles

| Method | Path | Roles | Status | Description |
|---|---|---|---|---|
| `POST` | `/vehicles` | `RECEPTIONIST`, `MANAGER` | `201` | Register a vehicle for a customer. |
| `GET` | `/vehicles/owner/{ownerId}` | `CLIENT`, `RECEPTIONIST`, `MECHANIC`, `MANAGER` | `200` | List vehicles owned by the customer. |
| `GET` | `/vehicles/{id}` | `CLIENT`, `RECEPTIONIST`, `MECHANIC`, `MANAGER` | `200` | Find vehicle by id. |
| `GET` | `/vehicles?plate={plate}` | `CLIENT`, `RECEPTIONIST`, `MECHANIC`, `MANAGER` | `200` | Find vehicle by license plate. |
| `PUT` | `/vehicles/{id}` | `RECEPTIONIST`, `MANAGER` | `200` | Update brand, model, and/or year. |
| `PATCH` | `/vehicles/{id}/plate` | `RECEPTIONIST`, `MANAGER` | `200` | Change license plate. |
| `PATCH` | `/vehicles/{id}/owner` | `RECEPTIONIST`, `MANAGER` | `200` | Transfer the vehicle to another customer. |

**`POST /vehicles` body**

```json
{
  "ownerId": "00000000-0000-0000-0000-000000000000",
  "plate": "ABC1D23",
  "brand": "Toyota",
  "model": "Corolla",
  "year": 2024,
  "color": "Prata"
}
```

`plate` accepts Mercosul (`ABC1D23`) or the old format (`ABC-1234`).

**`PUT /vehicles/{id}` body** (all fields optional)

```json
{
  "brand": "Toyota",
  "model": "Corolla Cross",
  "year": 2025
}
```

**`PATCH /vehicles/{id}/plate` body**

```json
{
  "plate": "XYZ1A23"
}
```

**`PATCH /vehicles/{id}/owner` body**

```json
{
  "newOwnerId": "00000000-0000-0000-0000-000000000000"
}
```

### Service orders

| Method | Path | Roles | Status | Description |
|---|---|---|---|---|
| `POST` | `/service-orders` | `RECEPTIONIST`, `MANAGER` | `201` | Open an OS (`customerId` + `vehicleId`). Status `RECEIVED`. |
| `GET` | `/service-orders` | `RECEPTIONIST`, `MECHANIC`, `MANAGER` | `200` | List all orders. |
| `GET` | `/service-orders/customer/{customerId}` | `CLIENT`, `RECEPTIONIST`, `MECHANIC`, `MANAGER` | `200` | List by customer. `CLIENT` must be the owner. |
| `GET` | `/service-orders/{id}` | `CLIENT`, `RECEPTIONIST`, `MECHANIC`, `MANAGER` | `200` | Detail (status, total, timestamps, `serviceIds`). |
| `POST` | `/service-orders/{id}/diagnosis` | `MECHANIC`, `MANAGER` | `200` | Start diagnosis. |
| `POST` | `/service-orders/{id}/diagnosis/complete` | `MECHANIC`, `MANAGER` | `200` | Finish diagnosis and wait for approval (budget must be > 0). |
| `POST` | `/service-orders/{id}/approve` | `CLIENT`, `RECEPTIONIST`, `MANAGER` | `200` | Approve the budget. |
| `POST` | `/service-orders/{id}/complete` | `MECHANIC`, `MANAGER` | `200` | Finish execution. |
| `POST` | `/service-orders/{id}/deliver` | `RECEPTIONIST`, `MANAGER` | `200` | Deliver the vehicle. |

**`POST /service-orders` body**

```json
{
  "customerId": "00000000-0000-0000-0000-000000000000",
  "vehicleId": "00000000-0000-0000-0000-000000000000"
}
```

Look up the customer first with `GET /customers/document/{document}` (CPF/CNPJ). Each status change emails the customer's contact address (Mailpit locally at `http://localhost:8025`):

- `POST /service-orders/{id}/diagnosis` (`RECEIVED` → `IN_DIAGNOSIS`) — subject `Diagnostico iniciado`
- `POST /service-orders/{id}/approve` (`WAITING_APPROVAL` → `IN_EXECUTION`) — subject `Orcamento aprovado`
- `POST /service-orders/{id}/deliver` (`FINISHED` → `DELIVERED`) — subject `Veiculo entregue`

### Catalog (services on an OS)

Services are line items of a service order (name, price, status, duration), not a shop-wide catalog.

| Method | Path | Roles | Status | Description |
|---|---|---|---|---|
| `POST` | `/services` | `RECEPTIONIST`, `MANAGER` | `201` | Add a service to an OS. Recalculates the budget. |
| `GET` | `/services` | `RECEPTIONIST`, `MECHANIC`, `MANAGER` | `200` | List all services. |
| `GET` | `/services/customer/{customerId}` | `CLIENT`, `RECEPTIONIST`, `MECHANIC`, `MANAGER` | `200` | List services of a customer. `CLIENT` must be the owner. |
| `GET` | `/services/service-order/{serviceOrderId}` | `CLIENT`, `RECEPTIONIST`, `MECHANIC`, `MANAGER` | `200` | List services of an OS. `CLIENT` must own the order. |
| `GET` | `/services/average-execution-time` | `RECEPTIONIST`, `MECHANIC`, `MANAGER` | `200` | Average duration of finished services (`sampleSize`, `averageSeconds`). |
| `GET` | `/services/{id}` | `RECEPTIONIST`, `MECHANIC`, `MANAGER` | `200` | Find a service. |
| `PUT` | `/services/{id}` | `MANAGER` | `200` | Update name and/or price. |
| `DELETE` | `/services/{id}` | `RECEPTIONIST`, `MANAGER` | `204` | Remove a service still `WAITING`. |
| `POST` | `/services/{id}/in-progress` | `MECHANIC`, `MANAGER` | `200` | Start execution. |
| `POST` | `/services/{id}/finish` | `MECHANIC`, `MANAGER` | `200` | Finish and record duration. |

**`POST /services` body**

```json
{
  "serviceOrderId": "00000000-0000-0000-0000-000000000000",
  "name": "Troca de oleo",
  "basePrice": 150.00
}
```

A `CLIENT` lists line items with `GET /services/customer/{customerId}` or `GET /services/service-order/{serviceOrderId}`. The OS detail only returns `serviceIds`.

### Docs and errors (public)

| Method | Path | Description |
|---|---|---|
| `GET` | `/swagger-ui.html` | Swagger UI |
| `GET` | `/v3/api-docs` | OpenAPI JSON |
| `GET` | `/error` | Spring error fallback |

Domain rules: valid Brazilian CPF/CNPJ, unique document and plate, no new vehicles on an inactive customer. Typical errors: `401` unauthenticated, `403` forbidden (including a `CLIENT` accessing another customer), `409` duplicate, `422` validation.

## Testing

```bash
./gradlew unitTest          # @Tag("unit") — no Testcontainers
./gradlew test              # all tests, including integration (needs Docker)
./gradlew koverHtmlReport koverVerify
./gradlew check             # tests + Detekt
```

Kover HTML report: `build/reports/kover/html`. Verification fails if line coverage on domain, application (use cases, mappers, assemblers), persistence adapters, event listeners, and HTTP controllers drops below **98%**.

Integration tests (`@Tag("integration")`) boot the Spring context against Postgres via Testcontainers.

## Code quality

Detekt uses Kotlin conventions + ktlint + type resolution on `main`. HTML report: `build/reports/detekt/`.

```bash
./gradlew detekt
./gradlew detektMain
./gradlew detekt --auto-correct
./gradlew check
```

`check` includes `detektMain` and fails on findings. `--auto-correct` applies ktlint fixes.

Gradle copies `hooks/pre-commit` into `.git/hooks` on compile/Detekt. Each `git commit` runs Detekt; findings block the commit.

## CI

On pull request (and `workflow_dispatch`), [`.github/workflows/unit-tests.yml`](.github/workflows/unit-tests.yml) runs unit tests with Kover, uploads the HTML coverage artifact, and posts a single coverage comment on the PR (the previous coverage comment is replaced).

## Configuration

Values in [`src/main/resources/application.properties`](src/main/resources/application.properties) are read from the environment. Copy [`.env.example`](.env.example) and export the variables (or set them in the shell) before a shared or production deploy.

| Property | Environment variable | Local default | Notes |
|---|---|---|---|
| `spring.datasource.url` | `DATABASE_URL` | `jdbc:postgresql://localhost:5432/autorepairshop` | In Compose the API uses `jdbc:postgresql://postgres:5432/autorepairshop` |
| `spring.datasource.username` | `DATABASE_USERNAME` | `postgres` | Local only |
| `spring.datasource.password` | `DATABASE_PASSWORD` | `postgres` | Override outside local Docker |
| `app.security.jwt.secret` | `JWT_SECRET` | placeholder | Must be **at least 32 bytes** outside local dev |
| `app.security.jwt.ttl-seconds` | `JWT_TTL_SECONDS` | `900` | Access token lifetime |
| `app.security.refresh.ttl-seconds` | `REFRESH_TTL_SECONDS` | `1209600` | Refresh session lifetime (14 days) |
| `spring.mail.host` | `MAIL_HOST` | `localhost` | Mailpit in Compose (`mailpit`) |
| `spring.mail.port` | `MAIL_PORT` | `1025` | SMTP port |
| `app.mail.from` | `MAIL_FROM` | `oficina@localhost` | Sender address |
| `app.mail.invite-base-url` | `INVITE_BASE_URL` | `http://localhost:8080/invite` | Prefix of the invite link (`/{token}` when the base has no `?`) |

Tests use `src/test/resources/application.properties` (a dedicated JWT secret). Do not commit real secrets; `.env` and `application-local.properties` are gitignored.

## Troubleshooting

**`Could not find a valid Docker environment`**  
Start Docker Desktop and wait until it is healthy, then run `docker compose up -d` again.

**Port 5432 or 8080 already in use**  
Another Postgres or API is bound to that port. Stop it, or change the port in `docker-compose.yml` / `application.properties`.

**Flyway / connection refused**  
Postgres is not ready yet. Run `docker compose ps` and wait until it is healthy. If you started only the database, run `bootRun` after that.

**401 on customer/vehicle requests**  
Login first (`POST /auth/login`) and send `Authorization: Bearer <accessToken>`. Staff vs client permissions are listed under [Roles](#roles).

**422 on first user registration**  
The empty database only accepts a `MANAGER` as the first user.
