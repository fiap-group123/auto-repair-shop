# Endpoints — Auto Repair Shop

Base URL: `http://localhost:8080`.

Enviar `Authorization: Bearer <accessToken>` em toda rota autenticada. Swagger: http://localhost:8080/swagger-ui.html. Pedidos prontos: [`http/auth.http`](../http/auth.http), [`http/customer.http`](../http/customer.http), [`http/service-order.http`](../http/service-order.http), [`http/budget.http`](../http/budget.http).

Um `CLIENT` só acessa **os próprios** cliente, veículos, OS e itens. Staff vê o que o papel permitir.

## Convenções

| Status | Quando |
|---|---|
| `200` / `201` / `204` | Sucesso (criação devolve `Location`) |
| `400` | Body malformado |
| `401` | Sem JWT ou token inválido |
| `403` | Papel insuficiente ou `CLIENT` acessando outro cliente |
| `409` | Duplicidade (documento, placa, e-mail, serviço na OS) |
| `422` | Regra de domínio (CPF/CNPJ, transição de status, orçamento vazio, cliente inativo) |

## Auth

| Método | Path | Auth | Status | Descrição |
|---|---|---|---|---|
| `POST` | `/auth/users` | Público (banco vazio) ou `MANAGER` | `201` | Cadastra staff. O primeiro usuário deve ser `MANAGER`. `CLIENT` é rejeitado. |
| `POST` | `/auth/login` | Público | `200` | Emite access + refresh. Access ~15 min; refresh ~14 dias. |
| `POST` | `/auth/refresh` | Público | `200` | Rotaciona o refresh e emite um novo JWT. |
| `POST` | `/auth/logout` | Público | `204` | Revoga a sessão de refresh. |
| `GET` | `/invite/{token}` | Público | `200` | Preview do convite (`customerName`, `expiresAt`). |
| `POST` | `/invite/{token}` | Público | `201` | Cria login `CLIENT` (`email` + `password`). |
| `POST` | `/invite/customer/{customerId}` | `RECEPTIONIST`, `MANAGER` | `204` | Reenvia o convite se o cliente ainda não tem usuário. |

### `POST /auth/users`

```json
{
  "email": "gerente@oficina.com",
  "password": "senha123",
  "role": "MANAGER",
  "customerId": null
}
```

`role`: `MANAGER` | `RECEPTIONIST` | `MECHANIC`. Staff omite `customerId`. Conta `CLIENT` só vem de `POST /invite/{token}`.

Resposta `201`:

```json
{
  "id": "00000000-0000-0000-0000-000000000000",
  "customerId": null,
  "email": "gerente@oficina.com",
  "role": "MANAGER",
  "active": true,
  "createdAt": "2026-08-30T12:00:00Z"
}
```

### `POST /auth/login`

```json
{
  "email": "gerente@oficina.com",
  "password": "senha123"
}
```

Resposta `200`:

```json
{
  "accessToken": "<jwt>",
  "refreshToken": "<opaque>",
  "tokenType": "Bearer",
  "expiresIn": 900
}
```

### `POST /auth/refresh` e `POST /auth/logout`

```json
{
  "refreshToken": "<opaque>"
}
```

Refresh devolve o mesmo formato de `POST /auth/login`. Logout não tem body de resposta.

### `GET /invite/{token}`

```json
{
  "customerName": "Ana Souza",
  "expiresAt": "2026-09-02T12:00:00Z"
}
```

### `POST /invite/{token}`

```json
{
  "email": "ana.souza@email.com",
  "password": "senha123"
}
```

Resposta `201`: `UserResponse` com `role` `CLIENT` e `customerId` preenchido.

## Customers

| Método | Path | Papéis | Status | Descrição |
|---|---|---|---|---|
| `POST` | `/customers` | `RECEPTIONIST`, `MANAGER` | `201` | Cadastra cliente (CPF ou CNPJ). Dispara convite no e-mail de contato. |
| `GET` | `/customers` | `RECEPTIONIST`, `MANAGER` | `200` | Lista todos os clientes. |
| `GET` | `/customers/document/{document}` | `RECEPTIONIST`, `MECHANIC`, `MANAGER` | `200` | Busca por CPF/CNPJ (formatado ou só dígitos). |
| `GET` | `/customers/{id}` | `CLIENT`, `RECEPTIONIST`, `MECHANIC`, `MANAGER` | `200` | Busca por id. |
| `PUT` | `/customers/{id}` | `RECEPTIONIST`, `MANAGER` | `200` | Atualiza nome e/ou contato (`email`, `phone`). |
| `DELETE` | `/customers/{id}` | `MANAGER` | `204` | Desativa (soft delete; histórico permanece). |
| `POST` | `/customers/{id}` | `MANAGER` | `204` | Reativa o cliente. |

