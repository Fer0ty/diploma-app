DROP TABLE IF EXISTS product_ozon_mapping CASCADE;
CREATE TABLE IF NOT EXISTS product_ozon_mapping
(
    mapping_id       BIGSERIAL PRIMARY KEY,
    tenant_id        BIGINT       NOT NULL,
    product_id       BIGINT       NOT NULL,
    ozon_product_id  BIGINT       NOT NULL,
    ozon_sku         BIGINT       NOT NULL,
    ozon_fbo_sku     BIGINT,
    ozon_fbs_sku     BIGINT,
    warehouse_id     BIGINT,
    last_price_sync  TIMESTAMP,
    last_stock_sync  TIMESTAMP,
    sync_status      VARCHAR(20)  DEFAULT 'PENDING',
    sync_error       TEXT,
    is_active        BOOLEAN      DEFAULT TRUE,
    created_at       TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (tenant_id) REFERENCES tenants (tenant_id) ON DELETE RESTRICT,
    FOREIGN KEY (product_id) REFERENCES product (product_id) ON DELETE CASCADE,
    FOREIGN KEY (tenant_id, product_id) REFERENCES product (tenant_id, product_id),
    UNIQUE (tenant_id, product_id),
    UNIQUE (tenant_id, ozon_product_id)
);

CREATE INDEX IF NOT EXISTS idx_product_ozon_mapping_tenant ON product_ozon_mapping (tenant_id);
CREATE INDEX IF NOT EXISTS idx_product_ozon_mapping_product ON product_ozon_mapping (tenant_id, product_id);
CREATE INDEX IF NOT EXISTS idx_product_ozon_mapping_sync_status ON product_ozon_mapping (tenant_id, sync_status);
CREATE INDEX IF NOT EXISTS idx_product_ozon_mapping_active ON product_ozon_mapping (tenant_id, is_active);