-- HydroGrow: arquitetura
-- DBML e SQL espelham o modelo; índices parciais e triggers são complementos PostgreSQL.
-- Access JWT: SmallRye, RS256, 15 min, sid -> auth_sessions; sem persistir JWT bruto.
-- Refresh: opaco, SHA-256 em refresh_tokens; replay revoga a sessão, inclusive acesso via sid.
BEGIN;

-- Identidade global e credenciais. status preserva o ciclo de vida completo.
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    email TEXT NOT NULL,
    password_hash TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'PENDING',
    email_verified BOOLEAN NOT NULL DEFAULT false,
    last_login_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT ck_users_email_normalized CHECK (email = lower(btrim(email)) AND length(email) BETWEEN 3 AND 255),
    CONSTRAINT ck_users_status CHECK (status IN ('PENDING', 'ACTIVE', 'BLOCKED', 'DISABLED'))
);

-- Um perfil global por usuário. O tenant apresentado no DTO vem da sessão autorizada, não desta tabela.
CREATE TABLE profiles (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    user_id UUID NOT NULL,
    full_name TEXT NOT NULL,
    avatar_url TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_profiles_user_id UNIQUE (user_id),
    CONSTRAINT ck_profiles_full_name CHECK (length(btrim(full_name)) BETWEEN 2 AND 100)
);

-- Catálogo comercial. Valores monetários em BRL; limite NULL significa ilimitado.
CREATE TABLE plans (
    id TEXT PRIMARY KEY,
    label TEXT NOT NULL,
    price NUMERIC(18,2) NOT NULL DEFAULT 0,
    product_limit INTEGER,
    user_limit INTEGER,
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_plans_price CHECK (price >= 0),
    CONSTRAINT ck_plans_limits CHECK ((product_limit IS NULL OR product_limit > 0) AND (user_limit IS NULL OR user_limit > 0))
);

-- Empresa. plan e subscription_active são a projeção de acesso comercial mantida pelo caso de uso de billing.
CREATE TABLE tenants (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    name TEXT NOT NULL,
    document TEXT,
    email TEXT,
    phone TEXT,
    address TEXT,
    city TEXT,
    state TEXT,
    zip_code TEXT,
    plan TEXT NOT NULL DEFAULT 'basico',
    is_active BOOLEAN NOT NULL DEFAULT true,
    subscription_active BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_tenants_name CHECK (length(btrim(name)) BETWEEN 2 AND 100),
    CONSTRAINT ck_tenants_state CHECK (state IS NULL OR state ~ '^[A-Z]{2}$')
);

-- Vínculo explícito usuário/empresa. Desativar em vez de excluir para preservar autoria e sessões históricas.
CREATE TABLE tenant_memberships (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    tenant_id UUID NOT NULL,
    user_id UUID NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_tenant_memberships_tenant_id_user_id UNIQUE (tenant_id, user_id),
    CONSTRAINT uq_tenant_memberships_tenant_id_id UNIQUE (tenant_id, id)
);

-- Papéis do vínculo. Super admin é global e nunca pode ser atribuído aqui.
CREATE TABLE membership_roles (
    membership_id UUID NOT NULL,
    role TEXT NOT NULL,
    CONSTRAINT pk_membership_roles_membership_id_role PRIMARY KEY (membership_id, role),
    CONSTRAINT ck_membership_roles_role CHECK (role IN ('admin', 'operator', 'financial'))
);

-- Privilégio administrativo global; não elimina os filtros de tenant nas rotas operacionais.
CREATE TABLE super_admins (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    user_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_super_admins_user_id UNIQUE (user_id)
);

-- Uma sessão por login/contexto/dispositivo. sid do JWT referencia id. Revogação é consultada em toda requisição. Expiração absoluta: 30 dias.
CREATE TABLE auth_sessions (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    user_id UUID NOT NULL,
    tenant_id UUID,
    scope TEXT NOT NULL DEFAULT 'tenant',
    device_label TEXT,
    user_agent TEXT,
    created_ip INET,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    last_seen_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ,
    revoke_reason TEXT,
    CONSTRAINT ck_auth_sessions_scope CHECK ((scope = 'tenant' AND tenant_id IS NOT NULL) OR (scope = 'global' AND tenant_id IS NULL)),
    CONSTRAINT ck_auth_sessions_expires CHECK (expires_at > created_at),
    CONSTRAINT ck_auth_sessions_revocation CHECK ((revoked_at IS NULL AND revoke_reason IS NULL) OR (revoked_at IS NOT NULL AND revoke_reason IS NOT NULL))
);

