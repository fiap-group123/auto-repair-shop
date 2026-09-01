# Auto Repair Shop

API de um MVP de oficina mecânica (FIAP Tech Challenge).

Monólito Kotlin/Spring Boot com DDD tático: usuários da oficina (JWT), clientes (CPF/CNPJ), veículos (placa brasileira), ordens de serviço, orçamento e itens de serviço. Um único deployável, PostgreSQL, eventos in-process.

Como subir o projeto: **[Getting started](docs/GETTING_STARTED.md)**.

## Documentação

| Documento                                         | Conteúdo |
|---------------------------------------------------|---|
| [GETTING_STARTED.md](docs/GETTING_STARTED.md)     | Pré-requisitos, Docker, Gradle, IDE, testes, qualidade, CI |
| [ENDPOINTS.md](docs/ENDPOINTS.md)                 | Rotas, papéis, request/response |
| [ERRORS.md](docs/ERRORS.md)                       | RFC 7807, status HTTP, exceção → status |
| [CONFIGURATION.md](docs/CONFIGURATION.md)         | Variáveis de ambiente, Compose, secrets do GitHub |
| [ARCHITECTURE.md](docs/ARCHITECTURE.md)           | Camadas, bounded contexts, fluxo, persistência |
| [ADR 001-postgresql.md](docs/adr/001-postgresql.md) | Por que PostgreSQL |
| [Auth HTTP](http/auth.http)                       | 1. Staff, login, convite, erros |
| [Customer HTTP](http/customer.http)               | 2. Cliente, veículos (Civic da OS + Corolla) |
| [Service Order HTTP](http/service-order.http)     | 3. OS do Civic até a entrega |
| [Budget HTTP](http/budget.http)                   | OS do Corolla: negociar, depois delete |
| [Extra Service HTTP](http/extra-service.http)     | 4. Nova OS no Civic + extras |

Cada `.http` é linear (de cima para baixo). Cada arquivo faz o próprio login. Ordem: **auth → customer → service-order → extra-service**. O `budget.http` usa o Corolla (`XYZ1A23`) e pode rodar depois do `customer.http`. No convite, copie o token no [Mailpit](http://localhost:8025).

## O que a API cobre

| Contexto | Pacote | Responsabilidade |
|---|---|---|
| Authentication | `authentication` | Staff, convite do cliente, JWT + refresh |
| Customer | `customer` | Cliente (CPF/CNPJ) e veículos |
| Service order | `serviceorder` | Ciclo de vida da OS |
| Budget | `budget` | Total e aprovação do orçamento |
| Catalog | `catalog` | Itens de diagnóstico (`Service`) e extras (`ExtraService`) |

Papéis: `MANAGER`, `RECEPTIONIST`, `MECHANIC`, `CLIENT`. Conta `CLIENT` só nasce pelo convite (e-mail de contato do cliente). Detalhe das rotas: [ENDPOINTS.md](docs/ENDPOINTS.md). Ciclo da OS:

```
RECEIVED → IN_DIAGNOSIS → WAITING_APPROVAL → BUDGET_APPROVED → IN_EXECUTION → FINISHED → DELIVERED
                              ↘ BUDGET_REJECTED
```

## Stack

| | |
|---|---|
| Linguagem | Kotlin 2.4.10, JDK 17 |
| Framework | Spring Boot 4.1.1 (Web MVC, Security, JPA, Validation, Mail, Actuator) |
| Auth | JWT HS256 (OAuth2 resource server), refresh no banco, senha BCrypt |
| Banco | PostgreSQL 16, Flyway, Hibernate `validate` |
| API docs | SpringDoc OpenAPI 3.1 / Swagger UI |
| Testes | JUnit 5, MockK, Testcontainers, ArchUnit, Kover (mín. **80%** de linha no núcleo) |
| Qualidade | Detekt + ktlint |
| Build | Gradle 9.5.1 (wrapper) |
| Runtime | Dockerfile + docker-compose (API, Postgres, Mailpit) |

## Comandos frequentes

```bash
./gradlew bootRun          # API no host (Postgres no Docker)
./gradlew unitTest         # @Tag("unit") — sem Testcontainers
./gradlew test             # todos os testes (precisa Docker)
./gradlew koverHtmlReport koverVerify
./gradlew detektMain
./gradlew check            # testes + Detekt
```

No Windows CMD/PowerShell use `gradlew.bat` no lugar de `./gradlew`.
