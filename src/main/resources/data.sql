-- ─── Seed admin user ────────────────────────────────────────────────────────
-- password: admin123  (BCrypt, 10 rounds)
INSERT IGNORE INTO users (name, email, password, role)
VALUES ('Admin', 'admin@ubuy.com',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh23',
        'ADMIN');

-- ─── Sample products ──────────────────────────────────────────────────────────
INSERT IGNORE INTO products (name, description, price, stock_quantity, category)
VALUES
  ('Classic White T-Shirt', '100% cotton, unisex fit',          19.99, 150, 'Apparel'),
  ('Wireless Earbuds',      'Bluetooth 5.0, 24h battery life',   49.99,  80, 'Electronics'),
  ('Stainless Steel Bottle','750ml, keeps drinks cold for 24h',  24.99, 200, 'Kitchen');