### `POST /customers`

```json
{
  "documentId": "529.982.247-25",
  "name": "Ana Souza",
  "email": "ana.souza@email.com",
  "phone": "11987654321"
}
```

Resposta `201` / `200` (também nas buscas e no `PUT`):

```json
{
  "id": "00000000-0000-0000-0000-000000000000",
  "documentId": "529.982.247-25",
  "documentType": "CPF",
  "name": "Ana Souza",
  "email": "ana.souza@email.com",
  "phone": "11987654321",
  "active": true,
  "createdAt": "2026-08-30T12:00:00Z"
}
```

### `PUT /customers/{id}`

Todos os campos opcionais:

```json
{
  "name": "Ana Souza Silva",
  "email": "ana.silva@email.com",
  "phone": "11988887777"
}
```

## Vehicles

| Método | Path | Papéis | Status | Descrição |
|---|---|---|---|---|
| `POST` | `/vehicles` | `RECEPTIONIST`, `MANAGER` | `201` | Cadastra veículo de um cliente. |
| `GET` | `/vehicles/owner/{ownerId}` | `CLIENT`, `RECEPTIONIST`, `MECHANIC`, `MANAGER` | `200` | Lista veículos do cliente. |
| `GET` | `/vehicles/{id}` | `CLIENT`, `RECEPTIONIST`, `MECHANIC`, `MANAGER` | `200` | Busca por id. |
| `GET` | `/vehicles?plate={plate}` | `CLIENT`, `RECEPTIONIST`, `MECHANIC`, `MANAGER` | `200` | Busca por placa. |
| `PUT` | `/vehicles/{id}` | `RECEPTIONIST`, `MANAGER` | `200` | Atualiza marca, modelo, ano e/ou cor. |
| `PATCH` | `/vehicles/{id}/plate` | `RECEPTIONIST`, `MANAGER` | `200` | Troca a placa. |
| `PATCH` | `/vehicles/{id}/owner` | `RECEPTIONIST`, `MANAGER` | `200` | Transfere o veículo para outro cliente. |
| `DELETE` | `/vehicles/{id}` | `MANAGER` | `204` | Desativa (soft delete). |
| `POST` | `/vehicles/{id}` | `MANAGER` | `204` | Reativa o veículo. |

Placa: Mercosul (`ABC1D23`) ou formato antigo (`ABC-1234`). Não se cadastra veículo em cliente inativo.

### `POST /vehicles`

```json
{
  "ownerId": "00000000-0000-0000-0000-000000000000",
  "plate": "ABC1D23",
  "brand": "Toyota",
  "model": "Corolla",
  "year": 2024,
  "color": "Prata"
}
```

Resposta `201` / `200`:

```json
{
  "id": "00000000-0000-0000-0000-000000000000",
  "ownerId": "00000000-0000-0000-0000-000000000000",
  "plate": "ABC1D23",
  "plateType": "MERCOSUL",
  "brand": "Toyota",
  "model": "Corolla",
  "year": 2024,
  "color": "Prata",
  "active": true,
  "createdAt": "2026-08-30T12:00:00Z"
}
```

### `PUT /vehicles/{id}`

Todos os campos opcionais:

```json
{
  "brand": "Toyota",
  "model": "Corolla Cross",
  "year": 2025,
  "color": "Preto"
}
```

### `PATCH /vehicles/{id}/plate`

```json
{
  "plate": "XYZ1A23"
}
```

### `PATCH /vehicles/{id}/owner`

```json
{
  "newOwnerId": "00000000-0000-0000-0000-000000000000"
}
```

## Service orders

