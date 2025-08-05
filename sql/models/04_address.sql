DROP TABLE IF EXISTS address CASCADE;
CREATE TABLE IF NOT EXISTS address
(
    address_id   BIGSERIAL PRIMARY KEY,
    tenant_id    BIGINT       NOT NULL,
    country      VARCHAR(100) NOT NULL,
    city         VARCHAR(100) NOT NULL,
    street       VARCHAR(255) NOT NULL,
    house_number VARCHAR(20)  NOT NULL,
    apartment    VARCHAR(20),
    postal_code  VARCHAR(20),
    comment      TEXT,
    FOREIGN KEY (tenant_id) REFERENCES tenants (tenant_id) ON DELETE RESTRICT
);

DROP INDEX IF EXISTS idx_address_tenant;
CREATE INDEX IF NOT EXISTS idx_address_tenant ON address (tenant_id);

