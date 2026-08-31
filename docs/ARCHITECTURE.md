# Arquitetura — Auto Repair Shop

Backend API de um MVP de oficina (FIAP Tech Challenge). Um único deployável Kotlin/Spring Boot, com DDD tático em quatro bounded contexts.

Este documento descreve **como o sistema funciona**: camadas, contextos, fluxos e integração. A escolha do banco está em [ADR 0001 — PostgreSQL](adr/001-postgresql.md).

A lista completa de rotas está em [ENDPOINTS.md](ENDPOINTS.md). Getting started e configuração continuam no [README](../README.md).

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
├── api/                  # Adaptador HTTP (não é um bounded context)
├── authentication/       # Usuários, JWT, convites, sessões
├── customer/             # Clientes e veículos
├── catalog/              # Itens de serviço de uma OS
├── serviceorder/         # Ciclo de vida da OS e orçamento
└── shared/               # Kernel: AggregateRoot, UseCase, Money, eventos, mail
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
| Agregados | `Customer`, `Vehicle`, `ServiceOrder`, `Service`, `User`, `CustomerInvite`, `RefreshSession` |
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

Leitura: o caso de uso carrega o agregado (ou uma lista), aplica `AccessGuard` se for dado de cliente, e mapeia para Response. Detalhe de OS usa `ServiceOrderAssembler` para incluir os `serviceIds` do catalog.

## Bounded contexts

Referências entre contextos são **UUIDs**, não objetos de agregado. `ServiceOrder` guarda `customerId` e `vehicleId`; `Service` guarda `serviceOrderId`; `User` guarda `customerId` opcional.

### authentication

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

### serviceorder

Ordem de serviço (OS): ciclo de vida e total do orçamento.

**Agregado `ServiceOrder`**

Status obrigatório, uma transição por vez:

```
RECEIVED → IN_DIAGNOSIS → WAITING_APPROVAL → IN_EXECUTION → FINISHED → DELIVERED
```

| Transição | Método | Regra extra |
|---|---|---|
| abrir | `open` | Cliente ativo; veículo do cliente; sem OS aberta no veículo |
| diagnóstico | `startDiagnosis` | Status `RECEIVED` |
| fechar diagnóstico | `finishDiagnosis` | Status `IN_DIAGNOSIS` e `total > 0` |
| aprovar | `approve` | Status `WAITING_APPROVAL` |
| concluir | `finish` | Status `IN_EXECUTION`; grava duração |
| entregar | `deliver` | Status `FINISHED` |

O total **não** é editado na tela da OS: `updateBudgetTotal` é chamado por `RecalculateBudgetTotalUseCase` quando o catalog muda.

**Casos de uso:** `RegisterServiceOrderUseCase`, `FindServiceOrderUseCase`, `ListServiceOrdersUseCase`, `ListServiceOrdersByCustomerIdUseCase`, `StartDiagnosisUseCase`, `FinishDiagnosisUseCase`, `ApproveServiceOrderUseCase`, `FinishServiceOrderUseCase`, `DeliverServiceOrderUseCase`, `RecalculateBudgetTotalUseCase`.

`RegisterServiceOrderUseCase` lê `CustomerRepository` e `VehicleRepository` na mesma transação (consistência: dono, ativo, placa). Não importa os agregados no domínio da OS — só os IDs.

**Read model:** `ServiceOrderAssembler` busca os `Service` do catalog e preenche `serviceIds` na response. O detalhe da OS não embute nome/preço dos itens.

**Listeners**

| Listener | Quando | Transação |
|---|---|---|
| `ServiceOrderEventListener` | `ServiceRegistered`, `ServicePriceChanged`, `ServiceRemoved` | Mesmo commit do item |
| `ServiceOrderStatusMailListener` | Eventos de status da OS | `AFTER_COMMIT`, assíncrono |

**Persistência:** `service_orders`. Índice parcial `uk_service_orders_open_vehicle`: no máximo uma OS com status diferente de `DELIVERED` por veículo.

### catalog

