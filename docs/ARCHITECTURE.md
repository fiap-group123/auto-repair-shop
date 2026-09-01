# Arquitetura — Auto Repair Shop

Backend API de um MVP de oficina (FIAP Tech Challenge). Um único deployável Kotlin/Spring Boot, com DDD tático em seis bounded contexts.

Este documento descreve **como o sistema funciona**: camadas, contextos, fluxos e integração. A escolha do banco está em [ADR 0001 — PostgreSQL](adr/001-postgresql.md).

A lista completa de rotas está em [ENDPOINTS.md](ENDPOINTS.md). Erros HTTP: [ERRORS.md](ERRORS.md). Subir o projeto: [GETTING_STARTED.md](GETTING_STARTED.md). Variáveis e Compose: [CONFIGURATION.md](CONFIGURATION.md).

## Visão geral

| | |
|---|---|
| Forma | Monólito em camadas, um módulo Gradle |
| Linguagem | Kotlin 2.4, JDK 17 |
| Framework | Spring Boot 4.1 (Web MVC, Security, JPA, Validation) |
| Persistência | PostgreSQL 16, Flyway, Hibernate `validate` |
| Auth | JWT (access) + refresh session no banco, BCrypt |
| Integração interna | Eventos de domínio in-process (Spring) |

Pacote raiz: `br.com.autorepairshop`.

```
src/main/kotlin/br/com/autorepairshop/
├── api/                    # Adaptador HTTP (não é um bounded context)
├── accessidentity/         # Usuários, JWT, convites, sessões
├── customer/               # Clientes e veículos
├── catalog/                # Itens de serviço de uma OS
├── inputmanagment/         # Estoque da oficina e linhas de peça da OS
├── serviceandexecution/    # Ciclo de vida da OS
├── budget/                 # Orçamento da OS (total e aprovação)
└── shared/                 # Kernel: AggregateRoot, UseCase, Money, eventos, mail
```

Cada contexto de negócio segue `domain` → `application` → `infrastructure`. HTTP, JWT da cadeia de filtros e OpenAPI ficam em `api`.

## Camadas

A regra de dependência: o domínio não conhece Spring nem HTTP. A aplicação orquestra. A infraestrutura implementa portas. A API só traduz HTTP.

### domain

Regras e invariantes. Sem anotações de framework.

| Peça | Papel |
|---|---|
| `AggregateRoot` | Identidade + lista de `DomainEvent` pendentes |
| `Entity` / `ValueObject` | Identidade vs igualdade por valor |
| Agregados | `Customer`, `Vehicle`, `ServiceOrder`, `Service`, `ExtraService`, `Inventory`, `Part`, `Budget`, `User`, `CustomerInvite`, `RefreshSession` |
| Value objects | Documento, placa, `Money`, `Role`, status, e-mails de login, etc. |
| Portas de repositório | Interfaces no domínio (`CustomerRepository`, `UserRepository`, …) |
| Eventos | Fatos do agregado (`CustomerRegistered`, `ServiceRegistered`, …) |
| Exceções | `DomainException` e hierarquias por contexto |

O agregado registra eventos com `registerEvent`. Quem publica é o caso de uso, depois do `save`.

Fábricas no companion: `register` / `open` / `issue` para criação; `rehydrate` (interno) para a persistência remontar o agregado sem disparar eventos de novo.

### application

Orquestração e casos de uso. Implementa `UseCase<IN, OUT>` com `execute(input)`.

| Peça | Papel |
|---|---|
| `*UseCase` | Um fluxo de aplicação (`@Service`, em geral `@Transactional`) |
| `*Command` | Entrada do caso de uso (já sem HTTP) |
| `*Response` | Saída estável para a API |
| `*Mapper` | Agregado → response |
| Listeners | Reagem a `DomainEvent` de outro (ou do mesmo) contexto |
| Portas de aplicação | `EventPublisher`, `EmailSender`, `CustomerAntiLayer`, `TokenIssuer`, `PasswordHasher`, `ActorProvider` |

O caso de uso carrega o agregado, aplica a regra, persiste e chama `events.publish(aggregate)` ou `events.publish(event)`.

### infrastructure

Adaptadores concretos.

