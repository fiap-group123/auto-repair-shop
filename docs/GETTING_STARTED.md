# Getting started

Como colocar a oficina no ar, percorrer o atendimento e rodar a suíte. O mapa do projeto está no [README](../README.md). Variáveis e segredos: [CONFIGURATION.md](CONFIGURATION.md). Rotas: [ENDPOINTS.md](ENDPOINTS.md).

## Pré-requisitos

- JDK 17 (Temurin ou equivalente), para `bootRun` e testes no host
- Docker Desktop com o engine em execução
- Git

O Gradle Wrapper está no repositório (`gradlew` e `gradlew.bat`). Não é preciso instalar o Gradle.

```bash
cp .env.example .env
```

O Compose lê o `.env` sozinho. Não commite esse arquivo.

## Duas formas de subir

| Modo | Comando | Quando usar |
|---|---|---|
| Compose | `docker compose up --build` | Código deste repositório. A imagem fica `auto-repair-shop:local`, em http://localhost:8080 |
| Cluster | `bash scripts/up-cluster.sh` | Imagem publicada `public.ecr.aws/w2x8c5h2/auto-repair-shop:v0.1.0`, com HPA, em http://localhost:30080. Recria o kind se ele não responder |
| API no host | `docker compose up -d postgres mailpit` e depois `./gradlew bootRun` | Depurar no IDE. A API fala com `localhost` |

O Compose sobe Postgres 16 (`autorepairshop`) e Mailpit junto com a API local. O cluster tem o próprio Postgres e o próprio Mailpit.

A API só sobe depois do healthcheck do Postgres. Confira com `docker compose ps`.

| Serviço | Porta |
|---|---|
| API | http://localhost:8080 |
| Swagger | http://localhost:8080/swagger-ui.html |
| OpenAPI | http://localhost:8080/v3/api-docs |
| Postgres | `5432` |
| Mailpit | http://localhost:8025 (SMTP `1025`) |

No Windows CMD ou PowerShell, troque `./gradlew` por `gradlew.bat`. A classe principal é `src/main/kotlin/br/com/autorepairshop/AutoRepairShopApplication.kt`.

`docker compose down` para os containers. `docker compose down -v` também apaga o volume do Postgres.

Dentro do Compose a API usa os hostnames `postgres` e `mailpit`. No `bootRun` os defaults são `localhost`. O serviço `api` do Compose já força a URL interna. Detalhe em [CONFIGURATION.md](CONFIGURATION.md).

## Primeiro atendimento

Banco vazio: `POST /auth/users` é público somente até existir o primeiro usuário, e esse usuário precisa ser `MANAGER`.

```bash
curl -s -X POST http://localhost:8080/auth/users \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"gerente@oficina.com\",\"password\":\"senha123\",\"role\":\"MANAGER\"}"
```

```bash
curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"gerente@oficina.com\",\"password\":\"senha123\"}"
```

Guarde o `accessToken` e envie `Authorization: Bearer <token>`. A senha tem no mínimo 8 caracteres, com letra e dígito. O access dura cerca de 1 dia. O refresh dura cerca de 14 dias.

A partir daí o atendimento segue esta ordem:

1. O gerente cadastra recepção e mecânico em `POST /auth/users`, agora autenticado.
2. A recepção cadastra o cliente em `POST /customers`. O convite cai no Mailpit.
3. O cliente abre `GET /invite/{token}` e conclui com `POST /invite/{token}`, informando e-mail e senha. Nasce o usuário `CLIENT`.
4. A recepção cadastra o veículo e, se a OS for usar peça, um item em `POST /inventories`.
5. A OS abre em `POST /service-orders`. Com documento e placa de quem já existe, ou com cliente, veículo, serviços e peças no mesmo body. A resposta traz o id.
6. O mecânico chama `POST /service-orders/{id}/diagnosis` e, ao terminar, `POST /service-orders/{id}/diagnosis/finish`. A OS fica `WAITING_APPROVAL`.
7. O cliente aprova em `POST /budgets/{id}/approve`, ou um sistema externo envia `POST /integrations/budget-decisions` com o header `X-Notification-Token: local-budget-notification-token`.
8. O mecânico inicia a execução, conclui, e a recepção entrega: `POST /service-orders/{id}/execution`, `/finish` e `/deliver`.
9. `GET /service-orders` é a fila, sem finalizadas e entregues. `GET /service-orders/{id}/status` devolve o rótulo em português.

Cada mudança de status manda e-mail ao contato do cliente. Leia a caixa em http://localhost:8025.