Ciclo: `RECEIVED` → `IN_DIAGNOSIS` → `WAITING_APPROVAL` → `BUDGET_APPROVED` → `IN_EXECUTION` → `FINISHED` → `DELIVERED`. Rejeição: `WAITING_APPROVAL` → `BUDGET_REJECTED`.

| Método | Path | Papéis | Status | Descrição |
|---|---|---|---|---|
| `POST` | `/service-orders` | `RECEPTIONIST`, `MANAGER` | `201` | Abre OS (CPF/CNPJ + `vehiclePlate`). Status `RECEIVED`. |
| `GET` | `/service-orders` | `RECEPTIONIST`, `MECHANIC`, `MANAGER` | `200` | Lista todas. |
| `GET` | `/service-orders/customer/{customerId}` | `CLIENT`, `RECEPTIONIST`, `MECHANIC`, `MANAGER` | `200` | Lista por cliente. `CLIENT` precisa ser o dono. |
| `GET` | `/service-orders/{id}` | `CLIENT`, `RECEPTIONIST`, `MECHANIC`, `MANAGER` | `200` | Detalhe (status, timestamps, `serviceIds`). |
| `POST` | `/service-orders/{id}/diagnosis` | `MECHANIC`, `MANAGER` | `200` | Inicia diagnóstico e cria o orçamento. |
| `POST` | `/service-orders/{id}/diagnosis/complete` | `MECHANIC`, `MANAGER` | `200` | Fecha diagnóstico e aguarda aprovação (`Budget.total` > 0). |
| `POST` | `/service-orders/{id}/complete` | `MECHANIC`, `MANAGER` | `200` | Conclui a execução. |
| `POST` | `/service-orders/{id}/deliver` | `RECEPTIONIST`, `MANAGER` | `200` | Entrega o veículo. |

