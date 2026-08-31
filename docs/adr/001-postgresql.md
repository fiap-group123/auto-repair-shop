# ADR 001: PostgreSQL como banco de dados

## Data

2026-08-23

## Status

Aceito

## Decisor 

Grupo 123 da Faculdade de Informática e Administração Paulista

## Contexto

O MVP modela um domínio relacional: o cliente possui veículos; a ordem de serviço aponta para um cliente e um veículo; os serviços (itens da OS) pendem dessa ordem com preço e tempo. Várias regras são de existência e unicidade — documento (CPF/CNPJ), placa, uma OS aberta por veículo, chaves estrangeiras.

O recálculo do orçamento precisa gravar no **mesmo commit** que a criação, a mudança de preço ou a remoção de um item. Um store sem transação ACID empurraria essa invariante para compensação na aplicação.

O time precisa de um motor único em desenvolvimento, testes de integração (Testcontainers) e produção local via Docker.

## Decisão

Usar **PostgreSQL 16** como único datastore da API.

O schema é versionado com Flyway (`src/main/resources/db/migration/`). O Hibernate apenas valida o mapeamento (`ddl-auto=validate`) e não cria tabela.

O Postgres cobre o que o domínio exige no banco:

- transações ACID para o total da OS e a mudança do item saírem juntos
- constraints (documento único, placa única, FKs, índice parcial `uk_service_orders_open_vehicle` onde `status <> 'DELIVERED'`)
- `UUID` nas identidades, `TIMESTAMPTZ` na linha do tempo da OS, `NUMERIC` para `Money`

## Consequências

- Unicidade e existência que o banco consegue garantir não dependem só do código da aplicação.
- Soft delete (`active` em cliente e veículo) preserva FKs das OS antigas.
- Testes de integração e o `docker-compose` usam o mesmo motor — não há “funciona no H2, quebra no Postgres”.
- Um único datastore simplifica o MVP: sem réplica, cache ou segundo banco.
- Operação e backup ficam no modelo relacional clássico; não há consulta documental nativa nem schema flexível por documento.

## Alternativas consideradas

- **Document store (MongoDB ou similar).** Rejeitado: relações, FKs e o orçamento transacional teriam de viver só na aplicação.
- **SQLite ou H2 em produção.** Rejeitado: índice parcial, `TIMESTAMPTZ` e paridade com Testcontainers importam; o MVP não justifica um motor “leve” diferente do de integração.
- **Outro relacional (MySQL, SQL Server).** Viável em tese, mas o Postgres 16 no Compose, as migrations já escritas para esse dialeto e o suporte a índice parcial + `TIMESTAMPTZ` tornam a troca um custo sem ganho no MVP.