| Peça | Papel |
|---|---|
| `*Entity` | Modelo JPA, espelha a tabela |
| `*JpaRepository` | Spring Data |
| `*RepositoryImpl` | Mapeia Entity ↔ agregado (`rehydrate` / colunas) |
| `*Column` | Enums persistidos (`UserRoleColumn`, `ServiceOrderStatusColumn`) |
| `SpringEventPublisher` | Encaminha para `ApplicationEventPublisher` e limpa a lista do agregado |
| `NimbusTokenIssuer` / `BcryptPasswordHasher` | JWT e hash de senha |
| `CustomerAntiLayerAdapter` | Lê Customer sem o domínio de auth importar o agregado `Customer` |
| `JavaMailEmailSender` | SMTP |

### api

Borda HTTP. Não contém regra de negócio.

| Peça | Papel |
|---|---|
| Controllers | Mapeiam `*Request` → `*Command` e devolvem `*Response` |
| `SecurityConfig` | Rotas públicas, papéis, bootstrap do primeiro usuário |
| `ActiveUserFilter` | Recusa JWT de usuário inativo |
| `CurrentUser` / `SecurityContextActorProvider` | Ator autenticado para `AccessGuard` |
| `*ApiExceptionHandler` | Exceções de domínio → `ProblemDetail` (por contexto) |
| `OpenApiConfig` | Swagger / OpenAPI |

### shared

Kernel usado por todos os contextos: `AggregateRoot`, `Entity`, `ValueObject`, `DomainEvent`, `UseCase`, `Money`, `Email`, `EventPublisher`, `EmailSender` e as implementações Spring/JavaMail.

## Fluxo de um request

Escrita típica (ex.: cadastrar cliente):

1. Controller recebe o JSON, monta o Command e chama `useCase.execute`.
2. O caso de uso (`@Transactional`) valida unicidade, monta value objects e chama a fábrica do agregado.
3. `repository.save` grava a Entity.
4. `events.publish(aggregate)` publica cada `DomainEvent` e limpa a lista.
5. Listeners `@EventListener` rodam **dentro da mesma transação** (orçamento, convite, ativar/desativar user).
6. Listeners `@TransactionalEventListener(AFTER_COMMIT)` + `@Async` enviam e-mail **depois** do commit.
7. A API devolve o Response. Exceções de domínio viram `422` (ou o status do handler do contexto).

Leitura: o caso de uso carrega o agregado (ou uma lista), aplica `AccessGuard` se for dado de cliente, e mapeia para Response. Detalhe de OS usa `ServiceOrderAssembler` para incluir os `serviceIds` do catalog e os `partIds` do inputmanagment.

## Bounded contexts

Referências entre contextos são **UUIDs**, não objetos de agregado. `ServiceOrder` guarda `customerId` e `vehicleId`; `Service` guarda `serviceOrderId`; `User` guarda `customerId` opcional.

### accessidentity

Identidade da oficina: staff, cliente com login, JWT e convite.

**Agregados**

| Agregado | Invariantes |
|---|---|
| `User` | `CLIENT` exige `customerId`; staff não pode ter `customerId`; e-mail de login único |
| `CustomerInvite` | Token hasheado, expiração (72h), revogação dos convites abertos ao reemitir |
| `RefreshSession` | Ligada ao usuário; rotação no refresh; revogação no logout |

**Value objects / segurança:** `Role` (`CLIENT`, `RECEPTIONIST`, `MECHANIC`, `MANAGER`), `LoginEmail`, `RawPassword`, `HashedPassword`, `InviteToken`.

**Casos de uso:** `RegisterUserUseCase`, `LoginUseCase`, `RefreshTokenUseCase`, `LogoutUseCase`, `IssueCustomerInviteUseCase`, `CompleteInviteUseCase`, `FindCustomerInviteUseCase`, `RequireActiveUserUseCase`.

**Portas:** `UserRepository`, `CustomerInviteRepository`, `RefreshSessionRepository`, `TokenIssuer`, `PasswordHasher`, `ActorProvider`, `CustomerAntiLayer`.

**Integração com Customer:** o domínio de auth não importa `Customer`. `IssueCustomerInviteUseCase` lê um `CustomerRecord` via `CustomerAntiLayer` (implementado por `CustomerAntiLayerAdapter`).

**Listeners**

