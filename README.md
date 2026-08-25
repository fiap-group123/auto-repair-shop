# Auto Repair Shop

Backend API for an auto repair shop MVP (FIAP Tech Challenge — Phase 1).

Kotlin, Spring Boot, PostgreSQL, and tactical DDD in a layered monolith. The API covers workshop users (JWT auth), customers (CPF/CNPJ), and vehicles (Brazilian plates, ownership transfer).

## Contents

- [Tech stack](#tech-stack)
- [Architecture](#architecture)
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
| Tests | JUnit 5, MockK, Testcontainers, Kover (min. 98% line coverage on domain/use cases) |
| Quality | Detekt + ktlint, Git pre-commit hook |
| Build | Gradle 9.5 (wrapper included) |

## Architecture

Two bounded contexts in a single deployable:

- **Authentication** — users, roles, login, JWT issuance
- **Customer** — customers and vehicles (aggregates, value objects, use cases)

Each context follows `domain` → `application` → `infrastructure`. HTTP adapters, JWT security, and OpenAPI live under `api`.

```
src/main/kotlin/br/com/autorepairshop/
├── api/                  # Controllers, security, exception handlers, OpenAPI
├── authentication/       # Users, roles, login
├── customer/             # Customers and vehicles
└── shared/               # AggregateRoot, UseCase, domain events
```

Schema is owned by Flyway (`src/main/resources/db/migration/`): customers, vehicles, then users.

## Prerequisites

- **JDK 17**
- **Docker Desktop** running (engine up)
- Git

The Gradle Wrapper is in the repo (`gradlew` / `gradlew.bat`). You do not need a local Gradle install.

## Getting started

From the repository root:

```bash
docker compose up -d
```

This starts PostgreSQL on `localhost:5432` (database `autorepairshop`, user/password `postgres`). Wait until the container is healthy (`docker compose ps`).

Then start the API:

```bash
./gradlew bootRun
```

On Windows CMD/PowerShell use `gradlew.bat bootRun`. When you see `Tomcat started on port 8080`, the app is up.

### IDE

1. `docker compose up -d`
2. Run `src/main/kotlin/br/com/autorepairshop/AutoRepairShopApplication.kt`

Do not use `bootTestRun` unless Docker is running: that profile starts Postgres via Testcontainers and fails if the engine is down.

### Stop

```bash
# API: Ctrl+C in the bootRun terminal

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

Authorize Swagger (or `.http` files) with `Authorization: Bearer <accessToken>` after login. Swagger UI, OpenAPI, and `/error` are public; everything else requires a JWT except user registration and login.

### Auth

1. Register the **first** user as `MANAGER` (`POST /auth/users`). Later staff and clients can be created with other roles.
2. `POST /auth/login` returns `{ "accessToken", "tokenType": "Bearer" }`. Tokens last **1 hour** by default.
3. A `CLIENT` user **must** include `customerId` (the shop customer already registered). Staff roles must **not**.

Ready-made flow: run [`http/auth.http`](http/auth.http) in order. The first requests persist tokens and ids for the rest.

### Roles

| Role | Typical access |
|---|---|
| `MANAGER` | Full customer/vehicle management, including deactivate/reactivate |
| `RECEPTIONIST` | Register and update customers and vehicles; cannot deactivate |
| `MECHANIC` | Read customers (by document/id) and vehicles |
| `CLIENT` | Read **their own** customer record and vehicles only |

## Endpoints

Base URL: `http://localhost:8080`. Send `Authorization: Bearer <accessToken>` unless the endpoint is public. A `CLIENT` may only read **their own** customer and vehicles.

### Auth

| Method | Path | Auth | Status | Description |
|---|---|---|---|---|
| `POST` | `/auth/users` | Public | `201` | Register a user. First user must be `MANAGER`. `CLIENT` requires `customerId`; staff must omit it. |
| `POST` | `/auth/login` | Public | `200` | Issue a JWT (`accessToken`, `tokenType`). |

**`POST /auth/users` body**

```json
{
  "email": "gerente@oficina.com",
  "password": "senha123",
  "role": "MANAGER",
  "customerId": null
}
```

`role`: `MANAGER` \| `RECEPTIONIST` \| `MECHANIC` \| `CLIENT`. `customerId` is required only for `CLIENT`.

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
| `POST` | `/customers` | `RECEPTIONIST`, `MANAGER` | `201` | Register a customer (CPF or CNPJ). |
| `GET` | `/customers` | `RECEPTIONIST`, `MANAGER` | `200` | List all customers. |
| `GET` | `/customers/document/{document}` | `RECEPTIONIST`, `MECHANIC`, `MANAGER` | `200` | Find by CPF/CNPJ (formatted or digits only). |
| `GET` | `/customers/{id}` | `CLIENT`, `RECEPTIONIST`, `MECHANIC`, `MANAGER` | `200` | Find by id. |
| `PUT` | `/customers/{id}` | `RECEPTIONIST`, `MANAGER` | `200` | Update name and/or contact (`email`, `phone`). |
| `DELETE` | `/customers/{id}` | `MANAGER` | `204` | Deactivate (soft delete; history is kept). |
| `POST` | `/customers/{id}` | `MANAGER` | `204` | Reactivate a customer. |
| `POST` | `/customers/{id}/vehicles` | `RECEPTIONIST`, `MANAGER` | `201` | Register a vehicle for that customer. |
| `GET` | `/customers/{id}/vehicles` | `CLIENT`, `RECEPTIONIST`, `MECHANIC`, `MANAGER` | `200` | List vehicles owned by the customer. |

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

**`POST /customers/{id}/vehicles` body**

```json
{
  "plate": "ABC1D23",
  "brand": "Toyota",
  "model": "Corolla",
  "year": 2024
}
```

`plate` accepts Mercosul (`ABC1D23`) or the old format (`ABC-1234`).

### Vehicles

| Method | Path | Roles | Status | Description |
|---|---|---|---|---|
| `GET` | `/vehicles/{id}` | `CLIENT`, `RECEPTIONIST`, `MECHANIC`, `MANAGER` | `200` | Find vehicle by id. |
| `GET` | `/vehicles?plate={plate}` | `CLIENT`, `RECEPTIONIST`, `MECHANIC`, `MANAGER` | `200` | Find vehicle by license plate. |
| `PUT` | `/vehicles/{id}` | `RECEPTIONIST`, `MANAGER` | `200` | Update brand, model, and/or year. |
| `PATCH` | `/vehicles/{id}/plate` | `RECEPTIONIST`, `MANAGER` | `200` | Change license plate. |
| `PATCH` | `/vehicles/{id}/owner` | `RECEPTIONIST`, `MANAGER` | `200` | Transfer the vehicle to another customer. |

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

Kover HTML report: `build/reports/kover/html`. Verification fails if line coverage on the configured domain/use-case packages drops below **98%**.

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

Defaults in [`src/main/resources/application.properties`](src/main/resources/application.properties):

| Property | Default | Notes |
|---|---|---|
| `spring.datasource.url` | `jdbc:postgresql://localhost:5432/autorepairshop` | Match `docker-compose.yml` |
| `spring.datasource.username` / `password` | `postgres` | Local only |
| `app.security.jwt.secret` | placeholder | Use a secret of **at least 32 bytes** outside local dev |
| `app.security.jwt.ttl-seconds` | `3600` | Access token lifetime |

Do not commit real secrets. Change the JWT secret before any shared or production deploy.

## Troubleshooting

**`Could not find a valid Docker environment`**  
Start Docker Desktop and wait until it is healthy, then run `docker compose up -d` again.

**Port 5432 or 8080 already in use**  
Another Postgres or API is bound to that port. Stop it, or change the port in `docker-compose.yml` / `application.properties`.

**Flyway / connection refused**  
Compose is not ready yet. Run `docker compose ps` and only then `bootRun`.

**401 on customer/vehicle requests**  
Login first (`POST /auth/login`) and send `Authorization: Bearer <accessToken>`. Staff vs client permissions are listed under [Roles](#roles).

**422 on first user registration**  
The empty database only accepts a `MANAGER` as the first user.
