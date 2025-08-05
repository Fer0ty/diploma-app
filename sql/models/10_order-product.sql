DROP TABLE IF EXISTS order_product CASCADE;
CREATE TABLE IF NOT EXISTS order_product
(
    order_product_id BIGSERIAL PRIMARY KEY,
    tenant_id        BIGINT         NOT NULL,
    order_id         BIGINT         NOT NULL,
    product_id       BIGINT         NOT NULL,
    quantity         INT            NOT NULL,
    unit_price       NUMERIC(10, 2) NOT NULL,
    total_price      NUMERIC(10, 2) NOT NULL,
    FOREIGN KEY (tenant_id) REFERENCES tenants (tenant_id) ON DELETE RESTRICT,
    FOREIGN KEY (order_id) REFERENCES store_order (order_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES product (product_id) ON DELETE SET NULL,
    UNIQUE (tenant_id, order_id, product_id)
);

DROP INDEX IF EXISTS idx_order_product_tenant_order;
DROP INDEX IF EXISTS idx_order_product_tenant_product;
DROP INDEX IF EXISTS idx_order_product_tenant;

CREATE INDEX IF NOT EXISTS idx_order_product_tenant_order ON order_product (tenant_id, order_id);
CREATE INDEX IF NOT EXISTS idx_order_product_tenant_product ON order_product (tenant_id, product_id);
CREATE INDEX IF NOT EXISTS idx_order_product_tenant ON order_product (tenant_id);
