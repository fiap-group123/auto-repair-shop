# Mapeamento de erros

Erros da API usam [RFC 7807](https://www.rfc-editor.org/rfc/rfc7807) (`application/problem+json`). O campo `detail` é a mensagem do domínio (ou da cadeia de segurança).

```json
{
  "type": "about:blank",
  "title": "Unprocessable Entity",
  "status": 422,
  "detail": "Customer 529.***.***-25 is inactive."
}
```

CPF/CNPJ em `detail` saem mascarados (`Document.masked()`). Placa aparece formatada.

## Como o mapeamento funciona

1. Exceções de domínio (`*Exception` sealed, herdeiras de `DomainException`) são capturadas pelos `*ApiExceptionHandler` de cada contexto (`@Order(HIGHEST_PRECEDENCE)`).
2. Qualquer outra `DomainException` (hoje: valor negativo em `Money`, e-mail inválido via `Email.of` sem factory específica) cai no `ApiExceptionHandler` como **422**.
3. JSON ilegível (`HttpMessageNotReadableException`) → **400**.
4. Falha **na cadeia de filtros** (sem JWT, token expirado, papel insuficiente, usuário inativo no `ActiveUserFilter`) é escrita por `SecurityProblemSupport` como Problem JSON **401** / **403** — não passa pelos `@RestControllerAdvice`.

Não há `@Valid` nos DTOs: validação de documento, placa, senha, nome etc. acontece no domínio e vira 422 (ou o status da tabela abaixo).

## Status HTTP

| Status | Significado |
|---|---|
| `400` | Body malformado (JSON inválido, tipo incompatível) |
| `401` | Sem JWT, JWT inválido/expirado, credenciais erradas, refresh inválido ou reutilizado, usuário inativo **no filtro** |
| `403` | Papel não permitido na rota, ou `CLIENT` acessando outro cliente (`AccessGuard`) |
| `404` | Recurso inexistente |
| `409` | Duplicidade ou conflito de estado (e-mail, documento, placa, OS aberta, orçamento já existe) |
| `410` | Convite expirado (72 h) ou já usado |
| `422` | Regra de domínio (validação, transição de status, entidade inativa no caso de uso) |

Login de usuário inativo pelo caso de uso devolve **422** (`UserInactive`). Request autenticado com JWT de usuário inativo é barrado no filtro e devolve **401**.

## Authentication

Handler: `AuthApiExceptionHandler`. Também trata `AuthenticationException` do Spring, `AccessDeniedException` e `JwtException`.

| Exceção | Status | Quando | `detail` típico |
|---|---|---|---|
| `InvalidCredentials` | 401 | E-mail inexistente ou senha errada no login | `Invalid credentials.` |
| `Unauthenticated` | 401 | Staff tentando cadastrar usuário sem ator (depois do bootstrap) | `Authentication required.` |
| `InvalidRefresh` | 401 | Refresh inexistente, revogado ou expirado | `Refresh token was not found.` / `…revoked.` / `…has expired.` |
| `RefreshReuse` | 401 | Refresh já rotacionado (reuso) | `Refresh token was reused.` |
| Spring `AuthenticationException`, `JwtException` | 401 | Cadeia OAuth2 / JWT | `Authentication required.` / `Invalid or expired token.` |
| `Forbidden` | 403 | Não-MANAGER cadastrando staff; `CLIENT` lendo outro cliente | `Only a MANAGER can register staff.` / `Cannot access another customer.` |
| Spring `AccessDeniedException` | 403 | Papel fora do `hasRole` / `hasAnyRole` | `Access denied.` |
| `UserNotFound` | 404 | Usuário do JWT/refresh não existe mais | `User <id> was not found.` |
| `InviteNotFound` | 404 | Token de convite desconhecido | `Invite was not found.` |
| `LinkedCustomerNotFound` | 404 | Cliente do convite sumiu | — |
| `UserAlreadyExists` | 409 | E-mail de login já cadastrado | `User with this email already exists.` |
| `CustomerAlreadyHasUser` | 409 | Cliente já tem conta / convite concluído | — |
| `InviteExpired` | 410 | Convite com mais de 72 h | `Invite has expired.` |
| `InviteConsumed` | 410 | Convite já usado | `Invite was already used.` |
| `UserInactive` | 422 | Login ou operação com usuário desativado (caso de uso) | `User is inactive.` |
| `UserAlreadyActive` | 422 | Reativar quem já está ativo | `User is already active.` |
| `InvalidEmail` | 422 | E-mail de login fora do formato ou tamanho 5–60 | `Invalid email address format.` / `Email address must be between 5 and 60 characters.` |
| `InvalidPassword` | 422 | Senha &lt; 8, ou sem letra, ou sem dígito | `Password must be at least 8 characters.` / `Password must contain at least one letter and one digit.` |
| `InvalidRole` | 422 | Primeiro usuário não é MANAGER; `CLIENT` em `/auth/users`; role desconhecida | `The first user must be MANAGER.` / `CLIENT accounts are created from an invite.` / `Unknown role: …` |
| `LinkedCustomerInactive` | 422 | Convite para cliente inativo | — |

Filtro (`SecurityProblemSupport` + `ActiveUserFilter`):

| Situação | Status |
|---|---|
| Sem `Authorization`, JWT malformado ou expirado | 401 |
| Subject do JWT não é UUID | 401 (`JWT subject is invalid.`) |
| Usuário inativo ou inexistente com JWT ainda válido | 401 (`User is inactive.` / `User … was not found.`) |
| Autenticado, mas o papel não entra na regra da rota | 403 |

## Customer

Handler: `CustomerApiExceptionHandler`.

| Exceção | Status | Quando | `detail` típico |
|---|---|---|---|
| `CustomerNotFound` | 404 | Id ou documento inexistente | `Customer <id> was not found.` |
| `CustomerAlreadyExists` | 409 | CPF/CNPJ já cadastrado | — |
| `CustomerAlreadyActive` | 422 | Reativar cliente ativo | `Customer is already active.` |
| `CustomerInactive` | 422 | Alterar/usar cliente desativado (ex.: novo veículo, abrir OS) | `Customer … is inactive.` (documento mascarado) |
| `InvalidDocument` | 422 | CPF/CNPJ inválido (dígitos, CNPJ alfanumérico RFB) | `Invalid document id: ***25` |
| `InvalidPersonName` | 422 | Nome fora de 2–60 caracteres (após trim) | `Name must be between 2 and 60 characters.` |
| `InvalidPhoneNumber` | 422 | Telefone sem 10 ou 11 dígitos (com DDD) | `Phone number must have 10 or 11 digits including area code` |
| `InvalidEmailAddress` | 422 | E-mail de contato inválido (mesmas regras de `Email`) | `Invalid email address format.` |

## Vehicle

Mesmo handler do customer.

| Exceção | Status | Quando | `detail` típico |
|---|---|---|---|
| `VehicleNotFound` | 404 | Id ou placa inexistente | `Vehicle <id> was not found.` |
| `VehicleAlreadyExists` | 409 | Placa já cadastrada (alta ou troca) | — |
| `AlreadyOwnedByCustomer` | 409 | Transferir para quem já é o dono | — |
| `InvalidLicensePlate` | 422 | Fora de Mercosul (`ABC1D23`) ou antiga (`ABC-1234`) | `Invalid license plate: …` |
| `InvalidModelYear` | 422 | Ano &lt; 1900 ou &gt; ano corrente + 1 | `Model year must be between 1900 and <ano>.` |
| `InvalidVehicleName` | 422 | Marca/modelo/cor inválidos | — |
| `VehicleInactive` | 422 | Operar veículo desativado | `Vehicle with plate ABC1D23 is inactive.` |
| `VehicleAlreadyActive` | 422 | Reativar veículo ativo | `Vehicle is already active.` |

## Service order

Handler: `ServiceOrderApiExceptionHandler`.

| Exceção | Status | Quando | `detail` típico |
|---|---|---|---|
| `ServiceOrderNotFound` | 404 | OS inexistente (também em budget/catalog que consultam a OS) | `Service order <id> was not found.` |
| `OpenOrderAlreadyExists` | 409 | Já existe OS não `DELIVERED` naquele veículo | — |
| `VehicleNotOwnedByCustomer` | 409 | Placa não pertence ao documento informado | — |
| `InvalidStatusTransition` | 422 | Método de ciclo chamado no status errado | `Cannot transition from <STATUS>.` (mensagem do agregado) |
| `InvalidDuration` | 422 | Duração inválida ao concluir a OS | — |

Transições válidas: [ARCHITECTURE.md](ARCHITECTURE.md#serviceorder) e [ENDPOINTS.md](ENDPOINTS.md#service-orders).

Fechar diagnóstico com orçamento ausente ou total ≤ 0 lança `BudgetException.EmptyBudget` (**422**), não uma exceção de OS.

## Budget

Handler: `BudgetApiExceptionHandler`. O `{id}` das rotas `/budgets/{id}` é o **id da OS**.

| Exceção | Status | Quando | `detail` típico |
|---|---|---|---|
| `BudgetNotFound` | 404 | OS existe, orçamento não | `Budget of order <id> was not found.` |
| `ServiceOrderNotFound` | 404 | OS do orçamento não existe | — |
| `BudgetAlreadyExists` | 409 | Segundo orçamento na mesma OS (diagnóstico duplicado) | — |
| `EmptyBudget` | 422 | Encerrar diagnóstico com total ≤ 0 | `Cannot send an empty budget for approval.` |
| `InvalidBudgetStatusTransition` | 422 | Aprovar/rejeitar/negociar fora de `WAITING_APPROVAL` | `Cannot transition from <STATUS>.` |

## Catalog (Service e ExtraService)

Handler: `CatalogApiExceptionHandler`. Extra duplicado também usa `ServiceAlreadyExists` (nome único **na OS**, entre `Service` e `ExtraService`).

| Exceção | Status | Quando | `detail` típico |
|---|---|---|---|
| `ServiceNotFound` | 404 | Item de diagnóstico inexistente | `Service <id> was not found.` |
| `ExtraServiceNotFound` | 404 | Extra inexistente | `Extra service <id> was not found.` |
| `ServiceAlreadyExists` | 409 | Nome já usado naquela OS (serviço ou extra) | `Service <nome> already exists.` |
| `InvalidServiceName` | 422 | Nome fora de 2–60 caracteres | `Service name must be between 2 and 60 characters.` |
| `InvalidStatusTransition` | 422 | Item `Service` no status errado, **ou** OS que não aceita o item | `Cannot register a service from RECEIVED.` / `Cannot register an extra service from …` |
| `InvalidExtraServiceStatusTransition` | 422 | Extra no status errado (ex.: iniciar um `PENDING`) | — |
| `InvalidDuration` | 422 | Duração inválida ao finalizar item/extra | — |

Serviço de diagnóstico só entra com OS em `RECEIVED`, `IN_DIAGNOSIS` ou `WAITING_APPROVAL`. Extra só com OS em `BUDGET_APPROVED` ou `IN_EXECUTION`.

## Shared (sem handler próprio)

| Exceção | Status | Quando | `detail` típico |
|---|---|---|---|
| `DomainException` (fallback) | 422 | `Money.of` com valor negativo; e-mail via `Email.of` sem factory | `Amount cannot be negative.` / mensagens de e-mail |

## Exemplos por fluxo

| O que você fez | Status | Por quê |
|---|---|---|
| `POST /auth/users` com `role: "CLIENT"` | 422 | Cliente só pelo convite |
| `POST /auth/users` com `role: "RECEPTIONIST"` no banco vazio | 422 | Primeiro usuário tem que ser `MANAGER` |
| `POST /auth/login` senha errada | 401 | `InvalidCredentials` (mensagem genérica de propósito) |
| `POST /customers` com CPF inválido | 422 | `InvalidDocument` |
| `POST /vehicles` com placa `123` | 422 | `InvalidLicensePlate` |
| `POST /service-orders` com veículo de outro cliente | 409 | `VehicleNotOwnedByCustomer` |
| Segunda OS no mesmo carro (ainda não entregue) | 409 | `OpenOrderAlreadyExists` |
| `POST …/diagnosis/complete` sem itens (total 0) | 422 | `EmptyBudget` |
| `POST /budgets/{id}/approve` já aprovado | 422 | `InvalidBudgetStatusTransition` |
| `GET /customers/{id}` como `CLIENT` de outro id | 403 | `AccessGuard` → `Forbidden` |
| `GET /invite/{token}` depois de 72 h | 410 | `InviteExpired` |
| Body `{` cortado | 400 | `HttpMessageNotReadableException` |
