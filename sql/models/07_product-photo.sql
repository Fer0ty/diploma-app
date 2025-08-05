DROP TABLE IF EXISTS product_photo CASCADE;
CREATE TABLE IF NOT EXISTS product_photo
(
    photo_id      BIGSERIAL PRIMARY KEY,
    tenant_id     BIGINT       NOT NULL,
    product_id    BIGINT       NOT NULL,
    file_path     VARCHAR(255) NOT NULL,
    display_order INT       DEFAULT 0,
    is_main       BOOLEAN   DEFAULT FALSE,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (tenant_id) REFERENCES tenants (tenant_id) ON DELETE RESTRICT,
    FOREIGN KEY (product_id) REFERENCES product (product_id) ON DELETE CASCADE,
    FOREIGN KEY (tenant_id, product_id) REFERENCES product (tenant_id, product_id)
);

DROP INDEX IF EXISTS idx_product_photo_tenant_product;
DROP INDEX IF EXISTS idx_product_photo_tenant_main;
DROP INDEX IF EXISTS idx_product_photo_tenant;

CREATE INDEX IF NOT EXISTS idx_product_photo_tenant_product ON product_photo (tenant_id, product_id);
CREATE INDEX IF NOT EXISTS idx_product_photo_tenant_main ON product_photo (tenant_id, is_main);
CREATE INDEX IF NOT EXISTS idx_product_photo_tenant ON product_photo (tenant_id);