-- Uma linha por emissão; SHA-256 hexadecimal de token opaco com 32 bytes aleatórios. Histórico preserva a família (session_id). SQL inclui índices parciais para um token aberto e uma raiz por sessão.
CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    session_id UUID NOT NULL,
    token_hash TEXT NOT NULL,
    parent_token_id UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_at TIMESTAMPTZ NOT NULL,
    consumed_at TIMESTAMPTZ,
    revoked_at TIMESTAMPTZ,
    CONSTRAINT uq_refresh_tokens_token_hash UNIQUE (token_hash),
    CONSTRAINT uq_refresh_tokens_session_id_id UNIQUE (session_id, id),
    CONSTRAINT uq_refresh_tokens_parent_token_id UNIQUE (parent_token_id),
    CONSTRAINT ck_refresh_tokens_hash CHECK (token_hash ~ '^[0-9a-f]{64}$'),
    CONSTRAINT ck_refresh_tokens_expiry CHECK (expires_at > created_at),
    CONSTRAINT ck_refresh_tokens_parent CHECK (parent_token_id IS NULL OR parent_token_id <> id),
    CONSTRAINT ck_refresh_tokens_consumed CHECK (consumed_at IS NULL OR consumed_at >= created_at)
);

-- Token opaco de uso único, SHA-256, validade de uma hora. SQL limita a um token aberto por usuário; novo pedido revoga os anteriores.
CREATE TABLE password_reset_tokens (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    user_id UUID NOT NULL,
    token_hash TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_at TIMESTAMPTZ NOT NULL,
    consumed_at TIMESTAMPTZ,
    revoked_at TIMESTAMPTZ,
    CONSTRAINT uq_password_reset_tokens_token_hash UNIQUE (token_hash),
    CONSTRAINT ck_password_reset_tokens_hash CHECK (token_hash ~ '^[0-9a-f]{64}$'),
    CONSTRAINT ck_password_reset_tokens_expiry CHECK (expires_at > created_at),
    CONSTRAINT ck_password_reset_tokens_consumed CHECK (consumed_at IS NULL OR consumed_at >= created_at)
);

-- Categorias próprias de cada empresa.
CREATE TABLE product_categories (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    tenant_id UUID NOT NULL,
    name TEXT NOT NULL,
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_product_categories_tenant_id_id UNIQUE (tenant_id, id),
    CONSTRAINT uq_product_categories_tenant_id_name UNIQUE (tenant_id, name),
    CONSTRAINT ck_product_categories_name CHECK (length(btrim(name)) > 0)
);

-- Catálogo. Lote, validade e saldo pertencem a inventory_lots; currentStock no DTO é SUM(current_stock) dos lotes. Unidade não muda após movimentação.
CREATE TABLE products (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    tenant_id UUID NOT NULL,
    category_id UUID,
    name TEXT NOT NULL,
    sku TEXT,
    unit TEXT NOT NULL DEFAULT 'un',
    cost_price NUMERIC(18,4) NOT NULL DEFAULT 0,
    sale_price NUMERIC(18,4) NOT NULL DEFAULT 0,
    min_stock NUMERIC(18,3) NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_products_tenant_id_id UNIQUE (tenant_id, id),
    CONSTRAINT uq_products_tenant_id_sku UNIQUE (tenant_id, sku),
    CONSTRAINT ck_products_name CHECK (length(btrim(name)) > 0),
    CONSTRAINT ck_products_prices CHECK (cost_price >= 0 AND sale_price >= 0 AND min_stock >= 0),
    CONSTRAINT ck_products_sku CHECK (sku IS NULL OR (sku = upper(btrim(sku)) AND length(sku) > 0)),
    CONSTRAINT ck_products_unit CHECK (unit IN ('un', 'kg', 'g', 'L', 'mL', 'cx'))
);

-- Parceiro por tenant. Documento opcional normalizado sem pontuação; vazio vira NULL. FKs impedem excluir parceiros utilizados.
CREATE TABLE customers (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    tenant_id UUID NOT NULL,
    name TEXT NOT NULL,
    document TEXT,
    email TEXT,
    phone TEXT,
    address TEXT,
    city TEXT,
    state TEXT,
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_customers_tenant_id_id UNIQUE (tenant_id, id),
    CONSTRAINT uq_customers_tenant_id_document UNIQUE (tenant_id, document),
    CONSTRAINT ck_customers_name CHECK (length(btrim(name)) > 0),
    CONSTRAINT ck_customers_document CHECK (document IS NULL OR document ~ '^([0-9]{11}|[0-9]{14})$'),
    CONSTRAINT ck_customers_state CHECK (state IS NULL OR state ~ '^[A-Z]{2}$')
);

-- Parceiro por tenant. Documento opcional normalizado sem pontuação; vazio vira NULL. FKs impedem excluir parceiros utilizados.
CREATE TABLE suppliers (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    tenant_id UUID NOT NULL,
    name TEXT NOT NULL,
    document TEXT,
    email TEXT,
    phone TEXT,
    address TEXT,
    city TEXT,
    state TEXT,
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_suppliers_tenant_id_id UNIQUE (tenant_id, id),
    CONSTRAINT uq_suppliers_tenant_id_document UNIQUE (tenant_id, document),
    CONSTRAINT ck_suppliers_name CHECK (length(btrim(name)) > 0),
    CONSTRAINT ck_suppliers_document CHECK (document IS NULL OR document ~ '^([0-9]{11}|[0-9]{14})$'),
    CONSTRAINT ck_suppliers_state CHECK (state IS NULL OR state ~ '^[A-Z]{2}$')
);

