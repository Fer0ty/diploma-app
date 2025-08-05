CREATE TABLE IF NOT EXISTS tenants
(
    tenant_id  BIGSERIAL PRIMARY KEY,
    name       VARCHAR(255) NOT NULL UNIQUE,
    subdomain  VARCHAR(100) UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active  BOOLEAN   DEFAULT TRUE,
    theme_id   BIGINT,
    CONSTRAINT fk_tenant_theme FOREIGN KEY (theme_id) REFERENCES themes(theme_id)
);

DROP INDEX IF EXISTS idx_tenants_subdomain;
CREATE INDEX IF NOT EXISTS idx_tenants_subdomain ON tenants (subdomain);

ALTER TABLE tenants
    ADD COLUMN IF NOT EXISTS contact_phone VARCHAR(50),
    ADD COLUMN IF NOT EXISTS contact_email VARCHAR(255),
    ADD COLUMN IF NOT EXISTS ozon_api_key VARCHAR(500),
    ADD COLUMN IF NOT EXISTS ozon_client_id VARCHAR(500),
    ADD COLUMN IF NOT EXISTS ozon_warehouse_id BIGINT,
    ADD COLUMN IF NOT EXISTS ozon_sync_enabled BOOLEAN DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS ozon_last_sync_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS wildberries_api_key VARCHAR(500),
    ADD COLUMN IF NOT EXISTS wildberries_api_key VARCHAR(500),
    ADD COLUMN IF NOT EXISTS wildberries_warehouse_id BIGINT,
    ADD COLUMN IF NOT EXISTS wildberries_sync_enabled BOOLEAN DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS wildberries_last_sync_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS yookassa_idempotency_key VARCHAR(500),
    ADD COLUMN IF NOT EXISTS yookassa_secret_key VARCHAR(500);