As collections prontas estão em [`http/`](../http/). Rode de cima para baixo. Para a fase 2, com o volume zerado, basta [`http/phase2.http`](../http/phase2.http): ele cria o gerente, o estoque, a OS completa, consulta status e fila e aprova o orçamento pelo token externo.

## Testes

```bash
./gradlew unitTest
./gradlew test
./gradlew koverHtmlReport koverVerify
./gradlew check
```

- `unitTest` roda o que está marcado com `@Tag("unit")`: domínio, casos de uso com MockK e handlers. Não sobe Spring nem Postgres.
- `test` inclui a integração (`@Tag("integration")`): contexto Spring e PostgreSQL via Testcontainers. O Docker precisa estar ligado.
- O HTML do Kover sai em `build/reports/kover/html`.
- `koverVerify` falha se a cobertura de linha do núcleo ficar abaixo de 80%. O núcleo é domain, application, adapters de persistência, listeners e controllers. Entidades JPA e alguns value objects de identidade ficam de fora. O filtro está em [`build.gradle.kts`](../build.gradle.kts).

Os testes leem [`src/test/resources/application.properties`](../src/test/resources/application.properties), com um JWT próprio. Não usam o `.env`.

`bootTestRun` sobe o Postgres por Testcontainers. Sem o Docker, esse perfil falha.

## Qualidade

```bash
./gradlew detektMain
./gradlew detekt --auto-correct
./gradlew check
```

O Detekt usa as convenções Kotlin, o ktlint e a resolução de tipos em `main`. O HTML sai em `build/reports/detekt/`. O `check` inclui `detektMain`. O ArchUnit, dentro da suíte de testes, vigia a direção das dependências entre camadas.

O CI roda `detektMain`. A tarefa `detekt` completa também analisa testes e ainda encontra achados antigos fora do que o pipeline barra.

## CI

Em pull request, [`.github/workflows/pull-request-checks.yml`](../.github/workflows/pull-request-checks.yml) compila, roda `detektMain` e a cobertura, publica o HTML como artifact e comenta a porcentagem no PR.

[`.github/workflows/cd.yml`](../.github/workflows/cd.yml) roda no push da `main`, em tags `v*` e no disparo manual. Ele repete as checagens, publica a imagem no ECR Public, cria a tag e a GitHub Release, sobe um kind com Terraform (Postgres incluso) e aplica [`k8s/`](../k8s). Esse cluster existe só durante o job.

[`.github/workflows/sonar.yml`](../.github/workflows/sonar.yml) analisa a `main` no SonarQube Cloud. O Dependabot atualiza Gradle, Actions e Docker.

Variáveis e secrets: [CONFIGURATION.md](CONFIGURATION.md#github-actions). Conta AWS: [AWS.md](AWS.md).

## Problemas comuns

**`Could not find a valid Docker environment`**  
Abra o Docker Desktop e espere o engine ficar saudável. Testes de integração também precisam dele.

**Porta 5432 ou 8080 ocupada**  
Outro Postgres ou outra API já está escutando. Pare o processo, ou mude a porta no `docker-compose.yml`.

**Flyway ou connection refused**  
O Postgres ainda não passou no healthcheck. Espere `healthy` em `docker compose ps` antes do `bootRun`.

**A API no container não alcança o banco**  
Dentro da rede do Compose o host é `postgres`. O serviço `api` já injeta `jdbc:postgresql://postgres:5432/autorepairshop`. Um `DATABASE_URL` com `localhost` serve para o Gradle, não para o container.

**401 em `POST /auth/login` com `Invalid credentials`**  
O banco não tem esse usuário. No volume vazio, crie o gerente com `POST /auth/users` antes do login. O `http/phase2.http` faz isso no primeiro request.

**401 nas rotas de cliente, veículo ou OS**  
Envie `Authorization: Bearer <accessToken>`. Papéis em [ENDPOINTS.md](ENDPOINTS.md).

**422 no primeiro `POST /auth/users`**  
O banco vazio só aceita `MANAGER`. `CLIENT` nasce pelo convite. Senha curta ou sem letra e dígito também devolve 422.

**Convite ou e-mail de status não aparece**  
Com o Compose, abra http://localhost:8025. Com a API no host, suba o Mailpit (`docker compose up -d mailpit`) e deixe `MAIL_HOST=localhost`.

**Token válido e mesmo assim 401 `User is inactive`**  
O `ActiveUserFilter` relê o usuário a cada request. Cliente desativado derruba o usuário ligado a ele.

**A API em `8080` e a API em `30080` não compartilham dados**  
`8080` é o Compose, com o código local. `30080` é o cluster, com a imagem publicada e outro Postgres.
