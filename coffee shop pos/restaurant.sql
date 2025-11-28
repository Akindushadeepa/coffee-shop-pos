-- Coffee Shop Management System Database Setup V2
-- Enhanced with Images, Kitchen Orders, and User Management

CREATE DATABASE IF NOT EXISTS restaurant;
USE restaurant;

-- Table 1: Food Items (with image support)
CREATE TABLE IF NOT EXISTS food (
    id INT AUTO_INCREMENT PRIMARY KEY,
    fName VARCHAR(100) NOT NULL,
    fPrice DOUBLE NOT NULL,
    image_path VARCHAR(255) DEFAULT NULL,
    available BOOLEAN DEFAULT TRUE,
    category VARCHAR(50) DEFAULT 'General',
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_available (available),
    INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table 2: Bills
CREATE TABLE IF NOT EXISTS bills (
    id VARCHAR(64) PRIMARY KEY,
    bill_date DATETIME NOT NULL,
    total DOUBLE NOT NULL,
    table_number VARCHAR(20) DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_date (bill_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table 3: Bill Items (Order Details)
CREATE TABLE IF NOT EXISTS bill_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    bill_id VARCHAR(64) NOT NULL,
    food_id INT NOT NULL,
    qty INT NOT NULL,
    unit_price DOUBLE NOT NULL,
    total DOUBLE NOT NULL,
    FOREIGN KEY (bill_id) REFERENCES bills(id) ON DELETE CASCADE,
    FOREIGN KEY (food_id) REFERENCES food(id) ON DELETE RESTRICT,
    INDEX idx_bill_id (bill_id),
    INDEX idx_food_id (food_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table 4: Kitchen Orders
CREATE TABLE IF NOT EXISTS kitchen_orders (
    id INT AUTO_INCREMENT PRIMARY KEY,
    bill_id VARCHAR(64) NOT NULL,
    food_id INT NOT NULL,
    name VARCHAR(255) NOT NULL,
    qty INT NOT NULL,
    table_number VARCHAR(20) DEFAULT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    completed_at DATETIME DEFAULT NULL,
    INDEX idx_bill_id (bill_id),
    INDEX idx_status (status),
    INDEX idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table 5: Users (for authentication)
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    full_name VARCHAR(100),
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Insert default users
INSERT IGNORE INTO users (username, password, role, full_name) VALUES
('admin', 'admin', 'ADMIN', 'System Administrator'),
('cashier', '12345', 'CASHIER', 'Cashier User'),
('kitchen', 'kitchen', 'KITCHEN', 'Kitchen Staff');

-- Insert sample food items with categories
INSERT INTO food (fName, fPrice, category, description, available) VALUES
('Espresso', 250.00, 'Hot Coffee', 'Strong and bold Italian coffee', TRUE),
('Cappuccino', 350.00, 'Hot Coffee', 'Espresso with steamed milk foam', TRUE),
('Latte', 400.00, 'Hot Coffee', 'Smooth espresso with steamed milk', TRUE),
('Americano', 300.00, 'Hot Coffee', 'Espresso with hot water', TRUE),
('Mocha', 450.00, 'Hot Coffee', 'Chocolate flavored coffee', TRUE),
('Hot Chocolate', 350.00, 'Hot Drinks', 'Rich chocolate drink', TRUE),
('Iced Coffee', 400.00, 'Cold Coffee', 'Refreshing cold brew', TRUE),
('Iced Latte', 450.00, 'Cold Coffee', 'Cold milk with espresso', TRUE),
('Frappe', 500.00, 'Cold Coffee', 'Blended iced coffee', TRUE),
('Croissant', 200.00, 'Bakery', 'Buttery French pastry', TRUE),
('Muffin', 180.00, 'Bakery', 'Fresh baked muffin', TRUE),
('Sandwich', 450.00, 'Food', 'Grilled sandwich', TRUE),
('Cake Slice', 350.00, 'Dessert', 'Fresh cake slice', TRUE),
('Cookie', 100.00, 'Dessert', 'Homemade cookie', TRUE),
('Green Tea', 300.00, 'Hot Drinks', 'Healthy green tea', TRUE);

-- View: Daily Sales Summary
CREATE OR REPLACE VIEW daily_sales AS
SELECT 
    DATE(bill_date) as sale_date,
    COUNT(*) as total_orders,
    SUM(total) as total_sales,
    AVG(total) as average_order
FROM bills
GROUP BY DATE(bill_date)
ORDER BY sale_date DESC;

-- View: Item Sales Analytics
CREATE OR REPLACE VIEW item_sales AS
SELECT 
    f.id,
    f.fName as item_name,
    f.category,
    f.fPrice as current_price,
    COALESCE(SUM(bi.qty), 0) as total_sold,
    COALESCE(SUM(bi.total), 0) as total_revenue,
    f.available
FROM food f
LEFT JOIN bill_items bi ON f.id = bi.food_id
GROUP BY f.id, f.fName, f.category, f.fPrice, f.available
ORDER BY total_revenue DESC;

-- View: Kitchen Performance
CREATE OR REPLACE VIEW kitchen_stats AS
SELECT 
    DATE(created_at) as order_date,
    COUNT(*) as total_orders,
    SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END) as completed,
    SUM(CASE WHEN status = 'PENDING' THEN 1 ELSE 0 END) as pending,
    AVG(TIMESTAMPDIFF(MINUTE, created_at, completed_at)) as avg_completion_time
FROM kitchen_orders
GROUP BY DATE(created_at)
ORDER BY order_date DESC;

-- Stored Procedure: Get Available Items by Category
DELIMITER //
CREATE PROCEDURE IF NOT EXISTS GetItemsByCategory(IN cat VARCHAR(50))
BEGIN
    SELECT id, fName, fPrice, image_path, description
    FROM food
    WHERE category = cat AND available = TRUE
    ORDER BY fName;
END //
DELIMITER ;

-- Show tables created
SHOW TABLES;
SELECT 'Database setup completed successfully!' as Status;
SELECT 'Default users created: admin/admin, cashier/12345, kitchen/kitchen' as Info;