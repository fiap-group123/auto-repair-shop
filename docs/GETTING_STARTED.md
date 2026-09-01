# Getting started

Como subir a API, cadastrar o primeiro usuário, rodar testes e o que o CI espera. Configuração detalhada (env, Compose, secrets): [CONFIGURATION.md](CONFIGURATION.md). Rotas: [ENDPOINTS.md](ENDPOINTS.md).

## Pré-requisitos

- **JDK 17** (Temurin ou equivalente)
- **Docker Desktop** com o engine rodando
- Git

O Gradle Wrapper está no repositório (`gradlew` / `gradlew.bat`). Não é preciso instalar Gradle no host.

Copie [`.env.example`](../.env.example) para `.env` na raiz. O Compose lê esse arquivo automaticamente. Não commite o `.env`.

```bash
cp .env.example .env
```

## Subir o stack

Na raiz do repositório:

```bash
docker compose up --build
```

Isso constrói o [Dockerfile](../Dockerfile) e sobe:

| Serviço | Porta | O que é |
|---|---|---|
| `api` | `8080` | Spring Boot |
| `postgres` | `5432` | PostgreSQL 16, banco `autorepairshop` |
| `mailpit` | SMTP `1025`, UI `8025` | Caixa de e-mail local (convites e status da OS) |

Espere o `api` ficar up (`docker compose ps`). A API só sobe depois do healthcheck do Postgres.

- Swagger: http://localhost:8080/swagger-ui.html
- Mailpit: http://localhost:8025

### Parar

```bash
docker compose down
```

`docker compose down -v` também apaga o volume do Postgres (dados locais somem).

## Só a API no host (Gradle)

Útil para debug no IDE. O Postgres e o Mailpit continuam no Docker:

```bash
docker compose up -d postgres mailpit
./gradlew bootRun
```

No Windows CMD/PowerShell: `gradlew.bat bootRun`. Quando aparecer `Tomcat started on port 8080`, a API está no ar.

O `bootRun` no host usa `DATABASE_URL=jdbc:postgresql://localhost:5432/autorepairshop` e `MAIL_HOST=localhost` (defaults de [`application.properties`](../src/main/resources/application.properties) / `.env`). Dentro do Compose a API usa o hostname `postgres` e `mailpit` — não `localhost`. Ver [CONFIGURATION.md](CONFIGURATION.md).

### IDE

1. `docker compose up -d postgres mailpit`
2. Rode `src/main/kotlin/br/com/autorepairshop/AutoRepairShopApplication.kt`

Não use `bootTestRun` a menos que o Docker esteja ligado: esse perfil sobe Postgres via Testcontainers e falha se o engine estiver parado.

## Primeiro uso

Banco vazio: `POST /auth/users` é público **somente** até existir o primeiro usuário, e esse usuário **precisa** ser `MANAGER`. Depois a rota exige `ROLE_MANAGER`. Conta `CLIENT` não se cadastra aqui — só pelo convite.

```bash
curl -s -X POST http://localhost:8080/auth/users \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"gerente@oficina.com\",\"password\":\"senha123\",\"role\":\"MANAGER\"}"
```

Login (access ~1 dia, refresh ~14 dias):

```bash
curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"gerente@oficina.com\",\"password\":\"senha123\"}"
```

Use o `accessToken` em `Authorization: Bearer <token>`. Senha: no mínimo 8 caracteres, com letra e dígito (`senha123` passa).

Fluxo típico depois disso:

1. Cadastre um cliente (`POST /customers`) — o sistema emite convite e manda e-mail (veja no Mailpit).
2. Abra o link `GET /invite/{token}` e complete com `POST /invite/{token}` (`email` + `password`) para criar o `CLIENT`.
3. Cadastre um veículo, abra uma OS, adicione itens, inicie o diagnóstico, aprove o orçamento.

Pedidos prontos (IntelliJ HTTP Client / VS Code REST Client): rode [`http/auth.http`](../http/auth.http) na ordem para gravar tokens e ids nas variáveis globais.

## Testes