-- Venda: open, completed, cancelled. Total recalculado no servidor. Conclusão, estoque e financeiro na mesma transação.
CREATE TABLE sales (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    tenant_id UUID NOT NULL,
    customer_id UUID,
    user_id UUID NOT NULL,
    total NUMERIC(18,2) NOT NULL DEFAULT 0,
    discount NUMERIC(18,2) NOT NULL DEFAULT 0,
    payment_method TEXT,
    status TEXT NOT NULL DEFAULT 'open',
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_sales_tenant_id_id UNIQUE (tenant_id, id),
    CONSTRAINT ck_sales_total CHECK (total >= 0),
    CONSTRAINT ck_sales_discount CHECK (discount >= 0),
    CONSTRAINT ck_sales_status CHECK (status IN ('open', 'completed', 'cancelled')),
    CONSTRAINT ck_sales_final_total CHECK (status <> 'completed' OR total > 0)
);

-- Preço, unidade e nome são snapshots comerciais. Um produto por documento; alocações de vários lotes são representadas nos movimentos.
CREATE TABLE sale_items (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    tenant_id UUID NOT NULL,
    sale_id UUID NOT NULL,
    product_id UUID NOT NULL,
    product_name TEXT NOT NULL,
    unit TEXT NOT NULL,
    quantity NUMERIC(18,3) NOT NULL,
    unit_price NUMERIC(18,4) NOT NULL,
    total NUMERIC(18,2) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_sale_items_tenant_id_id UNIQUE (tenant_id, id),
    CONSTRAINT uq_sale_items_tenant_id_id_product_id UNIQUE (tenant_id, id, product_id),
    CONSTRAINT uq_sale_items_tenant_id_sale_id_product_id UNIQUE (tenant_id, sale_id, product_id),
    CONSTRAINT ck_sale_items_quantity CHECK (quantity > 0),
    CONSTRAINT ck_sale_items_price CHECK (unit_price >= 0),
    CONSTRAINT ck_sale_items_total CHECK (total = round(quantity * unit_price, 2))
);

-- Compra: open, received, cancelled. Recebimento, estoque e financeiro na mesma transação.
CREATE TABLE purchases (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    tenant_id UUID NOT NULL,
    supplier_id UUID,
    user_id UUID NOT NULL,
    invoice_number TEXT,
    total NUMERIC(18,2) NOT NULL DEFAULT 0,
    status TEXT NOT NULL DEFAULT 'open',
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_purchases_tenant_id_id UNIQUE (tenant_id, id),
    CONSTRAINT ck_purchases_total CHECK (total >= 0),
    CONSTRAINT ck_purchases_status CHECK (status IN ('open', 'received', 'cancelled')),
    CONSTRAINT ck_purchases_final_total CHECK (status <> 'received' OR total > 0)
);

-- Preço, unidade e nome são snapshots comerciais. Um produto por documento; alocações de vários lotes são representadas nos movimentos.
CREATE TABLE purchase_items (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    tenant_id UUID NOT NULL,
    purchase_id UUID NOT NULL,
    product_id UUID NOT NULL,
    product_name TEXT NOT NULL,
    unit TEXT NOT NULL,
    quantity NUMERIC(18,3) NOT NULL,
    unit_price NUMERIC(18,4) NOT NULL,
    total NUMERIC(18,2) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_purchase_items_tenant_id_id UNIQUE (tenant_id, id),
    CONSTRAINT uq_purchase_items_tenant_id_id_product_id UNIQUE (tenant_id, id, product_id),
    CONSTRAINT uq_purchase_items_tenant_id_purchase_id_product_id UNIQUE (tenant_id, purchase_id, product_id),
    CONSTRAINT ck_purchase_items_quantity CHECK (quantity > 0),
    CONSTRAINT ck_purchase_items_price CHECK (unit_price >= 0),
    CONSTRAINT ck_purchase_items_total CHECK (total = round(quantity * unit_price, 2))
);

-- Modelo agronômico. O produto e a unidade são copiados para o lote de produção no plantio.
CREATE TABLE crops (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    tenant_id UUID NOT NULL,
    name TEXT NOT NULL,
    variety TEXT,
    cycle_days INTEGER,
    estimated_yield NUMERIC(18,3),
    yield_unit TEXT NOT NULL DEFAULT 'kg',
    notes TEXT,
    product_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_crops_tenant_id_id UNIQUE (tenant_id, id),
    CONSTRAINT ck_crops_name CHECK (length(btrim(name)) > 0),
    CONSTRAINT ck_crops_cycle CHECK (cycle_days IS NULL OR cycle_days > 0),
    CONSTRAINT ck_crops_yield CHECK (estimated_yield IS NULL OR estimated_yield > 0)
);

