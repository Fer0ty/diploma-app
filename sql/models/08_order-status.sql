DROP TABLE IF EXISTS order_status CASCADE;
CREATE TABLE IF NOT EXISTS order_status
(
    status_id   BIGSERIAL PRIMARY KEY,
    status_name VARCHAR(50) NOT NULL UNIQUE
);

INSERT INTO order_status (status_name)
VALUES ('Created'),
       ('Paid'),
       ('Processing'),
       ('Shipped'),
       ('Delivered'),
       ('Canceled'),
       ('Returned')
ON CONFLICT (status_name) DO NOTHING;

