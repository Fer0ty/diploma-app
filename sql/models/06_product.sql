DROP TABLE IF EXISTS product CASCADE;
CREATE TABLE IF NOT EXISTS product
(
    product_id     BIGSERIAL PRIMARY KEY,
    tenant_id      BIGINT         NOT NULL,
    name           VARCHAR(255)   NOT NULL,
    description    TEXT,
    price          NUMERIC(10, 2) NOT NULL,
    stock_quantity INT            NOT NULL DEFAULT 0,
    category       VARCHAR(100),
    created_at     TIMESTAMP               DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP               DEFAULT CURRENT_TIMESTAMP,
    is_active      BOOLEAN                 DEFAULT TRUE,
    FOREIGN KEY (tenant_id) REFERENCES tenants (tenant_id) ON DELETE RESTRICT,
    UNIQUE (tenant_id, name),
    UNIQUE (tenant_id, product_id)
);

DROP INDEX IF EXISTS idx_product_tenant_category;
DROP INDEX IF EXISTS idx_product_tenant_active;
DROP INDEX IF EXISTS idx_product_tenant;

CREATE INDEX IF NOT EXISTS idx_product_tenant_category ON product (tenant_id, category);
CREATE INDEX IF NOT EXISTS idx_product_tenant_active ON product (tenant_id, is_active);
CREATE INDEX IF NOT EXISTS idx_product_tenant ON product (tenant_id);