-- Ciclo de plantio. Produto e unidade de saída são snapshots. Uma colheita final no escopo atual; perdas não são status separado. Custos gerenciais não geram despesa duplicada de compras.
CREATE TABLE production_batches (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    tenant_id UUID NOT NULL,
    crop_id UUID NOT NULL,
    product_id UUID NOT NULL,
    yield_unit TEXT NOT NULL,
    batch_code TEXT NOT NULL,
    start_date DATE NOT NULL,
    expected_harvest DATE,
    actual_harvest DATE,
    quantity_planted NUMERIC(18,3) NOT NULL,
    quantity_harvested NUMERIC(18,3),
    germination_loss NUMERIC(5,4) NOT NULL DEFAULT 0,
    loss_quantity NUMERIC(18,3) NOT NULL DEFAULT 0,
    cost_seedlings NUMERIC(18,2) NOT NULL DEFAULT 0,
    cost_inputs NUMERIC(18,2) NOT NULL DEFAULT 0,
    cost_labor NUMERIC(18,2) NOT NULL DEFAULT 0,
    cost_other NUMERIC(18,2) NOT NULL DEFAULT 0,
    status TEXT NOT NULL DEFAULT 'growing',
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_production_batches_tenant_id_id UNIQUE (tenant_id, id),
    CONSTRAINT uq_production_batches_tenant_id_id_product_id UNIQUE (tenant_id, id, product_id),
    CONSTRAINT uq_production_batches_tenant_id_batch_code UNIQUE (tenant_id, batch_code),
    CONSTRAINT ck_production_batches_code CHECK (length(btrim(batch_code)) > 0),
    CONSTRAINT ck_production_batches_dates CHECK ((expected_harvest IS NULL OR expected_harvest >= start_date) AND (actual_harvest IS NULL OR actual_harvest >= start_date)),
    CONSTRAINT ck_production_batches_quantities CHECK (quantity_planted > 0 AND (quantity_harvested IS NULL OR quantity_harvested > 0) AND loss_quantity BETWEEN 0 AND quantity_planted),
    CONSTRAINT ck_production_batches_germination CHECK (germination_loss BETWEEN 0 AND 1),
    CONSTRAINT ck_production_batches_costs CHECK (cost_seedlings >= 0 AND cost_inputs >= 0 AND cost_labor >= 0 AND cost_other >= 0),
    CONSTRAINT ck_production_batches_status CHECK (status IN ('growing', 'harvested', 'cancelled')),
    CONSTRAINT ck_production_batches_harvest CHECK ((status = 'harvested' AND actual_harvest IS NOT NULL AND quantity_harvested IS NOT NULL) OR (status <> 'harvested' AND actual_harvest IS NULL AND quantity_harvested IS NULL))
);

-- Lote físico de estoque por produto, inclusive um lote interno para itens sem lote comercial. Saldo materializado inicia zero e é atualizado junto com o movimento. Validade opcional.
CREATE TABLE inventory_lots (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    tenant_id UUID NOT NULL,
    product_id UUID NOT NULL,
    lot_code TEXT NOT NULL,
    received_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    expiry_date DATE,
    production_batch_id UUID,
    current_stock NUMERIC(18,3) NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_inventory_lots_tenant_id_id UNIQUE (tenant_id, id),
    CONSTRAINT uq_inventory_lots_tenant_id_id_product_id UNIQUE (tenant_id, id, product_id),
    CONSTRAINT uq_inventory_lots_tenant_id_product_id_lot_code UNIQUE (tenant_id, product_id, lot_code),
    CONSTRAINT uq_inventory_lots_tenant_id_production_batch_id UNIQUE (tenant_id, production_batch_id),
    CONSTRAINT ck_inventory_lots_code CHECK (length(btrim(lot_code)) > 0),
    CONSTRAINT ck_inventory_lots_stock CHECK (current_stock >= 0)
);

-- Razão imutável de estoque. Delta assinado; estorno por novo registro ligado ao original. Origem tipada com FK. operation_key estável por comando/item/lote garante idempotência.
CREATE TABLE stock_movements (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    tenant_id UUID NOT NULL,
    product_id UUID NOT NULL,
    lot_id UUID NOT NULL,
    type TEXT NOT NULL,
    quantity_delta NUMERIC(18,3) NOT NULL,
    operation_key TEXT NOT NULL,
    sale_item_id UUID,
    purchase_item_id UUID,
    production_batch_id UUID,
    reversal_of_id UUID,
    notes TEXT,
    user_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_stock_movements_tenant_id_id_lot_id_product_id UNIQUE (tenant_id, id, lot_id, product_id),
    CONSTRAINT uq_stock_movements_tenant_id_operation_key UNIQUE (tenant_id, operation_key),
    CONSTRAINT uq_stock_movements_reversal_of_id UNIQUE (reversal_of_id),
    CONSTRAINT ck_stock_movements_delta CHECK (quantity_delta <> 0),
    CONSTRAINT ck_stock_movements_type CHECK ((type = 'entry' AND quantity_delta > 0) OR (type = 'exit' AND quantity_delta < 0) OR type IN ('adjustment', 'reversal')),
    CONSTRAINT ck_stock_movements_origin CHECK (num_nonnulls(sale_item_id, purchase_item_id, production_batch_id, reversal_of_id) <= 1),
    CONSTRAINT ck_stock_movements_reversal CHECK ((type = 'reversal') = (reversal_of_id IS NOT NULL)),
    CONSTRAINT ck_stock_movements_origin_type CHECK ((sale_item_id IS NULL OR type = 'exit') AND (purchase_item_id IS NULL OR type = 'entry') AND (production_batch_id IS NULL OR type = 'entry')),
    CONSTRAINT ck_stock_movements_manual_note CHECK (num_nonnulls(sale_item_id, purchase_item_id, production_batch_id, reversal_of_id) > 0 OR (notes IS NOT NULL AND length(btrim(notes)) > 0)),
    CONSTRAINT ck_stock_movements_operation_key CHECK (length(btrim(operation_key)) > 0),
    CONSTRAINT ck_stock_movements_self_reversal CHECK (reversal_of_id IS NULL OR reversal_of_id <> id)
);