| Listener | Origem | Efeito |
|---|---|---|
| `CustomerInviteEventListener` | `CustomerRegistered` | Emite convite na mesma transação |
| `CustomerAccountListener` | `CustomerDeactivated` / `CustomerReactivated` | Desativa ou reativa o `User` ligado |
| `InviteMailListener` | `CustomerInviteIssued` | E-mail do link **depois** do commit |

**Regras de acesso no application:** `AccessGuard.requireCustomer` impede que um `CLIENT` leia outro cliente. A cadeia HTTP está em `SecurityConfig` (ver [Segurança](#segurança)).

**Persistência:** tabelas `users`, `customer_invites`, `refresh_sessions`.

### customer

Cadastro de pessoas/empresas e dos veículos que elas possuem.

**Agregados**

| Agregado | Invariantes |
|---|---|
| `Customer` | CPF ou CNPJ válido e único; inativo não renomeia nem atualiza contato; soft delete |
| `Vehicle` | Placa Mercosul (`ABC1D23`) ou antiga (`ABC-1234`), única; dono ativo para veículo novo; transferência para outro cliente; soft delete |

**Value objects:** `Document` / `DocumentType`, `PersonName`, `ContactInfo`, `EmailAddress`, `PhoneNumber`, `LicensePlate` / `LicensePlateType`, `ModelYear`, `CustomerId`, `VehicleId`.

**Casos de uso (cliente):** `RegisterCustomerUseCase`, `UpdateCustomerUseCase`, `FindCustomerUseCase`, `FindCustomerByDocumentUseCase`, `ListCustomersUseCase`, `DeactivateCustomerUseCase`, `ReactivateCustomerUseCase`.

**Casos de uso (veículo):** `RegisterVehicleUseCase`, `FindVehicleUseCase`, `FindVehicleByPlateUseCase`, `ListVehiclesByOwnerUseCase`, `UpdateVehicleSpecUseCase`, `ChangeVehiclePlateUseCase`, `TransferVehicleUseCase`, `DeactivateVehicleUseCase`, `ReactivateVehicleUseCase`.

**Eventos:** `CustomerRegistered`, `CustomerDeactivated`, `CustomerReactivated`. Veículo não emite eventos hoje.

**Persistência:** `customers`, `vehicles`. Documento e placa únicos no banco.

### serviceandexecution

Ordem de serviço (OS): ciclo de vida. O orçamento vive no contexto Budget.

**Agregado `ServiceOrder`**

Status obrigatório, uma transição por vez:

```
RECEIVED → IN_DIAGNOSIS → WAITING_APPROVAL → BUDGET_APPROVED → IN_EXECUTION → FINISHED → DELIVERED
                              ↘ BUDGET_REJECTED 
```

| Transição | Método | Regra extra |
|---|---|---|
| abrir | `open` | Cliente ativo; veículo do cliente; sem OS aberta no veículo |
| diagnóstico | `startDiagnosis` | Status `RECEIVED`. Emite `DiagnosisStarted` (cria o Budget) |
| fechar diagnóstico | `finishDiagnosis` | Status `IN_DIAGNOSIS`. O caso de uso exige `Budget.total > 0` |
| aprovar orçamento | `budgetApprove` | Status `WAITING_APPROVAL` → `BUDGET_APPROVED`. Listeners de `BudgetApproved` e `BudgetTraded` |
| rejeitar orçamento | `budgetReject` | Status `WAITING_APPROVAL` → `BUDGET_REJECTED`. Listener de `BudgetRejected` |
| executar | `startExecution` | Status `BUDGET_APPROVED` → `IN_EXECUTION` |
| concluir | `finish` | Status `IN_EXECUTION`; grava duração |
| entregar | `deliver` | Status `FINISHED` |

**Casos de uso:** `RegisterServiceOrderUseCase`, `FindServiceOrderUseCase`, `ListServiceOrdersUseCase`, `ListServiceOrdersByCustomerIdUseCase`, `StartDiagnosisUseCase`, `FinishDiagnosisUseCase`, `ApproveServiceOrderUseCase`, `RejectServiceOrderUseCase`, `StartExecutionUseCase`, `FinishServiceOrderUseCase`, `DeliverServiceOrderUseCase`.

`RegisterServiceOrderUseCase` lê `CustomerRepository` e `VehicleRepository` na mesma transação (consistência: dono, ativo, placa). Não importa os agregados no domínio da OS — só os IDs.

**Read model:** `ServiceOrderAssembler` busca os `Service` do catalog e os `Part` do inputmanagment e preenche `serviceIds` e `partIds` na response. O detalhe da OS não embute nome, preço nem total.

**Listeners**

| Listener | Quando | Transação |
|---|---|---|
| `BudgetApprovedEventListener` | `BudgetApproved` | Mesmo commit: OS → `BUDGET_APPROVED` |
| `BudgetRejectedEventListener` | `BudgetRejected` | Mesmo commit: OS → `BUDGET_REJECTED` |
| `BudgetTradedEventListener` | `BudgetTraded` | Mesmo commit: OS → `BUDGET_APPROVED` |
| `ServiceOrderStatusMailListener` | Eventos de status da OS | `AFTER_COMMIT`, assíncrono |

**Persistência:** `service_orders`. Índice parcial `uk_service_orders_open_vehicle`: no máximo uma OS com status diferente de `DELIVERED` por veículo.

### budget

Orçamento de uma OS: um por ordem, total somado dos `Service` da OS, dos `ExtraService` faturáveis e das linhas `Part` (`unitPrice * quantity`).

**Agregado `Budget`**

```
WAITING_APPROVAL → APPROVED | REJECTED | TRADED
```

| Ação | Regra |
|---|---|
| registrar | Disparado por `DiagnosisStarted`; OS existe; um budget por OS; total inicia na soma dos itens já cadastrados |
| recalcular | Soma dos `basePrice` dos `Service` + extras `APPROVED`/`IN_PROGRESS`/`FINISHED` + `Part.unitPrice * quantity`; zero é permitido; um extra aprovado pode aumentar um orçamento já `APPROVED` |
| aprovar | Só de `WAITING_APPROVAL`; emite `BudgetApproved`; OS → `BUDGET_APPROVED` |
| rejeitar | Só de `WAITING_APPROVAL`; emite `BudgetRejected`; OS → `BUDGET_REJECTED` |
| negociar | Só de `WAITING_APPROVAL`; emite `BudgetTraded`; OS → `BUDGET_APPROVED` |

**Casos de uso:** `RegisterBudgetUseCase`, `FindBudgetUseCase`, `ApproveBudgetUseCase`, `RejectBudgetUseCase`, `TradeBudgetUseCase`, `DeleteBudgetUseCase`, `CalculateBudgetTotalUseCase`.

**Listeners**

| Listener | Quando | Transação |
|---|---|---|
| `RegisterBudgetEventListener` | `DiagnosisStarted` | Mesmo commit da OS |
| `CalculateBudgetEventListener` | `ServiceRegistered`, `ServicePriceChanged`, `ServiceRemoved`, `ExtraServiceApproved`, `ExtraServiceRejected`, `PartRegistered`, `PartQuantityChanged`, `PartRemoved` | Mesmo commit do item |

**Persistência:** `budgets`. `service_order_id` único → `service_orders`.

### catalog

Itens de serviço **de uma OS**, não um catálogo da oficina. `Service` é o diagnóstico. `ExtraService` é reparo adicional depois do orçamento aprovado e **não vira** `Service`.

**Agregado `Service`**

```
WAITING → IN_PROGRESS → FINISHED
```

| Ação | Regra |
|---|---|
| registrar | OS em `RECEIVED`, `IN_DIAGNOSIS` ou `WAITING_APPROVAL`; nome único **naquela** OS (também contra extras) |
| alterar preço | Emite `ServicePriceChanged` |
| iniciar | Só de `WAITING` |
| finalizar | Só de `IN_PROGRESS`; grava duração |
| remover | Só em `WAITING`; emite `ServiceRemoved` |

**Casos de uso:** `RegisterServiceUseCase`, `FindServiceUseCase`, `ListServicesUseCase`, `ListServicesByServiceOrderIdUseCase`, `ListServicesByCustomerIdUseCase`, `UpdateServiceUseCase`, `DeleteServiceUseCase`, `InProgressServiceUseCase`, `FinishServiceUseCase`, `AverageExecutionTimeUseCase`.

`RegisterServiceUseCase` consulta `ServiceOrderRepository` para garantir que a OS existe e ainda aceita itens de diagnóstico.

**Agregado `ExtraService`**

```
PENDING → APPROVED | REJECTED
APPROVED → IN_PROGRESS → FINISHED
```

| Ação | Regra |
|---|---|
| registrar | OS em `BUDGET_APPROVED` ou `IN_EXECUTION`; nome único **naquela** OS (também contra `Service`); emite `ExtraServiceRegistered` |
| aprovar | Só de `PENDING`; emite `ExtraServiceApproved` |
| rejeitar | Só de `PENDING`; emite `ExtraServiceRejected` |
| iniciar | Só de `APPROVED` |
| finalizar | Só de `IN_PROGRESS`; grava duração |

**Casos de uso:** `RegisterExtraServiceUseCase`, `FindExtraServiceUseCase`, `ListExtraServicesByServiceOrderIdUseCase`, `ApproveExtraServiceUseCase`, `RejectExtraServiceUseCase`, `InProgressExtraServiceUseCase`, `FinishExtraServiceUseCase`.

`AccessGuard.requireCustomer` no find/list/approve/reject (dono da OS).

**Eventos:** `ServiceRegistered`, `ServicePriceChanged`, `ServiceRemoved` recalculam o Budget. `ExtraServiceApproved` e `ExtraServiceRejected` também. `PENDING` e `REJECTED` não entram no total; `APPROVED`, `IN_PROGRESS` e `FINISHED` entram. `PartRegistered`, `PartQuantityChanged` e `PartRemoved` também recalculam.

**Listeners**

| Listener | Quando | Transação |
|---|---|---|
| `ExtraServiceRegisteredMailListener` | `ExtraServiceRegistered` | `AFTER_COMMIT`, assíncrono |

**Persistência:** `services` e `extra_services` (`service_order_id` → `service_orders`). Unique `(service_order_id, name)` em cada tabela.

### inputmanagment

Catálogo de estoque da oficina (`Inventory`) e linha da OS (`Part`). O estoque existe **sem** OS. A OS só guarda `partIds`, como `serviceIds`.

**Agregado `Inventory`**

| Ação | Regra |
|---|---|
| registrar | Nome único na oficina; `kind` `PART` ou `SUPPLY`; `stock` ≥ 0; nasce `active` |
| alterar | Nome, preço e kind só com item ativo; nome continua único |
| estoque | `setStock` (absoluto, PATCH) e `adjustStock` (delta da OS); recusa saldo negativo |
| desativar | Soft delete (`active = false`); não pode incluir na OS |
| reativar | Só de inativo |

**Agregado `Part`**

Linha da OS: `serviceOrderId` + `inventoryId` + `quantity` (≥ 1) + `unitPrice` (snapshot do catálogo). Unique `(service_order_id, inventory_id)`. Sem ciclo de execução.

| Ação | Regra |
|---|---|
| incluir | OS em `RECEIVED`, `IN_DIAGNOSIS` ou `WAITING_APPROVAL`; inventory ativo; baixa estoque; emite `PartRegistered` |
| alterar quantidade | Mesmo recorte de status; delta no estoque; emite `PartQuantityChanged` |
| remover | Mesmo recorte; devolve estoque; emite `PartRemoved` |

**Casos de uso:** `RegisterInventoryUseCase`, `FindInventoryUseCase`, `ListInventoriesUseCase`, `UpdateInventoryUseCase`, `DeactivateInventoryUseCase`, `ReactivateInventoryUseCase`, `AdjustInventoryStockUseCase`, `RegisterPartUseCase`, `FindPartUseCase`, `ListPartsByServiceOrderIdUseCase`, `UpdatePartUseCase`, `DeletePartUseCase`.

`AccessGuard.requireCustomer` no find/list de `Part` (dono da OS).

Fora deste recorte: peça extra depois do orçamento aprovado continua `ExtraService`.

**Persistência:** `inventories` (nome único) e `parts` (`service_order_id` → `service_orders`, `inventory_id` → `inventories`). Unique `(service_order_id, inventory_id)`.

### api (borda HTTP)

| Controller | Base | Contexto |
|---|---|---|
| `AuthController` | `/auth` | accessidentity |
| `InviteController` | `/invite` | accessidentity |
| `CustomerController` | `/customers` | customer |
| `VehicleController` | `/vehicles` | customer |
| `ServiceOrderController` | `/service-orders` | serviceandexecution |
| `BudgetController` | `/budgets` | budget |
| `ServiceController` | `/services` | catalog |
| `ExtraServiceController` | `/extra-services` | catalog |
| `InventoryController` | `/inventories` | inputmanagment |
| `PartController` | `/parts` | inputmanagment |

Handlers: `AuthApiExceptionHandler`, `CustomerApiExceptionHandler`, `ServiceOrderApiExceptionHandler`, `CatalogApiExceptionHandler`, `InventoryApiExceptionHandler`, `BudgetApiExceptionHandler`, mais `ApiExceptionHandler` genérico (`DomainException` → `422`).

Rotas públicas: login, refresh, logout, preview/conclusão de convite (`/invite/**`), Swagger, OpenAPI, `/error`. `POST /auth/users` é público só com banco sem usuários; depois exige `MANAGER`.

## Integração entre contextos

Três padrões, usados de propósito (não é acaso):

| Padrão | Quando | Exemplo |
|---|---|---|
| Evento in-process | Efeito colateral depois de um fato do agregado | Cliente cadastrado → convite; item criado → recálculo do budget; budget aprovado → OS em execução |
| Porta anti-corrupção | Um contexto precisa de um recorte do outro | `CustomerAntiLayer` / `CustomerRecord` |
| Repositório de outro contexto | A transação precisa da verdade agora | Abrir OS valida cliente e veículo; registrar serviço valida a OS |
| Assembler (leitura) | Response junta dados de dois contextos | `ServiceOrderAssembler` + `ServiceRepository` + `PartRepository` |

Não há broker, outbox nem event sourcing. O evento vive na memória do agregado até o `publish` e some.

## Segurança

- Sessão HTTP: stateless. Access JWT (~1 dia, claim `role`). Refresh persistido (~14 dias), rotacionado.
- Senha: BCrypt. O JWT é emitido por `NimbusTokenIssuer`.
- `ActiveUserFilter` recarrega o usuário e bloqueia inativo mesmo com token válido.
- Papéis na cadeia (`ROLE_*`). Isolamento de dados do `CLIENT`: `AccessGuard` nos casos de uso de leitura por `customerId`.
- Primeiro usuário do sistema: apenas `MANAGER`. Conta `CLIENT` só pelo convite (e-mail de contato do cliente).

## Persistência

Flyway é dono do schema (`src/main/resources/db/migration/`). Hibernate só valida (`spring.jpa.hibernate.ddl-auto=validate`). `open-in-view` está desligado. Motivo da escolha do motor: [ADR 0001](adr/001-postgresql.md).

| Tabela | Contexto | Observação |
|---|---|---|
| `customers` | customer | `document_id` único |
| `vehicles` | customer | placa única; `owner_id` → `customers` |
| `users` | accessidentity | e-mail único; `customer_id` opcional → `customers` |
| `customer_invites` | accessidentity | token hasheado |
| `refresh_sessions` | accessidentity | refresh rotacionável |
| `service_orders` | serviceandexecution | FKs para customer e vehicle; uma OS aberta por veículo |
| `budgets` | budget | `service_order_id` único → `service_orders` |
| `services` | catalog | FK para `service_orders` |
| `extra_services` | catalog | FK para `service_orders`; unique `(service_order_id, name)` |
| `inventories` | inputmanagment | nome único; `kind` `PART`/`SUPPLY`; soft delete `active` |
| `parts` | inputmanagment | FK para `service_orders` e `inventories`; unique `(service_order_id, inventory_id)` |

Timestamps em `TIMESTAMPTZ`. Soft delete de cliente e veículo: coluna `active`, histórico preservado.

## Qualidade (fronteira arquitetural)

Kover exige no mínimo **80%** de cobertura de linha em domain, application (casos de uso, mappers, assemblers), adapters de persistência, listeners e controllers. Isso marca o que o projeto considera núcleo.

Testes: `@Tag("unit")` sem container; `@Tag("integration")` sobe o contexto Spring com PostgreSQL via Testcontainers.

## Decisão de persistência

Por que PostgreSQL (e não um document store ou outro motor): [ADR 0001](adr/001-postgresql.md).
