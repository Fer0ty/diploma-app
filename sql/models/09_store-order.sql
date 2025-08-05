DROP TABLE IF EXISTS store_order CASCADE;
CREATE TABLE IF NOT EXISTS store_order
(
    order_id     BIGSERIAL PRIMARY KEY,
    tenant_id    BIGINT         NOT NULL,
    customer_id  BIGINT         NOT NULL,
    address_id   BIGINT         NOT NULL,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_amount NUMERIC(10, 2) NOT NULL,
    status_id    BIGINT         NOT NULL,
    comment      TEXT,
    FOREIGN KEY (tenant_id) REFERENCES tenants (tenant_id) ON DELETE RESTRICT,
    FOREIGN KEY (customer_id) REFERENCES customer (customer_id) ON DELETE SET NULL,
    FOREIGN KEY (address_id) REFERENCES address (address_id) ON DELETE SET NULL,
    FOREIGN KEY (status_id) REFERENCES order_status (status_id)
);

DROP INDEX IF EXISTS idx_order_tenant_customer;
DROP INDEX IF EXISTS idx_order_tenant_status;
DROP INDEX IF EXISTS idx_order_tenant_date;
DROP INDEX IF EXISTS idx_order_tenant;

CREATE INDEX IF NOT EXISTS idx_order_tenant_customer ON store_order (tenant_id, customer_id);
CREATE INDEX IF NOT EXISTS idx_order_tenant_status ON store_order (tenant_id, status_id);
CREATE INDEX IF NOT EXISTS idx_order_tenant_date ON store_order (tenant_id, created_at);
CREATE INDEX IF NOT EXISTS idx_order_tenant ON store_order (tenant_id);
