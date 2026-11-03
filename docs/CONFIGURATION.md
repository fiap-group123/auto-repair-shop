# Configuração

A API lê só [`src/main/resources/application.properties`](../src/main/resources/application.properties). O [Dockerfile](../Dockerfile) **não** define `ENV`: valores vêm do Compose, do `.env`, ou do ambiente do processo (`bootRun` / IDE).

Copie [`.env.example`](../.env.example) para `.env` (gitignored). O Compose carrega esse arquivo sozinho. `application-local.properties` também é gitignored, se você preferir profile local no IDE.

## Variáveis de ambiente

| Property | Variável | Default local | No Compose `api`? | Notas |
|---|---|---|---|---|
| `spring.datasource.url` | `DATABASE_URL` | `jdbc:postgresql://localhost:5432/autorepairshop` | Sim (fixo `postgres:5432`) | No host use `localhost`. Dentro do Docker o hostname é o serviço `postgres`. |
| `spring.datasource.username` | `DATABASE_USERNAME` | `postgres` | Sim | Tem que bater com o Postgres. |
| `spring.datasource.password` | `DATABASE_PASSWORD` | `postgres` | Sim | Troque fora do Docker local. |
| `app.security.jwt.secret` | `JWT_SECRET` | placeholder (≥ 32 bytes) | Sim | Obrigatório em produção. HS256 exige **no mínimo 32 bytes** (32 caracteres ASCII). |
| `app.security.jwt.ttl-seconds` | `JWT_TTL_SECONDS` | `86400` | Sim | Access token (1 dia). |
| `app.security.refresh.ttl-seconds` | `REFRESH_TTL_SECONDS` | `1209600` | Sim | Sessão de refresh (14 dias). |
| `spring.mail.host` | `MAIL_HOST` | `localhost` | Sim (fixo `mailpit`) | No host: Mailpit em localhost. No Compose: nome do serviço. |
| `spring.mail.port` | `MAIL_PORT` | `1025` | Sim (fixo) | SMTP do Mailpit. |
| `spring.mail.username` | `MAIL_USERNAME` | vazio | Não | Só SMTP autenticado (Gmail, SES, …). Omita com Mailpit. |
| `spring.mail.password` | `MAIL_PASSWORD` | vazio | Não | Idem. |
| `app.mail.from` | `MAIL_FROM` | `oficina@localhost` | Sim | Remetente. |
| `app.mail.invite-base-url` | `INVITE_BASE_URL` | `http://localhost:8080/invite` | Sim | Prefixo do link do convite (`/{token}` se a base não tiver `?`). O default aponta para o `GET /invite/{token}` público. |
| `app.notifications.budget-token` | `BUDGET_NOTIFICATION_TOKEN` | `local-budget-notification-token` | Sim | Segredo do header `X-Notification-Token` em `POST /integrations/budget-decisions`. Troque fora do ambiente local. |

O serviço `postgres` do Compose também recebe `POSTGRES_DB=autorepairshop`, `POSTGRES_USER` de `DATABASE_USERNAME` e `POSTGRES_PASSWORD` de `DATABASE_PASSWORD`.

Hibernate: `ddl-auto=validate` (Flyway é dono do schema). `open-in-view=false`.

`spring.mail.properties.mail.smtp.auth` e `starttls.enable` estão `false`. SMTP autenticado precisa de `MAIL_USERNAME` / `MAIL_PASSWORD` **e** ligar auth/STARTTLS nas properties — só as env do Compose não bastam.

## Arquivos de execução

| Arquivo | Papel |
|---|---|
| [`docker-compose.yml`](../docker-compose.yml) | Código local. Constrói o Dockerfile, grava `auto-repair-shop:local` e sobe Postgres e Mailpit |
| [`k8s/app/kustomization.yaml`](../k8s/app/kustomization.yaml) | Imagem publicada. `newName` é o registro e `newTag` é a versão. Hoje `public.ecr.aws/w2x8c5h2/auto-repair-shop` e `v0.1.0` |
| [`infra/kind-config.yaml`](../infra/kind-config.yaml) | Nó do kind local: `kindest/node:v1.37.0`, API no host `30080` e Mailpit no host `30025` |
| [`scripts/up-cluster.sh`](../scripts/up-cluster.sh) | Sobe esse kind, aplica os manifestos e nomeia o nó com a versão do Kubernetes |

`docker compose up --build` é o caminho local. `bash scripts/up-cluster.sh` é o caminho da imagem publicada. O script inicia o container `autorepairshop-control-plane` se ele só estiver parado e cria o cluster apenas quando esse container não existe.

No Kubernetes, o ConfigMap `api-config` e o Secret `api-secret` em [`k8s/base`](../k8s/base) carregam o equivalente. O Secret guarda `DATABASE_PASSWORD`, `JWT_SECRET` e `BUDGET_NOTIFICATION_TOKEN`. `api-config` não é uma URL.