-- Contas a receber/pagar, não contabilidade de partidas dobradas. Uma conta por venda/compra no MVP. overdue é derivado de pending e due_date; pagamento integral.
CREATE TABLE financial_transactions (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    tenant_id UUID NOT NULL,
    type TEXT NOT NULL,
    description TEXT NOT NULL,
    amount NUMERIC(18,2) NOT NULL,
    due_date DATE NOT NULL,
    paid_date DATE,
    status TEXT NOT NULL DEFAULT 'pending',
    sale_id UUID,
    purchase_id UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_financial_transactions_tenant_id_id UNIQUE (tenant_id, id),
    CONSTRAINT uq_financial_transactions_tenant_id_sale_id UNIQUE (tenant_id, sale_id),
    CONSTRAINT uq_financial_transactions_tenant_id_purchase_id UNIQUE (tenant_id, purchase_id),
    CONSTRAINT ck_financial_transactions_type CHECK (type IN ('receivable', 'payable')),
    CONSTRAINT ck_financial_transactions_description CHECK (length(btrim(description)) > 0),
    CONSTRAINT ck_financial_transactions_amount CHECK (amount > 0),
    CONSTRAINT ck_financial_transactions_status CHECK (status IN ('pending', 'paid', 'cancelled')),
    CONSTRAINT ck_financial_transactions_paid CHECK ((status = 'paid') = (paid_date IS NOT NULL)),
    CONSTRAINT ck_financial_transactions_origin CHECK (num_nonnulls(sale_id, purchase_id) <= 1),
    CONSTRAINT ck_financial_transactions_origin_type CHECK ((sale_id IS NULL OR type = 'receivable') AND (purchase_id IS NULL OR type = 'payable'))
);

-- Leituras vinculadas ao lote e tenant. Limites físicos aceitos pelo domínio, não faixa agronômica ideal.
CREATE TABLE technical_readings (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    tenant_id UUID NOT NULL,
    batch_id UUID NOT NULL,
    ph NUMERIC(4,2),
    ec NUMERIC(8,3),
    temperature NUMERIC(6,2),
    recorded_by UUID NOT NULL,
    recorded_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    notes TEXT,
    CONSTRAINT ck_technical_readings_measurement CHECK (num_nonnulls(ph, ec, temperature) >= 1),
    CONSTRAINT ck_technical_readings_ph CHECK (ph IS NULL OR ph BETWEEN 0 AND 14),
    CONSTRAINT ck_technical_readings_ec CHECK (ec IS NULL OR ec >= 0),
    CONSTRAINT ck_technical_readings_temperature CHECK (temperature IS NULL OR temperature BETWEEN -50 AND 80)
);

