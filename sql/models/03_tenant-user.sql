DROP TABLE IF EXISTS tenant_user CASCADE;
CREATE TABLE IF NOT EXISTS tenant_user
(
    user_id       BIGSERIAL PRIMARY KEY,
    tenant_id     BIGINT       NOT NULL,
    username      VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name    VARCHAR(100),
    last_name     VARCHAR(100),
    email         VARCHAR(255) NOT NULL UNIQUE,
    role          VARCHAR(255) NOT NULL DEFAULT 'ROLE_ADMIN',
    is_active     BOOLEAN               DEFAULT TRUE,
    created_at    TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_tenant_user_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenants (tenant_id)
            ON DELETE CASCADE,
    CONSTRAINT uq_tenant_user_username UNIQUE (tenant_id, username),
    CONSTRAINT uq_tenant_user_email UNIQUE (tenant_id, email)
);

DROP INDEX IF EXISTS idx_tenant_user_tenant_username;
DROP INDEX IF EXISTS idx_tenant_user_tenant_email;
DROP INDEX IF EXISTS idx_tenant_user_tenant;

CREATE INDEX IF NOT EXISTS idx_tenant_user_tenant_username ON tenant_user (tenant_id, username);
CREATE INDEX IF NOT EXISTS idx_tenant_user_tenant_email ON tenant_user (tenant_id, email);
CREATE INDEX IF NOT EXISTS idx_tenant_user_tenant ON tenant_user (tenant_id); -- Индекс только по tenant_id

CREATE OR REPLACE FUNCTION update_updated_at_column()
    RETURNS TRIGGER AS
$$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_tenant_user_updated_at
    BEFORE UPDATE
    ON tenant_user
    FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();