Itens de serviço **de uma OS**, não um catálogo da oficina. Nome, preço, status de execução e duração.

**Agregado `Service`**

```
WAITING → IN_PROGRESS → FINISHED
```

| Ação | Regra |
|---|---|
| registrar | OS existe; nome único **naquela** OS |
| alterar preço | Emite `ServicePriceChanged` |
| iniciar | Só de `WAITING` |
| finalizar | Só de `IN_PROGRESS`; grava duração |
| remover | Só em `WAITING`; emite `ServiceRemoved` |

**Casos de uso:** `RegisterServiceUseCase`, `FindServiceUseCase`, `ListServicesUseCase`, `ListServicesByServiceOrderIdUseCase`, `ListServicesByCustomerIdUseCase`, `UpdateServiceUseCase`, `DeleteServiceUseCase`, `InProgressServiceUseCase`, `FinishServiceUseCase`, `AverageExecutionTimeUseCase`.

`RegisterServiceUseCase` consulta `ServiceOrderRepository` para garantir que a OS existe.

**Eventos:** `ServiceRegistered`, `ServicePriceChanged`, `ServiceRemoved` — o total da OS é a soma dos `basePrice`.

**Persistência:** `services` (`service_order_id` → `service_orders`).

### api (borda HTTP)

| Controller | Base | Contexto |
|---|---|---|
| `AuthController` | `/auth` | authentication |
| `InviteController` | `/invite` | authentication |
| `CustomerController` | `/customers` | customer |
| `VehicleController` | `/vehicles` | customer |
| `ServiceOrderController` | `/service-orders` | serviceorder |
| `ServiceController` | `/services` | catalog |

Handlers: `AuthApiExceptionHandler`, `CustomerApiExceptionHandler`, `ServiceOrderApiExceptionHandler`, `CatalogApiExceptionHandler`, mais `ApiExceptionHandler` genérico (`DomainException` → `422`).

Rotas públicas: login, refresh, logout, preview/conclusão de convite (`/invite/**`), Swagger, OpenAPI, `/error`. `POST /auth/users` é público só com banco sem usuários; depois exige `MANAGER`.

## Integração entre contextos

Três padrões, usados de propósito (não é acaso):

| Padrão | Quando | Exemplo |
|---|---|---|
| Evento in-process | Efeito colateral depois de um fato do agregado | Cliente cadastrado → convite; item criado → recálculo |
| Porta anti-corrupção | Um contexto precisa de um recorte do outro | `CustomerAntiLayer` / `CustomerRecord` |
| Repositório de outro contexto | A transação precisa da verdade agora | Abrir OS valida cliente e veículo; registrar serviço valida a OS |
| Assembler (leitura) | Response junta dados de dois contextos | `ServiceOrderAssembler` + `ServiceRepository` |

Não há broker, outbox nem event sourcing. O evento vive na memória do agregado até o `publish` e some.

## Segurança

- Sessão HTTP: stateless. Access JWT (~15 min, claim `role`). Refresh persistido (~14 dias), rotacionado.
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
| `users` | authentication | e-mail único; `customer_id` opcional → `customers` |
| `customer_invites` | authentication | token hasheado |
| `refresh_sessions` | authentication | refresh rotacionável |
| `service_orders` | serviceorder | FKs para customer e vehicle; uma OS aberta por veículo |
| `services` | catalog | FK para `service_orders` |

Timestamps em `TIMESTAMPTZ`. Soft delete de cliente e veículo: coluna `active`, histórico preservado.

## Qualidade (fronteira arquitetural)

Kover exige cobertura de linha alta em domain, application (casos de uso, mappers, assemblers), adapters de persistência, listeners e controllers. Isso marca o que o projeto considera núcleo.

Testes: `@Tag("unit")` sem container; `@Tag("integration")` sobe o contexto Spring com PostgreSQL via Testcontainers.

## Decisão de persistência

Por que PostgreSQL (e não um document store ou outro motor): [ADR 0001](adr/001-postgresql.md).