Transições de status não têm body. Cada mudança envia e-mail ao contato do cliente (Mailpit local: http://localhost:8025).

### `POST /service-orders`

```json
{
  "document": "529.982.247-25",
  "vehiclePlate": "ABC1D23"
}
```

`document` aceita CPF/CNPJ formatado ou só dígitos. `vehiclePlate` aceita Mercosul ou formato antigo. Uma OS aberta por veículo. O veículo precisa pertencer ao cliente e o cliente precisa estar ativo.

Resposta `201` / `200`:

```json
{
  "id": "00000000-0000-0000-0000-000000000000",
  "customerId": "00000000-0000-0000-0000-000000000000",
  "vehicleId": "00000000-0000-0000-0000-000000000000",
  "serviceIds": [],
  "status": "RECEIVED",
  "createdAt": "2026-08-30T12:00:00Z",
  "startedAt": null,
  "finishedAt": null,
  "estimatedTime": null
}
```

O detalhe da OS só devolve `serviceIds`. Nome, preço e status dos itens saem em `/services`. O total sai em `GET /budgets/{serviceOrderId}`.

## Budgets

Um orçamento por OS. `{id}` é o **id da ordem de serviço**.

O orçamento é criado ao iniciar o diagnóstico (`POST /service-orders/{id}/diagnosis`). Não há `POST /budgets` nem `POST /service-orders/{id}/approve`.

| Método | Path | Papéis | Status | Descrição |
|---|---|---|---|---|
| `GET` | `/budgets/{id}` | `CLIENT`, `RECEPTIONIST`, `MECHANIC`, `MANAGER` | `200` | Busca pelo id da OS. |
| `POST` | `/budgets/{id}/approve` | `CLIENT`, `RECEPTIONIST`, `MANAGER` | `200` | Aprova o orçamento e passa a OS para `BUDGET_APPROVED`. |
| `POST` | `/budgets/{id}/reject` | `CLIENT`, `RECEPTIONIST`, `MANAGER` | `200` | Rejeita o orçamento e passa a OS para `BUDGET_REJECTED`. |
| `POST` | `/budgets/{id}/trade` | `CLIENT`, `RECEPTIONIST`, `MANAGER` | `200` | Negocia o orçamento e passa a OS para `BUDGET_APPROVED`. |
| `DELETE` | `/budgets/{id}` | `MANAGER` | `204` | Remove o orçamento da OS. |

### `GET /budgets/{id}`

Resposta `200`:

```json
{
  "id": "00000000-0000-0000-0000-000000000000",
  "serviceOrderId": "00000000-0000-0000-0000-000000000000",
  "total": 150.00,
  "status": "WAITING_APPROVAL",
  "createdAt": "2026-08-30T12:00:00Z",
  "finishedAt": null
}
```

## Catalog (itens da OS)

Serviço é linha da ordem, não catálogo da oficina. Status do item: `WAITING` → `IN_PROGRESS` → `FINISHED`.

| Método | Path | Papéis | Status | Descrição |
|---|---|---|---|---|
| `POST` | `/services` | `RECEPTIONIST`, `MANAGER` | `201` | Adiciona item à OS e recalcula o total do orçamento. |
| `GET` | `/services` | `RECEPTIONIST`, `MECHANIC`, `MANAGER` | `200` | Lista todos os itens. |
| `GET` | `/services/customer/{customerId}` | `CLIENT`, `RECEPTIONIST`, `MECHANIC`, `MANAGER` | `200` | Itens do cliente. `CLIENT` precisa ser o dono. |
| `GET` | `/services/service-order/{serviceOrderId}` | `CLIENT`, `RECEPTIONIST`, `MECHANIC`, `MANAGER` | `200` | Itens da OS. `CLIENT` precisa ser dono da OS. |
| `GET` | `/services/average-execution-time` | `RECEPTIONIST`, `MECHANIC`, `MANAGER` | `200` | Média de duração dos itens finalizados (`sampleSize`, `averageSeconds`). |
| `GET` | `/services/{id}` | `RECEPTIONIST`, `MECHANIC`, `MANAGER` | `200` | Busca um item. |
| `PUT` | `/services/{id}` | `MANAGER` | `200` | Atualiza nome e/ou preço. |
| `DELETE` | `/services/{id}` | `RECEPTIONIST`, `MANAGER` | `204` | Remove item ainda `WAITING`. |
| `POST` | `/services/{id}/in-progress` | `MECHANIC`, `MANAGER` | `200` | Inicia execução. |
| `POST` | `/services/{id}/finish` | `MECHANIC`, `MANAGER` | `200` | Finaliza e grava duração. |

`CLIENT` lista itens só por `GET /services/customer/{customerId}` ou `GET /services/service-order/{serviceOrderId}`.

### `POST /services`

```json
{
  "serviceOrderId": "00000000-0000-0000-0000-000000000000",
  "name": "Troca de oleo",
  "basePrice": 150.00
}
```

Nome único **naquela** OS.

Resposta `201` / `200`:

```json
{
  "id": "00000000-0000-0000-0000-000000000000",
  "serviceOrderId": "00000000-0000-0000-0000-000000000000",
  "name": "Troca de oleo",
  "basePrice": 150.00,
  "status": "WAITING",
  "createdAt": "2026-08-30T12:00:00Z",
  "startedAt": null,
  "finishedAt": null,
  "estimatedTime": null
}
```

### `PUT /services/{id}`

Todos os campos opcionais:

```json
{
  "name": "Troca de oleo e filtro",
  "price": 180.00
}
```

Alterar o preço recalcula o total do orçamento.

### `GET /services/average-execution-time`

```json
{
  "sampleSize": 12,
  "averageSeconds": 3600
}
```

## Docs e erros (públicos)

| Método | Path | Descrição |
|---|---|---|
| `GET` | `/swagger-ui.html` | Swagger UI |
| `GET` | `/swagger-ui/**` | Assets do Swagger |
| `GET` | `/v3/api-docs` | OpenAPI JSON |
| `GET` | `/v3/api-docs/**` | OpenAPI (grupos) |
| `GET` | `/.well-known/**` | Descoberta OAuth/OIDC do resource server |
| `GET` | `/error` | Fallback de erro do Spring |

## Papéis (resumo)

| Papel | Acesso típico |
|---|---|
| `MANAGER` | Tudo: cadastro de staff, desativar/reativar, alterar preço de item |
| `RECEPTIONIST` | Cadastra e atualiza cliente/veículo/OS/itens; entrega o veículo; não desativa |
| `MECHANIC` | Lê cliente/veículo/OS; diagnóstico, execução e conclusão |
| `CLIENT` | Lê os próprios dados; aprova, rejeita ou negocia o próprio orçamento |