ALTER TABLE profiles ADD CONSTRAINT fk_profiles_1 FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE RESTRICT;
ALTER TABLE tenants ADD CONSTRAINT fk_tenants_1 FOREIGN KEY (plan) REFERENCES plans (id) ON DELETE RESTRICT;
ALTER TABLE tenant_memberships ADD CONSTRAINT fk_tenant_memberships_1 FOREIGN KEY (tenant_id) REFERENCES tenants (id) ON DELETE RESTRICT;
ALTER TABLE tenant_memberships ADD CONSTRAINT fk_tenant_memberships_2 FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE RESTRICT;
ALTER TABLE membership_roles ADD CONSTRAINT fk_membership_roles_1 FOREIGN KEY (membership_id) REFERENCES tenant_memberships (id) ON DELETE RESTRICT;
ALTER TABLE super_admins ADD CONSTRAINT fk_super_admins_1 FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE RESTRICT;
ALTER TABLE auth_sessions ADD CONSTRAINT fk_auth_sessions_1 FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE RESTRICT;
ALTER TABLE auth_sessions ADD CONSTRAINT fk_auth_sessions_2 FOREIGN KEY (tenant_id) REFERENCES tenants (id) ON DELETE RESTRICT;
ALTER TABLE auth_sessions ADD CONSTRAINT fk_auth_sessions_3 FOREIGN KEY (tenant_id, user_id) REFERENCES tenant_memberships (tenant_id, user_id) ON DELETE RESTRICT;
ALTER TABLE refresh_tokens ADD CONSTRAINT fk_refresh_tokens_1 FOREIGN KEY (session_id) REFERENCES auth_sessions (id) ON DELETE RESTRICT;
ALTER TABLE refresh_tokens ADD CONSTRAINT fk_refresh_tokens_2 FOREIGN KEY (session_id, parent_token_id) REFERENCES refresh_tokens (session_id, id) ON DELETE RESTRICT;
ALTER TABLE password_reset_tokens ADD CONSTRAINT fk_password_reset_tokens_1 FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE RESTRICT;
ALTER TABLE product_categories ADD CONSTRAINT fk_product_categories_1 FOREIGN KEY (tenant_id) REFERENCES tenants (id) ON DELETE RESTRICT;
ALTER TABLE products ADD CONSTRAINT fk_products_1 FOREIGN KEY (tenant_id) REFERENCES tenants (id) ON DELETE RESTRICT;
ALTER TABLE products ADD CONSTRAINT fk_products_2 FOREIGN KEY (tenant_id, category_id) REFERENCES product_categories (tenant_id, id) ON DELETE RESTRICT;
ALTER TABLE customers ADD CONSTRAINT fk_customers_1 FOREIGN KEY (tenant_id) REFERENCES tenants (id) ON DELETE RESTRICT;
ALTER TABLE suppliers ADD CONSTRAINT fk_suppliers_1 FOREIGN KEY (tenant_id) REFERENCES tenants (id) ON DELETE RESTRICT;
ALTER TABLE sales ADD CONSTRAINT fk_sales_1 FOREIGN KEY (tenant_id) REFERENCES tenants (id) ON DELETE RESTRICT;
ALTER TABLE sales ADD CONSTRAINT fk_sales_2 FOREIGN KEY (tenant_id, customer_id) REFERENCES customers (tenant_id, id) ON DELETE RESTRICT;
ALTER TABLE sales ADD CONSTRAINT fk_sales_3 FOREIGN KEY (tenant_id, user_id) REFERENCES tenant_memberships (tenant_id, user_id) ON DELETE RESTRICT;
ALTER TABLE sale_items ADD CONSTRAINT fk_sale_items_1 FOREIGN KEY (tenant_id) REFERENCES tenants (id) ON DELETE RESTRICT;
ALTER TABLE sale_items ADD CONSTRAINT fk_sale_items_2 FOREIGN KEY (tenant_id, sale_id) REFERENCES sales (tenant_id, id) ON DELETE RESTRICT;
ALTER TABLE sale_items ADD CONSTRAINT fk_sale_items_3 FOREIGN KEY (tenant_id, product_id) REFERENCES products (tenant_id, id) ON DELETE RESTRICT;
ALTER TABLE purchases ADD CONSTRAINT fk_purchases_1 FOREIGN KEY (tenant_id) REFERENCES tenants (id) ON DELETE RESTRICT;
ALTER TABLE purchases ADD CONSTRAINT fk_purchases_2 FOREIGN KEY (tenant_id, supplier_id) REFERENCES suppliers (tenant_id, id) ON DELETE RESTRICT;
ALTER TABLE purchases ADD CONSTRAINT fk_purchases_3 FOREIGN KEY (tenant_id, user_id) REFERENCES tenant_memberships (tenant_id, user_id) ON DELETE RESTRICT;
ALTER TABLE purchase_items ADD CONSTRAINT fk_purchase_items_1 FOREIGN KEY (tenant_id) REFERENCES tenants (id) ON DELETE RESTRICT;
ALTER TABLE purchase_items ADD CONSTRAINT fk_purchase_items_2 FOREIGN KEY (tenant_id, purchase_id) REFERENCES purchases (tenant_id, id) ON DELETE RESTRICT;
ALTER TABLE purchase_items ADD CONSTRAINT fk_purchase_items_3 FOREIGN KEY (tenant_id, product_id) REFERENCES products (tenant_id, id) ON DELETE RESTRICT;
ALTER TABLE crops ADD CONSTRAINT fk_crops_1 FOREIGN KEY (tenant_id) REFERENCES tenants (id) ON DELETE RESTRICT;
ALTER TABLE crops ADD CONSTRAINT fk_crops_2 FOREIGN KEY (tenant_id, product_id) REFERENCES products (tenant_id, id) ON DELETE RESTRICT;
ALTER TABLE production_batches ADD CONSTRAINT fk_production_batches_1 FOREIGN KEY (tenant_id) REFERENCES tenants (id) ON DELETE RESTRICT;
ALTER TABLE production_batches ADD CONSTRAINT fk_production_batches_2 FOREIGN KEY (tenant_id, crop_id) REFERENCES crops (tenant_id, id) ON DELETE RESTRICT;
ALTER TABLE production_batches ADD CONSTRAINT fk_production_batches_3 FOREIGN KEY (tenant_id, product_id) REFERENCES products (tenant_id, id) ON DELETE RESTRICT;
ALTER TABLE inventory_lots ADD CONSTRAINT fk_inventory_lots_1 FOREIGN KEY (tenant_id) REFERENCES tenants (id) ON DELETE RESTRICT;
ALTER TABLE inventory_lots ADD CONSTRAINT fk_inventory_lots_2 FOREIGN KEY (tenant_id, product_id) REFERENCES products (tenant_id, id) ON DELETE RESTRICT;
ALTER TABLE inventory_lots ADD CONSTRAINT fk_inventory_lots_3 FOREIGN KEY (tenant_id, production_batch_id, product_id) REFERENCES production_batches (tenant_id, id, product_id) ON DELETE RESTRICT;
ALTER TABLE stock_movements ADD CONSTRAINT fk_stock_movements_1 FOREIGN KEY (tenant_id) REFERENCES tenants (id) ON DELETE RESTRICT;
ALTER TABLE stock_movements ADD CONSTRAINT fk_stock_movements_2 FOREIGN KEY (tenant_id, lot_id, product_id) REFERENCES inventory_lots (tenant_id, id, product_id) ON DELETE RESTRICT;
ALTER TABLE stock_movements ADD CONSTRAINT fk_stock_movements_3 FOREIGN KEY (tenant_id, sale_item_id, product_id) REFERENCES sale_items (tenant_id, id, product_id) ON DELETE RESTRICT;
ALTER TABLE stock_movements ADD CONSTRAINT fk_stock_movements_4 FOREIGN KEY (tenant_id, purchase_item_id, product_id) REFERENCES purchase_items (tenant_id, id, product_id) ON DELETE RESTRICT;
ALTER TABLE stock_movements ADD CONSTRAINT fk_stock_movements_5 FOREIGN KEY (tenant_id, production_batch_id, product_id) REFERENCES production_batches (tenant_id, id, product_id) ON DELETE RESTRICT;
ALTER TABLE stock_movements ADD CONSTRAINT fk_stock_movements_6 FOREIGN KEY (tenant_id, reversal_of_id, lot_id, product_id) REFERENCES stock_movements (tenant_id, id, lot_id, product_id) ON DELETE RESTRICT;
ALTER TABLE stock_movements ADD CONSTRAINT fk_stock_movements_7 FOREIGN KEY (tenant_id, user_id) REFERENCES tenant_memberships (tenant_id, user_id) ON DELETE RESTRICT;
ALTER TABLE financial_transactions ADD CONSTRAINT fk_financial_transactions_1 FOREIGN KEY (tenant_id) REFERENCES tenants (id) ON DELETE RESTRICT;
ALTER TABLE financial_transactions ADD CONSTRAINT fk_financial_transactions_2 FOREIGN KEY (tenant_id, sale_id) REFERENCES sales (tenant_id, id) ON DELETE RESTRICT;
ALTER TABLE financial_transactions ADD CONSTRAINT fk_financial_transactions_3 FOREIGN KEY (tenant_id, purchase_id) REFERENCES purchases (tenant_id, id) ON DELETE RESTRICT;
ALTER TABLE technical_readings ADD CONSTRAINT fk_technical_readings_1 FOREIGN KEY (tenant_id) REFERENCES tenants (id) ON DELETE RESTRICT;
ALTER TABLE technical_readings ADD CONSTRAINT fk_technical_readings_2 FOREIGN KEY (tenant_id, batch_id) REFERENCES production_batches (tenant_id, id) ON DELETE RESTRICT;
ALTER TABLE technical_readings ADD CONSTRAINT fk_technical_readings_3 FOREIGN KEY (tenant_id, recorded_by) REFERENCES tenant_memberships (tenant_id, user_id) ON DELETE RESTRICT;

