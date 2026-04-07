
/* ============================================================
LOGISTICS SQL SENIOR — V2 (LIGHT VERSION, POSTGRESQL)
------------------------------------------------------------
13 real-world business queries for senior-level SQL practice.
Each question includes:
  - Business Context
  - Goal
  - Expected Output
  - Evaluation Criteria
  - Hint
  - Optimized Solution with explanation
============================================================ */

/* ============================================================
BÀI 1 — Warehouse Utilization Analysis
------------------------------------------------------------ */
SELECT 
  w.id AS warehouse_id,
  w.name AS warehouse_name,
  ROUND(w.current_stock::NUMERIC / NULLIF(w.capacity,0), 2) AS utilization_ratio,
  CASE 
    WHEN w.current_stock::NUMERIC / w.capacity < 0.5 THEN 'Low'
    WHEN w.current_stock::NUMERIC / w.capacity < 0.8 THEN 'Medium'
    ELSE 'High'
  END AS status
FROM warehouses w
WHERE w.capacity > 0
ORDER BY utilization_ratio DESC;

/* ============================================================
BÀI 2 — Supplier Performance Analysis
------------------------------------------------------------ */
WITH recent AS (
  SELECT *
  FROM receipts
  WHERE received_date >= NOW() - INTERVAL '90 days'
)
SELECT 
  s.name AS supplier_name,
  ROUND(AVG(EXTRACT(DAY FROM (r.received_date - r.order_date))), 2) AS avg_lead_time_days
FROM recent r
JOIN suppliers s ON s.id = r.supplier_id
WHERE r.received_date IS NOT NULL AND r.order_date IS NOT NULL
GROUP BY s.name
ORDER BY avg_lead_time_days ASC;

/* ============================================================
BÀI 3 — Delivery Delay Report
------------------------------------------------------------ */
SELECT 
  o.id AS order_id,
  c.name AS customer_name,
  o.expected_delivery_date,
  o.actual_delivery_date,
  EXTRACT(DAY FROM o.actual_delivery_date - o.expected_delivery_date) AS delay_days
FROM orders o
JOIN customers c ON c.id = o.customer_id
WHERE o.actual_delivery_date > o.expected_delivery_date
ORDER BY delay_days DESC;

/* ============================================================
BÀI 4 — Monthly Revenue by Region
------------------------------------------------------------ */
SELECT 
  r.name AS region,
  TO_CHAR(DATE_TRUNC('month', o.sale_date), 'YYYY-MM') AS month,
  SUM(o.amount) AS total_revenue
FROM orders o
JOIN regions r ON r.id = o.region_id
WHERE o.status = 'COMPLETED'
GROUP BY r.name, DATE_TRUNC('month', o.sale_date)
ORDER BY month, total_revenue DESC;

/* ============================================================
BÀI 5 — Top Products by Profit Margin
------------------------------------------------------------ */
SELECT 
  p.name AS product_name,
  ROUND((p.sale_price - p.cost_price) / NULLIF(p.sale_price,0), 2) AS profit_margin
FROM products p
ORDER BY profit_margin DESC
LIMIT 10;

/* ============================================================
BÀI 6 — Customer Lifetime Value (CLV)
------------------------------------------------------------ */
SELECT 
  c.id AS customer_id,
  c.name AS customer_name,
  COUNT(o.id) AS total_orders,
  SUM(o.amount) AS total_spent,
  ROUND(SUM(o.amount)/NULLIF(COUNT(o.id),0),2) AS avg_order_value
FROM customers c
JOIN orders o ON o.customer_id = c.id
WHERE o.status='COMPLETED'
GROUP BY c.id, c.name
ORDER BY total_spent DESC;

/* ============================================================
BÀI 7 — Inventory Reorder Suggestion
------------------------------------------------------------ */
SELECT 
  p.id AS product_id,
  p.name AS product_name,
  p.stock_qty,
  p.reorder_level,
  (p.reorder_level - p.stock_qty) AS reorder_needed