## Compose vs host

O `.env` pode ter `DATABASE_URL=…localhost…` para Gradle/IDE. O serviço `api` **ignora** esse URL e força:

```
DATABASE_URL=jdbc:postgresql://postgres:5432/autorepairshop
MAIL_HOST=mailpit
MAIL_PORT=1025
```

Se você exportar `DATABASE_URL` com `localhost` **e** rodar a API no container, a conexão falha (localhost dentro do container não é o Postgres).

## Segredos

HS256 precisa de **pelo menos 32 bytes**. Localmente o default de [`.env.example`](../.env.example) serve:

```
DATABASE_PASSWORD=postgres
JWT_SECRET=change-me-to-a-32-byte-or-longer-secret-key
```

Em qualquer ambiente compartilhado:

```bash
openssl rand -base64 48
```

Use o **mesmo** `JWT_SECRET` em todas as instâncias da API. Trocar o secret invalida access tokens já emitidos (refresh sessions no banco continuam válidas até expirar ou logout, mas o próximo access será assinado com a chave nova).

Não commite segredos reais. `.env` e `application-local.properties` estão no `.gitignore`.

Testes usam [`src/test/resources/application.properties`](../src/test/resources/application.properties) (`test-only-jwt-secret-key-32-bytes-min`).

## GitHub Actions

Crie em **Settings → Secrets and variables → Actions**. Os workflows leem `${{ vars.* }}` e `${{ secrets.* }}` — **não** usam o `.env` do Docker.

| Nome | Tipo | Valor | Obrigatório |
|---|---|---|---|
| `JAVA_VERSION` | Variable | `17` | Sim. [`.github/actions/setup`](../.github/actions/setup). |
| `GRADLE_VERSION` | Variable | `9.5.1` | Sim. Igual ao Gradle Wrapper. |
| `AWS_REGION` | Variable | `us-east-1` | Sim para o CD publicar no ECR. |
| `ECR_REPOSITORY` | Variable | `auto-repair-shop` | Não. Esse é o default do workflow. |
| `AWS_ROLE_ARN` | Secret | `arn:aws:iam::<conta>:role/github-auto-repair-shop` | Sim para o CD. A role é assumida por OIDC, sem access key no GitHub. |
| `SONAR_TOKEN` | Secret | \*\*\* | Sim para o job Sonar. 403 costuma ser token ausente, errado ou sem Execute Analysis. |

### Amazon ECR Public

O CD autentica na AWS com OIDC e envia a imagem para o registro público. A API pública do ECR só existe em `us-east-1`. A URI deste repositório é:

`public.ecr.aws/w2x8c5h2/auto-repair-shop:<tag>`

O alias `w2x8c5h2` vem de `aws ecr-public describe-registries`. O workflow cria o repositório `auto-repair-shop` se ele ainda não existir.

| Disparo | Tag da imagem e da release |
|---|---|
| Push em `main` | o pipeline calcula a próxima tag a partir dos commits (`feat:` sobe minor, mudança incompatível sobe major, o resto sobe patch) e publica `latest` |
| Disparo manual | a escolha patch, minor ou major define a próxima tag, e também publica `latest` |
| Push de uma tag Git `v*` | a mesma tag, sem abrir outra release |

O cluster kind do CD baixa a tag publicada naquele job. No seu cluster, a versão padrão está em `newTag` de [`k8s/app/kustomization.yaml`](../k8s/app/kustomization.yaml). O passo a passo da conta está em [AWS.md](AWS.md).

Não crie `GITHUB_TOKEN`: o GitHub injeta sozinho (comentário de cobertura no PR, checkout, decoração Sonar).

Não crie `JWT_SECRET` nem `DATABASE_PASSWORD` no GitHub. Os jobs de teste usam `ci-test-jwt-secret-key-of-32-bytes-min` e `postgres` (Testcontainers, não o banco do Compose). `DATABASE_URL`, `MAIL_*` e `INVITE_BASE_URL` não entram no Actions.

## Produção (checklist)

- `JWT_SECRET` forte, ≥ 32 bytes, igual em todos os nós
- `DATABASE_PASSWORD` diferente do default
- SMTP real: `MAIL_HOST` / `MAIL_PORT` / `MAIL_FROM`, e se o provedor exigir auth: usuário, senha, `mail.smtp.auth=true`, STARTTLS
- `INVITE_BASE_URL` apontando para a URL pública onde o cliente abre o convite
- Postgres 16 (ou compatível com as migrations Flyway em `src/main/resources/db/migration/`)
- Não ligue `ddl-auto=update` / `create`: o schema é Flyway
