INSERT INTO users (id, email, password_hash, role, active)
VALUES
    (1, 'admin@logistics.com', 'pass123', 'ADMIN', true),
    (2, 'client@logistics.com', 'pass123', 'CLIENT', true),
    (3, 'manager@logistics.com', 'pass123', 'WAREHOUSE_MANAGER', true);

INSERT INTO clients (name, contact_info) VALUES
('ACME Retail', 'acme@contact.com'),
('TechNova', 'sales@technova.com');

INSERT INTO suppliers (name, contact_info) VALUES
('Global Supplies', 'contact@globalsupplies.com'),
('PaperCorp', 'support@papercorp.com');

INSERT INTO warehouses (code, name, active) VALUES
('WHS-01', 'Main Warehouse', true),
('WHS-02', 'Secondary Warehouse', true);

INSERT INTO products (sku, name, category, original_price, final_price, active) VALUES
('P-001', 'Laptop 13”', 'Electronics', 600, 750, true),
('P-002', 'Printer Paper Box', 'Supplies', 3, 5, true),
('P-003', 'Wireless Mouse', 'Electronics', 10, 15, true);

INSERT INTO carriers (code, name, contact_email, phone, max_daily_shipments, max_capacity, cutoff_time, base_shipping_rate, status)
VALUES
('CR-001', 'FastShip', 'ops@fastship.com', '+212612345678', 80, 1000, 16, 7.50, 'ACTIVE'),
('CR-002', 'GlobalExpress', 'support@globalexpress.com', '+212698745632', 50, 700, 15, 6.20, 'ACTIVE');

INSERT INTO inventories (warehouse_id, product_id, qty_on_hand, qty_reserved)
VALUES
(1, 1, 100, 10),
(1, 2, 300, 30),
(2, 3, 80, 5);

INSERT INTO purchase_orders (supplier_id, status, created_at, expected_delivery)
VALUES
(1, 'CREATED', now(), now() + interval '7 days'),
(2, 'APPROVED', now() - interval '3 days', now() + interval '4 days');

INSERT INTO purchase_order_lines (purchase_order_id, product_id, quantity, unit_price)
VALUES
(1, 2, 500, 3),
(1, 3, 200, 10),
(2, 1, 50, 600);

INSERT INTO sales_orders (client_id, status, created_at, reserved_at, shipped_at, delivered_at)
VALUES
(1, 'RESERVED', now() - interval '2 days', now() - interval '1 day', null, null),
(2, 'SHIPPED', now() - interval '4 days', now() - interval '3 days', now() - interval '2 days', null);

INSERT INTO sales_order_lines (sales_order_id, product_id, quantity, unit_price)
VALUES
(1, 1, 5, 750),
(1, 3, 10, 15),
(2, 2, 20, 5);

INSERT INTO shipments (tracking_number, status, planned_date, shipped_date, delivered_date, carrier_id)
VALUES
('TRK-1001', 'IN_TRANSIT', now() - interval '1 day', now() - interval '1 day', null, 1),
('TRK-2001', 'DELIVERED', now() - interval '5 days', now() - interval '4 days', now() - interval '2 days', 2);

INSERT INTO inventory_movements (product_id, warehouse_id, type, quantity, occurred_at, reference_document, description)
VALUES
(1, 1, 'OUTBOUND', 5, now() - interval '1 day', 'SO#1', 'Sales order shipment'),
(3, 1, 'ADJUSTMENT', -2, now() - interval '2 days', 'ADJ#001', 'Damaged stock'),
(2, 2, 'INBOUND', 100, now() - interval '3 days', 'PO#1', 'Purchase order receipt');