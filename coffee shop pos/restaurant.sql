-- Coffee Shop Management System Database Setup
-- Run this in your MySQL (XAMPP phpMyAdmin)

-- Create database if not exists
CREATE DATABASE IF NOT EXISTS restaurant;
USE restaurant;

-- Table 1: Food Items
CREATE TABLE IF NOT EXISTS food (
    id INT AUTO_INCREMENT PRIMARY KEY,
    fName VARCHAR(100) NOT NULL,
    fPrice DOUBLE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table 2: Bills
CREATE TABLE IF NOT EXISTS bills (
    id VARCHAR(64) PRIMARY KEY,
    bill_date DATETIME NOT NULL,
    total DOUBLE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
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

-- Insert sample food items
INSERT INTO food (fName, fPrice) VALUES
('Espresso', 250.00),
('Cappuccino', 350.00),
('Latte', 400.00),
('Americano', 300.00),
('Mocha', 450.00),
('Hot Chocolate', 350.00),
('Iced Coffee', 400.00),
('Croissant', 200.00),
('Muffin', 180.00),
('Sandwich', 450.00),
('Cake Slice', 350.00),
('Cookie', 100.00);

-- View to get sales summary
CREATE OR REPLACE VIEW sales_summary AS
SELECT 
    DATE(bill_date) as sale_date,
    COUNT(*) as total_bills,
    SUM(total) as total_sales
FROM bills
GROUP BY DATE(bill_date)
ORDER BY sale_date DESC;

-- View to get item-wise sales
CREATE OR REPLACE VIEW item_sales AS
SELECT 
    f.id,
    f.fName as item_name,
    SUM(bi.qty) as total_quantity_sold,
    SUM(bi.total) as total_revenue
FROM bill_items bi
JOIN food f ON bi.food_id = f.id
GROUP BY f.id, f.fName
ORDER BY total_revenue DESC;

-- Show tables created
SHOW TABLES;
SELECT 'Database setup completed successfully!' as Status;