CREATE INDEX ix_tenants_plan ON tenants (plan);
CREATE INDEX ix_tenant_memberships_user_id_is_active ON tenant_memberships (user_id, is_active);
CREATE INDEX ix_auth_sessions_user_id_revoked_at ON auth_sessions (user_id, revoked_at);
CREATE INDEX ix_auth_sessions_tenant_id_user_id ON auth_sessions (tenant_id, user_id);
CREATE INDEX ix_auth_sessions_expires_at ON auth_sessions (expires_at);
CREATE INDEX ix_refresh_tokens_expires_at ON refresh_tokens (expires_at);
CREATE INDEX ix_password_reset_tokens_user_id ON password_reset_tokens (user_id);
CREATE INDEX ix_password_reset_tokens_expires_at ON password_reset_tokens (expires_at);
CREATE INDEX ix_products_tenant_id_category_id ON products (tenant_id, category_id);
CREATE INDEX ix_products_tenant_id_is_active ON products (tenant_id, is_active);
CREATE INDEX ix_customers_tenant_id_name ON customers (tenant_id, name);
CREATE INDEX ix_suppliers_tenant_id_name ON suppliers (tenant_id, name);
CREATE INDEX ix_sales_tenant_id_created_at ON sales (tenant_id, created_at);
CREATE INDEX ix_sales_tenant_id_customer_id ON sales (tenant_id, customer_id);
CREATE INDEX ix_sales_tenant_id_user_id ON sales (tenant_id, user_id);
CREATE INDEX ix_sale_items_tenant_id_product_id ON sale_items (tenant_id, product_id);
CREATE INDEX ix_purchases_tenant_id_created_at ON purchases (tenant_id, created_at);
CREATE INDEX ix_purchases_tenant_id_supplier_id ON purchases (tenant_id, supplier_id);
CREATE INDEX ix_purchases_tenant_id_user_id ON purchases (tenant_id, user_id);
CREATE INDEX ix_purchase_items_tenant_id_product_id ON purchase_items (tenant_id, product_id);
CREATE INDEX ix_crops_tenant_id_product_id ON crops (tenant_id, product_id);
CREATE INDEX ix_production_batches_tenant_id_crop_id ON production_batches (tenant_id, crop_id);
CREATE INDEX ix_production_batches_tenant_id_product_id ON production_batches (tenant_id, product_id);
CREATE INDEX ix_production_batches_tenant_id_status_expected_harvest ON production_batches (tenant_id, status, expected_harvest);
CREATE INDEX ix_inventory_lots_tenant_id_product_id_expiry_date ON inventory_lots (tenant_id, product_id, expiry_date);
CREATE INDEX ix_stock_movements_tenant_id_product_id_created_at ON stock_movements (tenant_id, product_id, created_at);
CREATE INDEX ix_stock_movements_tenant_id_lot_id_product_id ON stock_movements (tenant_id, lot_id, product_id);
CREATE INDEX ix_stock_movements_tenant_id_sale_item_id_product_id ON stock_movements (tenant_id, sale_item_id, product_id);
CREATE INDEX ix_stock_movements_tenant_id_purchase_item_id_product_id ON stock_movements (tenant_id, purchase_item_id, product_id);
CREATE INDEX ix_stock_movements_tenant_id_production_batch_id_product_id ON stock_movements (tenant_id, production_batch_id, product_id);
CREATE INDEX ix_stock_movements_tenant_id_user_id ON stock_movements (tenant_id, user_id);
CREATE INDEX ix_financial_transactions_tenant_id_status_due_date ON financial_transactions (tenant_id, status, due_date);
CREATE INDEX ix_technical_readings_tenant_id_batch_id_recorded_at ON technical_readings (tenant_id, batch_id, recorded_at);
CREATE INDEX ix_technical_readings_tenant_id_recorded_by ON technical_readings (tenant_id, recorded_by);

