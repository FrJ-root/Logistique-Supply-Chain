CREATE TABLE users(
    id UUID PRIMARY KEY,
    email VARCHAR(120) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(30),
    active BOOLEAN
);

CREATE TABLE clients(
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    contact_info VARCHAR(255)
);

CREATE TABLE suppliers(
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    contact_info VARCHAR(255)
);

CREATE TABLE warehouses(
    id SERIAL PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    active BOOLEAN
);

CREATE TABLE products(
    id SERIAL PRIMARY KEY,
    sku VARCHAR(100) UNIQUE,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(255),
    original_price DECIMAL,
    final_price DECIMAL,
    active BOOLEAN
);

CREATE TABLE inventories(
    id SERIAL PRIMARY KEY,
    warehouse_id INT REFERENCES warehouses(id),
    product_id INT REFERENCES products(id),
    qty_on_hand INT,
    qty_reserved INT,
    UNIQUE(warehouse_id, product_id)
);

CREATE TABLE carriers(
    id SERIAL PRIMARY KEY,
    code VARCHAR(100),
    name VARCHAR(255),
    contact_email VARCHAR(255),
    phone VARCHAR(50),
    max_daily_shipments INT,
    max_capacity INT,
    cutoff_time INT,
    base_shipping_rate DECIMAL,
    status VARCHAR(50)
);

CREATE TABLE shipments(
    id SERIAL PRIMARY KEY,
    tracking_number VARCHAR(255),
    status VARCHAR(50),
    planned_date TIMESTAMP,
    shipped_date TIMESTAMP,
    delivered_date TIMESTAMP,
    carrier_id INT REFERENCES carriers(id)
);

CREATE TABLE sales_orders(
    id SERIAL PRIMARY KEY,
    client_id INT REFERENCES clients(id),
    status VARCHAR(50),
    created_at TIMESTAMP,
    reserved_at TIMESTAMP,
    shipped_at TIMESTAMP,
    delivered_at TIMESTAMP
);

CREATE TABLE sales_order_lines(
    id SERIAL PRIMARY KEY,
    sales_order_id INT REFERENCES sales_orders(id),
    product_id INT REFERENCES products(id),
    quantity INT,
    unit_price DECIMAL
);

CREATE TABLE purchase_orders(
    id SERIAL PRIMARY KEY,
    supplier_id INT REFERENCES suppliers(id),
    status VARCHAR(50),
    created_at TIMESTAMP,
    expected_delivery TIMESTAMP
);

CREATE TABLE purchase_order_lines(
    id SERIAL PRIMARY KEY,
    purchase_order_id INT REFERENCES purchase_orders(id),
    product_id INT REFERENCES products(id),
    quantity INT,
    unit_price DECIMAL
);

CREATE TABLE inventory_movements(
    id SERIAL PRIMARY KEY,
    product_id INT REFERENCES products(id),
    warehouse_id INT REFERENCES warehouses(id),
    type VARCHAR(50),
    quantity INT,
    occurred_at TIMESTAMP,
    reference_document VARCHAR(255),
    description VARCHAR(255)
);