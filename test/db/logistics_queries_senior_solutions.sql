
-- logistics_queries_senior_solutions.sql
-- Solutions (PostgreSQL) for the 13 Senior-level Logistics & Supply Chain challenges.
-- Generated: 2025-10-14
-- Each section: Problem summary, Solution SQL, Explanation & Performance notes.

/* 1) In-transit volume between source region and destination region (last 90 days)
   Problem: for shipments with status='IN_TRANSIT', sum shipped_qty grouped by source_region and dest_region
*/
-- Solution
WITH transit AS (
  SELECT
    sr.region_name AS source_region,
    dr.region_name AS dest_region,
    SUM(sh.shipped_qty) AS total_in_transit
  FROM shipments sh
  JOIN warehouses sw ON sh.source_warehouse = sw.warehouse_id
  JOIN warehouses dw ON sh.dest_warehouse = dw.warehouse_id
  JOIN regions sr ON sw.region_id = sr.region_id
  JOIN regions dr ON dw.region_id = dr.region_id
  WHERE sh.status = 'IN_TRANSIT'
    AND sh.ship_date >= now() - interval '90 days'
  GROUP BY 1,2
)
SELECT source_region, dest_region, total_in_transit
FROM transit
ORDER BY total_in_transit DESC;

-- Explanation:
-- Uses joins to map warehouses -> regions then aggregates shipped_qty. 
-- Performance: create index on shipments(status, ship_date, source_warehouse, dest_warehouse, shipped_qty)
-- Partial index idea: CREATE INDEX idx_ship_in_transit_date ON shipments(ship_date) WHERE status='IN_TRANSIT';

--------------------------------------------------------------------------------
/* 2) Top 5 products with highest inventory turnover in last 3 months.
   Turnover = total_outflow / avg_inventory. Outflow approximated by SUM(shipped_qty WHERE source=warehouse) + SUM(orders.quantity).
*/
-- Solution (approximate using shipments as outflow + orders where status indicates shipped/completed)
WITH outflow AS (
  SELECT product_id, SUM(shipped_qty) AS shipped_out
  FROM shipments
  WHERE ship_date >= now() - interval '3 months'
  GROUP BY product_id
),
ordered_out AS (
  SELECT product_id, SUM(quantity) AS ordered_qty
  FROM orders
  WHERE order_date >= now() - interval '3 months' AND status IN ('SHIPPED','COMPLETED')
  GROUP BY product_id
),
total_out AS (
  SELECT COALESCE(o.product_id, r.product_id) AS product_id,
         COALESCE(shipped_out,0) + COALESCE(ordered_qty,0) AS total_outflow
  FROM outflow o
  FULL JOIN ordered_out r ON o.product_id = r.product_id
),
avg_inventory AS (
  -- approximate avg inventory using current stock across warehouses (could be improved with historical snapshots)
  SELECT i.product_id, AVG(i.stock_qty) AS avg_stock
  FROM inventory i
  GROUP BY i.product_id
)
SELECT p.product_id, p.sku, p.name, p.category,
       t.total_outflow,
       ai.avg_stock,
       CASE WHEN ai.avg_stock IS NULL OR ai.avg_stock = 0 THEN NULL
            ELSE ROUND(t.total_outflow::numeric / NULLIF(ai.avg_stock,0), 4) END AS turnover_ratio
FROM total_out t
JOIN products p ON p.product_id = t.product_id
LEFT JOIN avg_inventory ai ON ai.product_id = t.product_id
ORDER BY turnover_ratio DESC NULLS LAST
LIMIT 5;

-- Explanation:
-- Uses shipments + orders to estimate outflow. For more accurate turnover you'd need daily inventory snapshots.
-- Performance: index shipments(ship_date, product_id), orders(order_date, product_id), inventory(product_id)

--------------------------------------------------------------------------------
/* 3) Warehouses at risk of congestion (current_stock / capacity > 0.9 AND few active orders last 30 days)
*/
-- Solution
WITH active_orders AS (
  SELECT warehouse_id, COUNT(*) AS active_orders_count
  FROM orders
  WHERE order_date >= now() - interval '30 days' AND status NOT IN ('CANCELLED')
  GROUP BY warehouse_id
)
SELECT w.warehouse_id, w.code, w.capacity, w.current_stock,
       ROUND((w.current_stock::numeric / w.capacity) * 100,2) AS pct_filled,
       COALESCE(a.active_orders_count,0) AS active_orders_count
