USE inventory_db;

-- =========================
-- PRODUCTS TABLE
-- =========================
CREATE TABLE IF NOT EXISTS products (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(100) NOT NULL,
    quantity INT NOT NULL DEFAULT 0,
    price DOUBLE NOT NULL DEFAULT 0,
    supplier VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =========================
-- SUPPLIERS TABLE
-- =========================
CREATE TABLE IF NOT EXISTS suppliers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    contact VARCHAR(50),
    email VARCHAR(100),
    address VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =========================
-- CATEGORIES TABLE
-- =========================
CREATE TABLE IF NOT EXISTS categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =========================
-- STOCK HISTORY TABLE
-- =========================
CREATE TABLE IF NOT EXISTS stock_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    product_id INT NOT NULL,
    product_name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL,
    quantity INT NOT NULL,
    remarks VARCHAR(255),
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- OPTIONAL: FK (safe if products already exist)
ALTER TABLE stock_history
ADD CONSTRAINT fk_stock_product
FOREIGN KEY (product_id) REFERENCES products(id)
ON DELETE CASCADE;