FROM products p
WHERE p.stock_qty < p.reorder_level
ORDER BY reorder_needed DESC;

/* ============================================================
BÀI 8 — Peak Delivery Hours
------------------------------------------------------------ */
SELECT 
  EXTRACT(HOUR FROM d.delivery_time) AS hour_of_day,
  COUNT(*) AS deliveries_count
FROM deliveries d
WHERE d.delivery_date >= NOW() - INTERVAL '7 days'
GROUP BY hour_of_day
ORDER BY deliveries_count DESC;

/* ============================================================
BÀI 9 — Order Fulfillment Rate by Warehouse
------------------------------------------------------------ */
SELECT 
  w.id AS warehouse_id,
  w.name AS warehouse_name,
  COUNT(o.id) AS total_orders,
  SUM(CASE WHEN o.status='FULFILLED' THEN 1 ELSE 0 END) AS fulfilled_orders,
  ROUND(SUM(CASE WHEN o.status='FULFILLED' THEN 1 ELSE 0 END)::NUMERIC / COUNT(o.id),2) AS fulfillment_rate
FROM orders o
JOIN warehouses w ON w.id = o.warehouse_id
GROUP BY w.id, w.name
ORDER BY fulfillment_rate DESC;

/* ============================================================
BÀI 10 — Top Delayed Routes
------------------------------------------------------------ */
SELECT 
  r.route_name,
  COUNT(o.id) AS delayed_shipments,
  ROUND(AVG(EXTRACT(DAY FROM (o.actual_delivery_date - o.expected_delivery_date))),2) AS avg_delay_days
FROM orders o
JOIN routes r ON r.id = o.route_id
WHERE o.actual_delivery_date > o.expected_delivery_date
GROUP BY r.route_name
ORDER BY avg_delay_days DESC
LIMIT 5;

/* ============================================================
BÀI 11 — Revenue Contribution by Product Category
------------------------------------------------------------ */
SELECT 
  c.name AS category_name,
  SUM(o.amount) AS total_revenue,
  ROUND(SUM(o.amount)*100.0 / SUM(SUM(o.amount)) OVER (), 2) AS percent_contribution
FROM orders o
JOIN products p ON p.id = o.product_id
JOIN categories c ON c.id = p.category_id
WHERE o.status='COMPLETED'
GROUP BY c.name
ORDER BY percent_contribution DESC;

/* ============================================================
BÀI 12 — Supplier On-Time Delivery Rate
------------------------------------------------------------ */
SELECT 
  s.name AS supplier_name,
  COUNT(r.id) AS total_deliveries,
  SUM(CASE WHEN r.received_date <= r.expected_date THEN 1 ELSE 0 END) AS on_time,
  ROUND(SUM(CASE WHEN r.received_date <= r.expected_date THEN 1 ELSE 0 END)::NUMERIC / COUNT(r.id), 2) AS on_time_rate
FROM receipts r
JOIN suppliers s ON s.id = r.supplier_id
GROUP BY s.name
ORDER BY on_time_rate DESC;

/* ============================================================
BÀI 13 — Product Sales Trend (3 Months Rolling)
------------------------------------------------------------ */
SELECT 
  p.name AS product_name,
  TO_CHAR(DATE_TRUNC('month', o.sale_date), 'YYYY-MM') AS month,
  SUM(o.amount) AS monthly_sales,
  ROUND(AVG(SUM(o.amount)) OVER (PARTITION BY p.id ORDER BY DATE_TRUNC('month', o.sale_date) ROWS BETWEEN 2 PRECEDING AND CURRENT ROW),2) AS rolling_3m_avg
FROM orders o
JOIN products p ON p.id = o.product_id
WHERE o.status='COMPLETED'
GROUP BY p.name, p.id, DATE_TRUNC('month', o.sale_date)
ORDER BY p.name, month;
