-- Sample data insertion script

-- Insert roles
INSERT INTO roles (name, description) VALUES 
('ROLE_ADMIN', 'Administrator role with full access'),
('ROLE_USER', 'Regular user role with limited access');

-- Insert sample users (password: 'password')
INSERT INTO users (username, email, password_hash, first_name, last_name) VALUES 
('admin', 'admin@example.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'Admin', 'User'),
('user1', 'user1@example.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'John', 'Doe'),
('user2', 'user2@example.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'Jane', 'Smith');

-- Assign roles to users
INSERT INTO user_roles (user_id, role_id) VALUES 
(1, 1), -- admin has ROLE_ADMIN
(1, 2), -- admin also has ROLE_USER
(2, 2), -- user1 has ROLE_USER
(3, 2); -- user2 has ROLE_USER

-- Insert sample products
INSERT INTO products (name, description, price, sku, inventory_count) VALUES 
('Laptop', 'High-performance laptop with 16GB RAM', 1299.99, 'TECH-1001', 10),
('Smartphone', 'Latest smartphone with 128GB storage', 799.99, 'TECH-2001', 15),
('Headphones', 'Noise-cancelling wireless headphones', 199.99, 'TECH-3001', 20),
('Tablet', '10-inch tablet with 64GB storage', 399.99, 'TECH-4001', 8),
('Smartwatch', 'Fitness tracking smartwatch', 249.99, 'TECH-5001', 12);

-- Insert sample orders
INSERT INTO orders (user_id, status, total_amount, shipping_address, billing_address) VALUES 
(2, 'COMPLETED', 1499.98, '123 Main St, Anytown, USA', '123 Main St, Anytown, USA'),
(3, 'PROCESSING', 649.98, '456 Oak Ave, Somewhere, USA', '456 Oak Ave, Somewhere, USA'),
(2, 'SHIPPED', 199.99, '123 Main St, Anytown, USA', '123 Main St, Anytown, USA');

-- Insert order items
INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES 
(1, 1, 1, 1299.99), -- Laptop in order 1
(1, 3, 1, 199.99),  -- Headphones in order 1
(2, 2, 1, 799.99),  -- Smartphone in order 2
(2, 5, 1, 249.99),  -- Smartwatch in order 2
(3, 3, 1, 199.99);  -- Headphones in order 3