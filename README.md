# Auto Repair Shop

Backend API for an auto repair shop MVP (FIAP Tech Challenge — Phase 1).

Kotlin, Spring Boot, PostgreSQL, and tactical DDD in a layered monolith. The API covers workshop users (JWT auth), customers (CPF/CNPJ), vehicles (Brazilian plates), service orders, and the services attached to each order.

## Contents

- [Documentation](#documentation)
- [Error mapping](#error-mapping)
- [Tech stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Getting started](#getting-started)
- [Testing](#testing)
- [Code quality](#code-quality)
- [CI](#ci)
- [Configuration](#configuration)
- [Troubleshooting](#troubleshooting)

## Documentation

| Document | Contents |
|---|---|
| [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) | Layers, bounded contexts, request flow, integration |
| [docs/ENDPOINTS.md](docs/ENDPOINTS.md) | All routes, roles, request/response bodies |
| [docs/adr/001-postgresql.md](docs/adr/001-postgresql.md) | Why PostgreSQL |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |
| Auth requests | [`http/auth.http`](http/auth.http) |
| Customer / vehicle requests | [`http/customer.http`](http/customer.http) |
| Service order / catalog requests | [`http/service-order.http`](http/service-order.http) |

Authorize Swagger or `.http` files with `Authorization: Bearer <accessToken>` after login. Run [`http/auth.http`](http/auth.http) in order to persist tokens for the rest.

## Error mapping

Errors use RFC 7807 (`application/problem+json`). `detail` is the domain message.

```json
{
  "type": "about:blank",
  "title": "Unprocessable Entity",
  "status": 422,
  "detail": "Customer 529.***.***-25 is inactive."
}
```

Anything that extends `DomainException` and is not listed below falls through to `ApiExceptionHandler` as `422`.

| Status | Meaning |
|---|---|
| `400` | Malformed JSON (`HttpMessageNotReadableException`) |
| `401` | Missing/invalid JWT, bad credentials, invalid or reused refresh |
| `403` | Role not allowed, or `CLIENT` accessing another customer |
| `404` | Resource not found |
| `409` | Duplicate or conflicting state |
| `410` | Invite expired or already used |
| `422` | Domain validation (document, plate, status transition, inactive entity) |

### Authentication

| Exception | Status |
|---|---|
| `InvalidCredentials`, `Unauthenticated`, `InvalidRefresh`, `RefreshReuse` | `401` |
| Spring `AuthenticationException`, `JwtException` | `401` |
| `Forbidden`, Spring `AccessDeniedException` | `403` |
| `UserNotFound`, `InviteNotFound`, `LinkedCustomerNotFound` | `404` |
| `UserAlreadyExists`, `CustomerAlreadyHasUser` | `409` |
| `InviteExpired`, `InviteConsumed` | `410` |
| `UserInactive`, `UserAlreadyActive`, `InvalidEmail`, `InvalidPassword`, `InvalidRole`, `LinkedCustomerInactive` | `422` |

Filter-chain auth (`SecurityProblemSupport`) also writes `401` / `403` as Problem JSON (no JWT, expired token, role mismatch).

### Customer

| Exception | Status |
|---|---|
| `CustomerNotFound` | `404` |
| `CustomerAlreadyExists` | `409` |
| `CustomerAlreadyActive`, `CustomerInactive`, `InvalidDocument`, `InvalidPersonName`, `InvalidPhoneNumber`, `InvalidEmailAddress` | `422` |

### Vehicle

| Exception | Status |
|---|---|
| `VehicleNotFound` | `404` |
| `VehicleAlreadyExists`, `AlreadyOwnedByCustomer` | `409` |
| `InvalidLicensePlate`, `InvalidModelYear`, `InvalidVehicleName`, `VehicleInactive`, `VehicleAlreadyActive` | `422` |

### Service order

| Exception | Status |
|---|---|
| `ServiceOrderNotFound` | `404` |
| `OpenOrderAlreadyExists`, `VehicleNotOwnedByCustomer` | `409` |
| `InvalidStatusTransition`, `EmptyBudget`, `InvalidDuration` | `422` |

### Catalog

| Exception | Status |
|---|---|
| `ServiceNotFound` | `404` |
| `ServiceAlreadyExists` | `409` |
| `InvalidServiceName`, `InvalidStatusTransition`, `InvalidDuration` | `422` |

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
Login first (`POST /auth/login`) and send `Authorization: Bearer <accessToken>`. Roles and routes: [docs/ENDPOINTS.md](docs/ENDPOINTS.md).

**422 on first user registration**  
The empty database only accepts a `MANAGER` as the first user.