-- Token aberto significa não consumido e não revogado; expiração é validada pela aplicação.
-- Ao expirar, revogar antes de criar substituto; não usar now() em predicado de índice.
CREATE UNIQUE INDEX uq_refresh_tokens_open_session ON refresh_tokens (session_id) WHERE consumed_at IS NULL AND revoked_at IS NULL;
CREATE UNIQUE INDEX uq_refresh_tokens_root_session ON refresh_tokens (session_id) WHERE parent_token_id IS NULL;
CREATE UNIQUE INDEX uq_password_reset_tokens_open_user ON password_reset_tokens (user_id) WHERE consumed_at IS NULL AND revoked_at IS NULL;

-- Atualização de timestamps é responsabilidade do banco, inclusive para alterações fora do ORM.
CREATE FUNCTION set_updated_at() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    NEW.updated_at := clock_timestamp();
    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_users_updated_at BEFORE UPDATE ON users FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_profiles_updated_at BEFORE UPDATE ON profiles FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_plans_updated_at BEFORE UPDATE ON plans FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_tenants_updated_at BEFORE UPDATE ON tenants FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_tenant_memberships_updated_at BEFORE UPDATE ON tenant_memberships FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_product_categories_updated_at BEFORE UPDATE ON product_categories FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_products_updated_at BEFORE UPDATE ON products FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_customers_updated_at BEFORE UPDATE ON customers FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_suppliers_updated_at BEFORE UPDATE ON suppliers FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_sales_updated_at BEFORE UPDATE ON sales FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_purchases_updated_at BEFORE UPDATE ON purchases FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_crops_updated_at BEFORE UPDATE ON crops FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_production_batches_updated_at BEFORE UPDATE ON production_batches FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_inventory_lots_updated_at BEFORE UPDATE ON inventory_lots FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_financial_transactions_updated_at BEFORE UPDATE ON financial_transactions FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- Razão de estoque imutável: correções usam novos movimentos de estorno.
CREATE FUNCTION reject_stock_movement_change() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    RAISE EXCEPTION 'Movimentações de estoque são imutáveis; registre um estorno' USING ERRCODE = '23514';
END;
$$;
CREATE TRIGGER trg_stock_movements_immutable BEFORE UPDATE OR DELETE ON stock_movements FOR EACH ROW EXECUTE FUNCTION reject_stock_movement_change();

-- Seeds comerciais iniciais. Mudanças de preço posteriores são migrations próprias.
INSERT INTO plans (id, label, price, product_limit, user_limit) VALUES
    ('basico', 'Básico', 99.99, 50, 2),
    ('profissional', 'Profissional', 199.99, NULL, NULL);

-- Invariantes entre linhas (saldo/movimentos, total/itens, limite de plano, último admin,
-- expiração do refresh limitada à sessão e magnitude do estorno) exigem transações
-- nos casos de uso; não são garantidas apenas por CHECK. Ver ARCHITECTURE_DECISIONS.md.
COMMIT;
