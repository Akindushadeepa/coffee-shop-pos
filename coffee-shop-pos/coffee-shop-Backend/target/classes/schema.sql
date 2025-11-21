CREATE TABLE products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY, -- මෙහි AUTO_INCREMENT තිබිය යුතුය!
    name VARCHAR(255) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    category VARCHAR(255),
    available BOOLEAN
);

-- ඔබ Order Table එකත් මෙහි සකස් කළේ නම්:
CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_date TIMESTAMP NOT NULL,
    status VARCHAR(255) NOT NULL,
    total_amount FLOAT(53) NOT NULL
);