```bash
./gradlew unitTest          # @Tag("unit") — sem Testcontainers
./gradlew test              # unitários + integração (precisa Docker)
./gradlew koverHtmlReport koverVerify
./gradlew check             # testes + Detekt
```

- Unitários (`@Tag("unit")`): domínio, casos de uso com MockK, handlers — não sobem Spring nem Postgres.
- Integração (`@Tag("integration")`): contexto Spring + PostgreSQL via Testcontainers. O engine do Docker precisa estar up.
- Relatório HTML do Kover: `build/reports/kover/html`.
- `koverVerify` falha se a cobertura de **linha** no núcleo cair abaixo de **80%**.

O núcleo coberto pelo Kover é domain, application, adapters de persistência e controllers HTTP dos bounded contexts (mais `shared.domain`). Entidades JPA, `*JpaRepository` e alguns value objects de identidade estão excluídos. Filtros em [`build.gradle.kts`](../build.gradle.kts) (`kover { reports { filters { … } } }`).

Testes usam [`src/test/resources/application.properties`](../src/test/resources/application.properties) (JWT próprio). Não dependem do `.env`.

## Qualidade

Detekt usa convenções Kotlin + ktlint + type resolution em `main`. Relatório HTML: `build/reports/detekt/`.

```bash
./gradlew detekt
./gradlew detektMain
./gradlew detekt --auto-correct
./gradlew check
```

`check` inclui `detektMain` e falha em finding. `--auto-correct` aplica correções do ktlint.

ArchUnit entra na suíte de testes (regras de dependência entre camadas).

## CI

Em pull request (e `workflow_dispatch`), [`.github/workflows/pull-request-checks.yml`](../.github/workflows/pull-request-checks.yml):

1. Compila Kotlin
2. Roda `detektMain`
3. Roda testes com Kover, sobe o HTML de cobertura como artifact
4. Comenta no PR a porcentagem (o comentário anterior é substituído)

[`.github/workflows/sonar.yml`](../.github/workflows/sonar.yml) analisa `main` no SonarQube Cloud (`fiap-group123_auto-repair-shop`). Dependabot atualiza Gradle, Actions e Docker semanalmente / mensalmente.

Variáveis e secrets do GitHub: [CONFIGURATION.md](CONFIGURATION.md#github-actions).

## Troubleshooting

**`Could not find a valid Docker environment`**  
Abra o Docker Desktop e espere ficar healthy. Depois `docker compose up -d` de novo. Integração e `bootTestRun` também precisam do engine.

**Porta 5432 ou 8080 em uso**  
Outro Postgres ou API já está bound. Pare o processo, ou mude a porta no `docker-compose.yml` / `server.port`.

**Flyway / connection refused**  
O Postgres ainda não passou no healthcheck. `docker compose ps` — espere `healthy`. Se você subiu só o banco, rode o `bootRun` depois.

**API no Docker não conecta no banco**  
Dentro da rede do Compose o host do Postgres é `postgres`, não `localhost`. O serviço `api` já injeta `DATABASE_URL=jdbc:postgresql://postgres:5432/autorepairshop`. Não sobrescreva isso com o valor do `.env` feito para o host.

**401 em cliente/veículo/OS**  
Faça login (`POST /auth/login`) e envie `Authorization: Bearer <accessToken>`. Papéis: [ENDPOINTS.md](ENDPOINTS.md). Mapeamento: [ERRORS.md](ERRORS.md).

**422 no primeiro cadastro de usuário**  
O banco vazio só aceita `MANAGER`. `CLIENT` não se cadastra em `/auth/users`. Senha curta ou sem letra+dígito também vira 422.

**Convite não chega**  
Com o stack Compose, abra http://localhost:8025. Se a API está no host, o Mailpit precisa estar up (`docker compose up -d mailpit`) e `MAIL_HOST=localhost`.

**Token válido mas 401 “User is inactive”**  
O `ActiveUserFilter` recarrega o usuário a cada request. Cliente desativado desativa o `User` ligado; o JWT antigo deixa de passar.