FROM warehouses w
LEFT JOIN active_orders a ON w.warehouse_id = a.warehouse_id
WHERE (w.current_stock::numeric / w.capacity) > 0.9
  AND COALESCE(a.active_orders_count,0) < 10
ORDER BY pct_filled DESC;

-- Explanation:
-- Simple metric combining utilization and low outbound throughput. Useful for identifying overstock risk.
-- Performance: index warehouses(code), orders(warehouse_id, order_date)

--------------------------------------------------------------------------------
/* 4) Shipments delayed > 2 days (arrival_date - ship_date > 2 days) grouped by destination region
*/
-- Solution
SELECT dr.region_name AS dest_region,
       COUNT(*) AS delayed_shipments,
       ROUND(AVG(EXTRACT(EPOCH FROM (sh.arrival_date - sh.ship_date))/86400)::numeric,2) AS avg_delay_days
FROM shipments sh
JOIN warehouses dw ON sh.dest_warehouse = dw.warehouse_id
JOIN regions dr ON dw.region_id = dr.region_id
WHERE sh.arrival_date IS NOT NULL
  AND sh.arrival_date > sh.ship_date + interval '2 days'
GROUP BY dr.region_name
ORDER BY delayed_shipments DESC;

-- Explanation:
-- Uses epoch diff to compute fractional days. Consider partitioning shipments by ship_date for large data.
-- Performance: index shipments(dest_warehouse, arrival_date, ship_date)

--------------------------------------------------------------------------------
/* 5) Average lead time per supplier (using receipts.received_date).
   Because receipts has no 'order_date', we approximate lead time by difference between consecutive receipts for same supplier-product pair
   or by comparing received_date to inventory.last_restock_date where possible.
*/
-- Solution (approximate via receipts: compute average days between successive receipts per supplier-product)
WITH r AS (
  SELECT supplier_id, product_id, received_date,
         LAG(received_date) OVER (PARTITION BY supplier_id, product_id ORDER BY received_date) AS prev_received
  FROM receipts
)
SELECT r.supplier_id, su.name,
       ROUND(AVG(EXTRACT(DAY FROM (r.received_date - r.prev_received)))::numeric,2) AS avg_lead_days_between_receipts,
       COUNT(*) FILTER (WHERE r.prev_received IS NOT NULL) AS intervals_count
FROM r
JOIN suppliers su ON r.supplier_id = su.supplier_id
GROUP BY r.supplier_id, su.name
ORDER BY avg_lead_days_between_receipts NULLS LAST;

-- Explanation:
-- This gives average spacing between receipts, which approximates cadence/lead-time variability.
-- For precise PO lead time you'd need PO dates. Index receipts(supplier_id, product_id, received_date).

--------------------------------------------------------------------------------
/* 6) Net profit by region and product category for last 6 months
   Profit = revenue_from_orders (total_amount) - cost_from_receipts (received_qty * cost_per_unit) matched by product+region.
   We attribute receipts to warehouse's region, and orders to warehouse's region.
*/
-- Solution
WITH ord AS (
  SELECT p.category, w.region_id, SUM(o.total_amount) AS revenue
  FROM orders o
  JOIN products p ON o.product_id = p.product_id
  JOIN warehouses w ON o.warehouse_id = w.warehouse_id
  WHERE o.order_date >= now() - interval '6 months' AND o.status IN ('COMPLETED','SHIPPED')
  GROUP BY p.category, w.region_id
),
costs AS (
  SELECT p.category, w.region_id, SUM(r.received_qty * r.cost_per_unit) AS cost
  FROM receipts r
  JOIN products p ON r.product_id = p.product_id
  JOIN warehouses w ON r.warehouse_id = w.warehouse_id
  WHERE r.received_date >= (now() - interval '6 months')::date
  GROUP BY p.category, w.region_id
)
SELECT reg.region_name, COALESCE(ord.category, costs.category) AS category,
       COALESCE(revenue,0) AS revenue, COALESCE(cost,0) AS cost,
       COALESCE(revenue,0) - COALESCE(cost,0) AS net_profit
