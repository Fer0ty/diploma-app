DROP TABLE IF EXISTS customer CASCADE;
CREATE TABLE IF NOT EXISTS customer
(
    customer_id BIGSERIAL PRIMARY KEY,
    tenant_id   BIGINT       NOT NULL,
    first_name  VARCHAR(100) NOT NULL,
    last_name   VARCHAR(100) NOT NULL,
    patronymic  VARCHAR(100),
    email       VARCHAR(255) NOT NULL,
    phone       VARCHAR(20),
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active   BOOLEAN   DEFAULT TRUE,
    FOREIGN KEY (tenant_id) REFERENCES tenants (tenant_id) ON DELETE RESTRICT
);

DROP INDEX IF EXISTS idx_customer_tenant_email;
CREATE INDEX IF NOT EXISTS idx_customer_tenant_email ON customer (tenant_id, email);

DROP INDEX IF EXISTS idx_customer_tenant;
CREATE INDEX IF NOT EXISTS idx_customer_tenant ON customer (tenant_id);

