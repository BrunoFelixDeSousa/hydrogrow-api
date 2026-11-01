# Diagramas do modelo de dados

Estes diagramas apresentam visões simplificadas do modelo definido em
[`schema.db.sql`](./schema.db.sql). Campos auxiliares, índices e constraints foram omitidos para
manter a leitura clara. O arquivo SQL permanece como fonte de verdade, enquanto
[`schema.dbml`](./schema.dbml) contém a representação detalhada para o dbdiagram.io.

## Visão geral dos domínios

```mermaid
flowchart LR
    identity[Identidade] --> tenancy[Tenancy e acesso]
    tenancy --> catalog[Catálogo]
    tenancy --> commercial[Compras e vendas]
    catalog --> commercial
    catalog --> production[Produção]
    production --> inventory[Estoque]
    commercial --> inventory
    commercial --> finance[Financeiro]
    production --> readings[Leituras técnicas]
```

## Identidade, tenancy e autenticação

```mermaid
erDiagram
    USERS {
        uuid id PK
        string email UK
        string password_hash
        string status
        string email_verification_status
        timestamp last_login_at
    }

    PROFILES {
        uuid id PK
        uuid user_id FK, UK
        string full_name
        string avatar_url
    }

    PLANS {
        string id PK
        string label
        decimal price
        int product_limit
        int user_limit
        boolean is_active
    }

    TENANTS {
        uuid id PK
        string plan FK
        string name
        string document
        boolean is_active
        boolean subscription_active
    }

    TENANT_MEMBERSHIPS {
        uuid id PK
        uuid tenant_id FK
        uuid user_id FK
        boolean is_active
    }

    MEMBERSHIP_ROLES {
        uuid membership_id PK, FK
        string role PK
    }

    SUPER_ADMINS {
        uuid id PK
        uuid user_id FK, UK
    }

    AUTH_SESSIONS {
        uuid id PK
        uuid user_id FK
        uuid tenant_id FK
        string scope
        timestamp expires_at
        timestamp revoked_at
    }

    REFRESH_TOKENS {
        uuid id PK
        uuid session_id FK
        uuid parent_token_id FK
        string token_hash UK
        timestamp expires_at
        timestamp consumed_at
        timestamp revoked_at
    }

    PASSWORD_RESET_TOKENS {
        uuid id PK
        uuid user_id FK
        string token_hash UK
        timestamp expires_at
        timestamp consumed_at
        timestamp revoked_at
    }

    USERS ||--o| PROFILES : possui
    PLANS ||--o{ TENANTS : contratado_por
    USERS ||--o{ TENANT_MEMBERSHIPS : participa
    TENANTS ||--o{ TENANT_MEMBERSHIPS : agrega
    TENANT_MEMBERSHIPS ||--o{ MEMBERSHIP_ROLES : recebe
    USERS ||--o| SUPER_ADMINS : pode_ser
    USERS ||--o{ AUTH_SESSIONS : abre
    TENANTS o|--o{ AUTH_SESSIONS : contextualiza
    TENANT_MEMBERSHIPS o|--o{ AUTH_SESSIONS : autoriza
    AUTH_SESSIONS ||--o{ REFRESH_TOKENS : emite
    REFRESH_TOKENS o|--o| REFRESH_TOKENS : rotaciona
    USERS ||--o{ PASSWORD_RESET_TOKENS : solicita
```

Uma sessão de tenant referencia simultaneamente o usuário, o tenant e o respectivo vínculo. Uma
sessão global não possui tenant. Os refresh tokens formam uma cadeia de rotação por sessão.

## Catálogo, compras, vendas e financeiro