FROM ord
FULL JOIN costs ON ord.category = costs.category AND ord.region_id = costs.region_id
JOIN regions reg ON COALESCE(ord.region_id, costs.region_id) = reg.region_id
ORDER BY reg.region_name, net_profit DESC;

-- Explanation:
-- Uses FULL JOIN in case receipts exist without recent orders (or vice versa).
-- Performance: indexes on orders(order_date, product_id, warehouse_id), receipts(received_date, warehouse_id, product_id).

--------------------------------------------------------------------------------
/* 7) Warehouse efficiency: revenue per ton per month
   revenue per ton = sum(total_amount) / (sum(weight_kg * quantity)/1000)
*/
-- Solution
WITH monthly AS (
  SELECT w.warehouse_id, w.code, date_trunc('month', o.order_date) AS mth,
         SUM(o.total_amount) AS revenue,
         SUM((p.weight_kg::numeric * o.quantity)) AS kg_shipped
  FROM orders o
  JOIN warehouses w ON o.warehouse_id = w.warehouse_id
  JOIN products p ON o.product_id = p.product_id
  WHERE o.order_date >= now() - interval '12 months' AND o.status IN ('SHIPPED','COMPLETED')
  GROUP BY w.warehouse_id, w.code, mth
)
SELECT warehouse_id, code, mth, revenue,
       ROUND((revenue / NULLIF((kg_shipped/1000.0),0))::numeric,2) AS revenue_per_ton
FROM monthly
ORDER BY revenue_per_ton DESC NULLS LAST
LIMIT 50;

-- Explanation:
-- Aggregates revenue and mass shipped per month. NULLIF prevents division-by-zero.
-- Performance: index orders(order_date, warehouse_id), products(product_id, weight_kg)

--------------------------------------------------------------------------------
/* 8) Quarterly order trend per region (growth % vs previous quarter)
*/
-- Solution
WITH q AS (
  SELECT r.region_name, date_trunc('quarter', o.order_date) AS qtr, SUM(o.total_amount) AS revenue
  FROM orders o
  JOIN warehouses w ON o.warehouse_id = w.warehouse_id
  JOIN regions r ON w.region_id = r.region_id
  WHERE o.status IN ('COMPLETED','SHIPPED')
  GROUP BY r.region_name, qtr
)
SELECT region_name, qtr, revenue,
       ROUND( (revenue - LAG(revenue) OVER (PARTITION BY region_name ORDER BY qtr)) 
              / NULLIF(LAG(revenue) OVER (PARTITION BY region_name ORDER BY qtr),0) * 100, 2) AS growth_pct
FROM q
ORDER BY region_name, qtr;

-- Explanation:
-- Standard use of LAG to compute period-over-period growth.
-- Performance: partitioned/clustered materialized view possible for heavy workloads.

--------------------------------------------------------------------------------
/* 9) Aggregate summary report using ROLLUP: (region, warehouse, month)
   Produce orders_count, shipments_count, end_inventory (sum stock_qty)
*/
-- Solution
SELECT COALESCE(reg.region_name,'ALL_REGIONS') AS region_name,
       COALESCE(w.code,'ALL_WAREHOUSES') AS warehouse_code,
       COALESCE(date_trunc('month', o.order_date)::date, date_trunc('month', now())::date) AS month,
       COUNT(DISTINCT o.order_id) FILTER (WHERE o.order_id IS NOT NULL) AS orders_count,
       COUNT(DISTINCT s.shipment_id) FILTER (WHERE s.shipment_id IS NOT NULL) AS shipments_count,
       SUM(i.stock_qty) AS end_inventory
FROM warehouses w
LEFT JOIN regions reg ON w.region_id = reg.region_id
LEFT JOIN orders o ON o.warehouse_id = w.warehouse_id AND o.order_date >= now() - interval '12 months'
LEFT JOIN shipments s ON (s.dest_warehouse = w.warehouse_id OR s.source_warehouse = w.warehouse_id) AND s.ship_date >= now() - interval '12 months'
LEFT JOIN inventory i ON i.warehouse_id = w.warehouse_id
GROUP BY ROLLUP (reg.region_name, w.code, month)
ORDER BY region_name NULLS FIRST, warehouse_code NULLS FIRST, month;

