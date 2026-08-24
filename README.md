# Auto Repair Shop

Back-end do MVP da oficina mecânica (Tech Challenge FIAP — Fase 1).  
Kotlin, Spring Boot, PostgreSQL, DDD tático em monolito em camadas.

## Pré-requisitos

- **JDK 17**
- **Docker Desktop** aberto (engine running)
- Git

O Gradle Wrapper já vem no repositório (`gradlew` / `gradlew.bat`). Não precisa instalar o Gradle.

## Subir o projeto

Na raiz do repositório:

```bash
docker compose up -d
```

Isso sobe o PostgreSQL em `localhost:5432` (banco `autorepairshop`, usuário e senha `postgres`).

Em seguida, a API:

```bash
./gradlew bootRun
```

Quando aparecer `Tomcat started on port 8080`, a aplicação está no ar.

### Alternativa: IntelliJ / Cursor

1. `docker compose up -d`
2. Rodar `src/main/kotlin/br/com/autorepairshop/AutoRepairShopApplication.kt`

Não use `bootTestRun` a menos que o Docker esteja de pé: esse modo sobe o Postgres via Testcontainers e falha se o Docker Desktop não estiver running.

## Conferir se funcionou

| Recurso | URL |
|---|---|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |
| Requests prontas | `http/customer.http` |

O arquivo `.http` cobre cadastro de cliente, veículo, busca por CPF/placa e os erros (documento inválido, placa duplicada, cliente inativo). Execute na ordem: os primeiros `POST` gravam os ids para as próximas chamadas.

## Parar

```bash
# API: Ctrl+C no terminal do bootRun

docker compose down
```

`docker compose down -v` também apaga o volume do banco (dados locais somem).

## Problemas comuns

**`Could not find a valid Docker environment`**  
Abra o Docker Desktop e espere o status ficar verde. Depois rode de novo o `docker compose up -d`.

**Porta 5432 ou 8080 em uso**  
Outro Postgres ou outra API já está no ar. Pare o processo ou mude a porta no `docker-compose.yml` / `application.properties`.

**Flyway / conexão recusada**  
O compose ainda não ficou healthy. Rode `docker compose ps` e só então o `bootRun`.
