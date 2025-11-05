-- USERS
INSERT INTO users (id, email, password_hash, role, active)
VALUES
    (1, 'admin@logistics.com', 'pass123', 'ADMIN', TRUE),
    (2, 'client@logistics.com', 'pass123', 'CLIENT', TRUE),
    (3, 'manager@logistics.com', 'pass123', 'WAREHOUSE_MANAGER', TRUE);

-- CLIENTS
-- Link client to user with user_id = 2 (client@logistics.com)
INSERT INTO clients (id, name, contact_info, user_id)
VALUES
    (1, 'Jack', '0654876459', 2);

-- SUPPLIERS
INSERT INTO suppliers (id, name, contact_info)
VALUES
    (1, 'Global Supplies', 'contact@globalsupplies.com'),
    (2, 'PaperCorp', 'support@papercorp.com');

-- WAREHOUSES
INSERT INTO warehouses (id, code, name, active)
VALUES
    (1, 'WHS-01', 'Main Warehouse', TRUE),
    (2, 'WHS-02', 'Secondary Warehouse', TRUE);

-- PRODUCTS
INSERT INTO products (id, sku, name, category, original_price, final_price, active)
VALUES
    (1, 'P-001', 'Laptop 13”', 'Electronics', 600, 750, TRUE),
    (2, 'P-002', 'Printer Paper Box', 'Supplies', 3, 5, TRUE),
    (3, 'P-003', 'Wireless Mouse', 'Electronics', 10, 15, TRUE);

-- INVENTORIES
INSERT INTO inventories (id, warehouse_id, product_id, qty_on_hand, qty_reserved)
VALUES
    (1, 1, 1, 100, 10),
    (2, 1, 2, 300, 30),
    (3, 2, 3, 80, 5);

-- CARRIERS
INSERT INTO carriers (id, code, name, contact_email, phone, max_daily_shipments, max_capacity, cutoff_time, base_shipping_rate, status)
VALUES
    (1, 'CR-001', 'FastShip', 'ops@fastship.com', '+212612345678', 80, 1000, 16, 7.50, 'ACTIVE'),
    (2, 'CR-002', 'GlobalExpress', 'support@globalexpress.com', '+212698745632', 50, 700, 15, 6.20, 'ACTIVE');

-- SHIPMENTS
INSERT INTO shipments (id, tracking_number, status, planned_date, shipped_date, delivered_date, carrier_id)
VALUES
    (1, 'TRK-1001', 'IN_TRANSIT', now() - interval '1 day', now() - interval '1 day', NULL, 1),
    (2, 'TRK-2001', 'DELIVERED',  now() - interval '5 days', now() - interval '4 days', now() - interval '2 days', 2);

-- SALES ORDERS
INSERT INTO sales_orders (id, client_id, status, created_at, reserved_at, shipped_at, delivered_at)
VALUES
    (1, 1, 'RESERVED', now() - interval '2 days', now() - interval '1 day', NULL, NULL),
    (2, 1, 'SHIPPED',  now() - interval '4 days', now() - interval '3 days', now() - interval '2 days', NULL);

-- SALES ORDER LINES
INSERT INTO sales_order_lines (id, sales_order_id, product_id, quantity, unit_price)
VALUES
    (1, 1, 1, 5, 750),
    (2, 1, 3, 10, 15),
    (3, 2, 2, 20, 5);

-- PURCHASE ORDERS
INSERT INTO purchase_orders (id, supplier_id, status, created_at, expected_delivery)
VALUES
    (1, 1, 'CREATED',  now(),                    now() + interval '7 days'),
    (2, 2, 'APPROVED', now() - interval '3 days', now() + interval '4 days');

-- PURCHASE ORDER LINES
INSERT INTO purchase_order_line (id, purchase_order_id, product_id, quantity, unit_price)
VALUES
    (1, 1, 2, 500, 3),
    (2, 1, 3, 200, 10),
    (3, 2, 1, 50, 600);

-- INVENTORY MOVEMENTS
INSERT INTO inventory_movements (id, product_id, warehouse_id, type, quantity, occurred_at, reference_document, description)
VALUES
    (1, 1, 1, 'OUTBOUND',   5,  now() - interval '1 day', 'SO#1', 'Sales order shipment'),
    (2, 3, 1, 'ADJUSTMENT', -2, now() - interval '2 days', 'ADJ#001', 'Damaged stock'),
    (3, 2, 2, 'INBOUND',    100, now() - interval '3 days', 'PO#1', 'Purchase order receipt');