-- Explanation:
-- ROLLUP creates subtotals; for large data consider pre-aggregated tables. LEFT JOINs ensure warehouses with no recent activity are shown.
-- Performance: heavy - use materialized view for 12-month aggregates.

--------------------------------------------------------------------------------
/* 10) Partitioning strategy suggestion example (create monthly partitions for shipments)
   We'll show example DDL for range partitioning by ship_date (Postgres >=10)
*/
-- Example (do not run if table already exists)
-- CREATE TABLE shipments_prt (
--   LIKE shipments INCLUDING ALL
-- ) PARTITION BY RANGE (ship_date);
-- CREATE TABLE shipments_2025_10 PARTITION OF shipments_prt FOR VALUES FROM ('2025-10-01') TO ('2025-11-01');
-- CREATE TABLE shipments_2025_11 PARTITION OF shipments_prt FOR VALUES FROM ('2025-11-01') TO ('2025-12-01');

-- Explanation:
-- Partitioning reduces scan ranges for time-bounded queries. Choose RANGE by ship_date or LIST by region if queries are region-scoped.
-- Consider using pg_pathman or native partitioning depending on PG version.

--------------------------------------------------------------------------------
/* 11) Index suggestions: partial index example for in-transit shipments
*/
-- Partial index to speed up common filter
CREATE INDEX IF NOT EXISTS idx_ship_in_transit_src_dest ON shipments (source_warehouse, dest_warehouse, ship_date) WHERE status = 'IN_TRANSIT';

-- Other suggested indexes
CREATE INDEX IF NOT EXISTS idx_orders_order_date_status ON orders (order_date) WHERE status IN ('SHIPPED','COMPLETED');
CREATE INDEX IF NOT EXISTS idx_receipts_received_date ON receipts (received_date);
CREATE INDEX IF NOT EXISTS idx_inventory_product_warehouse ON inventory (product_id, warehouse_id);

-- Run: ANALYZE; then test EXPLAIN ANALYZE before/after index creation.

--------------------------------------------------------------------------------
/* 12) Materialized view example (pre-aggregated monthly shipments by region pair)
*/
CREATE MATERIALIZED VIEW IF NOT EXISTS mv_monthly_shipments AS
SELECT date_trunc('month', sh.ship_date)::date AS month,
       sr.region_name AS source_region,
       dr.region_name AS dest_region,
       SUM(sh.shipped_qty) AS total_shipped,
       COUNT(*) AS shipments_count
FROM shipments sh
JOIN warehouses sw ON sh.source_warehouse = sw.warehouse_id
JOIN warehouses dw ON sh.dest_warehouse = dw.warehouse_id
JOIN regions sr ON sw.region_id = sr.region_id
JOIN regions dr ON dw.region_id = dr.region_id
GROUP BY 1,2,3
WITH NO DATA;

-- To populate: REFRESH MATERIALIZED VIEW CONCURRENTLY mv_monthly_shipments;

--------------------------------------------------------------------------------
/* 13) EXPLAIN ANALYZE recommended workflow - example heavy query and next steps
   1. Run EXPLAIN ANALYZE <query>
   2. Inspect where seq scans or large sorts happen
   3. Add targeted indexes or partitioning
   4. Re-run EXPLAIN ANALYZE and compare total time & plan
*/
-- Example EXPLAIN (do in psql against your DB)
-- EXPLAIN ANALYZE
-- SELECT dr.region_name AS dest_region, COUNT(*) AS delayed_shipments
-- FROM shipments sh
-- JOIN warehouses dw ON sh.dest_warehouse = dw.warehouse_id
-- JOIN regions dr ON dw.region_id = dr.region_id
-- WHERE sh.arrival_date IS NOT NULL
--   AND sh.arrival_date > sh.ship_date + interval '2 days'
--   AND sh.ship_date >= now() - interval '12 months'
-- GROUP BY dr.region_name
-- ORDER BY delayed_shipments DESC;

-- After plan review, add indexes like:
-- CREATE INDEX idx_ship_arrival_ship_date ON shipments (ship_date, arrival_date) WHERE arrival_date IS NOT NULL;
-- Then run EXPLAIN ANALYZE again.

-- End of file