```mermaid
erDiagram
    TENANTS {
        uuid id PK
        string name
    }

    TENANT_MEMBERSHIPS {
        uuid id PK
        uuid tenant_id FK
        uuid user_id FK
    }

    PRODUCT_CATEGORIES {
        uuid id PK
        uuid tenant_id FK
        string name
    }

    PRODUCTS {
        uuid id PK
        uuid tenant_id FK
        uuid category_id FK
        string name
        string sku
        string unit
        decimal cost_price
        decimal sale_price
    }

    CUSTOMERS {
        uuid id PK
        uuid tenant_id FK
        string name
        string document
    }

    SUPPLIERS {
        uuid id PK
        uuid tenant_id FK
        string name
        string document
    }

    SALES {
        uuid id PK
        uuid tenant_id FK
        uuid customer_id FK
        uuid user_id FK
        decimal total
        decimal discount
        string status
    }

    SALE_ITEMS {
        uuid id PK
        uuid tenant_id FK
        uuid sale_id FK
        uuid product_id FK
        decimal quantity
        decimal unit_price
        decimal total
    }

    PURCHASES {
        uuid id PK
        uuid tenant_id FK
        uuid supplier_id FK
        uuid user_id FK
        decimal total
        string status
    }

    PURCHASE_ITEMS {
        uuid id PK
        uuid tenant_id FK
        uuid purchase_id FK
        uuid product_id FK
        decimal quantity
        decimal unit_price
        decimal total
    }

    FINANCIAL_TRANSACTIONS {
        uuid id PK
        uuid tenant_id FK
        uuid sale_id FK
        uuid purchase_id FK
        string type
        decimal amount
        date due_date
        string status
    }

    TENANTS ||--o{ PRODUCT_CATEGORIES : possui
    TENANTS ||--o{ PRODUCTS : possui
    PRODUCT_CATEGORIES o|--o{ PRODUCTS : classifica
    TENANTS ||--o{ CUSTOMERS : atende
    TENANTS ||--o{ SUPPLIERS : contrata
    TENANTS ||--o{ SALES : registra
    CUSTOMERS o|--o{ SALES : realiza
    TENANT_MEMBERSHIPS ||--o{ SALES : opera
    SALES ||--o{ SALE_ITEMS : contem
    PRODUCTS ||--o{ SALE_ITEMS : vendido_como
    TENANTS ||--o{ PURCHASES : registra
    SUPPLIERS o|--o{ PURCHASES : fornece
    TENANT_MEMBERSHIPS ||--o{ PURCHASES : opera
    PURCHASES ||--o{ PURCHASE_ITEMS : contem
    PRODUCTS ||--o{ PURCHASE_ITEMS : comprado_como
    TENANTS ||--o{ FINANCIAL_TRANSACTIONS : contabiliza
    SALES o|--o| FINANCIAL_TRANSACTIONS : gera_recebivel
    PURCHASES o|--o| FINANCIAL_TRANSACTIONS : gera_pagavel
```

Os itens preservam nome, unidade e preço como snapshots comerciais. Uma venda ou compra pode
gerar no máximo uma transação financeira no escopo atual.

## Produção, estoque e leituras técnicas

```mermaid
erDiagram
    TENANTS {
        uuid id PK
        string name
    }

    TENANT_MEMBERSHIPS {
        uuid id PK
        uuid tenant_id FK
        uuid user_id FK
    }

    PRODUCTS {
        uuid id PK
        uuid tenant_id FK
        string name
        string unit
    }

    CROPS {
        uuid id PK
        uuid tenant_id FK
        uuid product_id FK
        string name
        string variety
        int cycle_days
        decimal estimated_yield
    }

    PRODUCTION_BATCHES {
        uuid id PK
        uuid tenant_id FK
        uuid crop_id FK
        uuid product_id FK
        string batch_code
        date start_date
        date expected_harvest
        date actual_harvest
        string status
    }

    INVENTORY_LOTS {
        uuid id PK
        uuid tenant_id FK
        uuid product_id FK
        uuid production_batch_id FK
        string lot_code
        decimal current_stock
        date expiry_date
    }

    SALE_ITEMS {
        uuid id PK
        uuid tenant_id FK
        uuid product_id FK
    }

    PURCHASE_ITEMS {
        uuid id PK
        uuid tenant_id FK
        uuid product_id FK
    }

    STOCK_MOVEMENTS {
        uuid id PK
        uuid tenant_id FK
        uuid product_id FK
        uuid lot_id FK
        uuid sale_item_id FK
        uuid purchase_item_id FK
        uuid production_batch_id FK
        uuid reversal_of_id FK
        uuid user_id FK
        string type
        decimal quantity_delta
        string operation_key UK
    }

    TECHNICAL_READINGS {
        uuid id PK
        uuid tenant_id FK
        uuid batch_id FK
        uuid recorded_by FK
        decimal ph
        decimal ec
        decimal temperature
        timestamp recorded_at
    }

    TENANTS ||--o{ CROPS : cultiva
    PRODUCTS ||--o{ CROPS : representa_colheita
    CROPS ||--o{ PRODUCTION_BATCHES : origina
    PRODUCTS ||--o{ PRODUCTION_BATCHES : produz
    PRODUCTION_BATCHES o|--o| INVENTORY_LOTS : gera
    PRODUCTS ||--o{ INVENTORY_LOTS : estocado_em
    INVENTORY_LOTS ||--o{ STOCK_MOVEMENTS : movimenta
    PRODUCTS ||--o{ STOCK_MOVEMENTS : referencia
    SALE_ITEMS o|--o{ STOCK_MOVEMENTS : causa_saida
    PURCHASE_ITEMS o|--o{ STOCK_MOVEMENTS : causa_entrada
    PRODUCTION_BATCHES o|--o{ STOCK_MOVEMENTS : causa_entrada
    STOCK_MOVEMENTS o|--o| STOCK_MOVEMENTS : estorna
    TENANT_MEMBERSHIPS ||--o{ STOCK_MOVEMENTS : registra
    PRODUCTION_BATCHES ||--o{ TECHNICAL_READINGS : recebe
    TENANT_MEMBERSHIPS ||--o{ TECHNICAL_READINGS : coleta
```

Cada movimento pertence ao mesmo tenant, produto e lote das entidades que lhe deram origem. O
saldo materializado do lote deve ser atualizado na mesma transação em que o movimento imutável é
gravado.
