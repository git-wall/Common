
-- logistics_queries_senior.sql
-- 13 Senior-level SQL challenge queries for the Logistics dataset (PostgreSQL)
-- Each challenge includes a template / hint to get you started.

-- 1) In-transit volume between source region and destination region (last 90 days)

-- Hint: join shipments -> warehouses -> regions, filter status='IN_TRANSIT', GROUP BY source_region, dest_region
-- Example:
-- WITH s AS (
--   SELECT sh.shipment_id, sh.product_id, sh.shipped_qty,
--          sr.region_name AS source_region,
--          dr.region_name AS dest_region,
--          sh.ship_date
--   FROM shipments sh
--   JOIN warehouses sw ON sh.source_warehouse = sw.warehouse_id
--   JOIN warehouses dw ON sh.dest_warehouse = dw.warehouse_id
--   JOIN regions sr ON sw.region_id = sr.region_id
--   JOIN regions dr ON dw.region_id = dr.region_id
--   WHERE sh.status = 'IN_TRANSIT' AND sh.ship_date >= now() - interval '90 days'
-- )
-- SELECT source_region, dest_region, SUM(shipped_qty) AS total_in_transit
-- FROM s
-- GROUP BY source_region, dest_region
-- ORDER BY total_in_transit DESC;

-- 2) Top 5 fastest inventory turnover products in last 3 months
-- Hint: turnover = total shipped out / avg inventory;
-- consider shipments + orders to compute 'outflow'
-- (complex — use window functions and aggregates)

-- 3) Warehouses at risk of congestion (current_stock / capacity > 0.9 AND few active orders)
-- Hint: join warehouses with orders filtered by status NOT IN ('CANCELLED','COMPLETED') in last 30 days.

-- 4) Shipments delayed > 2 days (arrival_date - ship_date > 2) grouped by destination region

-- 5) Average lead time per supplier (from receipts dates) — compute avg(received_date - inferred order date) if order date exists.
-- (Note: receipts table only contains received_date; this challenge can ask to approximate lead time by looking at last_restock_date in inventory or sequence of receipts per supplier)

-- 6) Net profit by region and product category for last 6 months
-- Hint: revenue from orders (total_amount) minus cost from receipts (received_qty * cost_per_unit) joined by product and region.

-- 7) Warehouse efficiency: revenue per ton per month
-- Hint: sum(total_amount) grouped by warehouse and month divided by (sum(weight_kg * quantity) / 1000) -> tons

-- 8) Quarterly order trend per region (use date_trunc('quarter', order_date))
-- Hint: produce growth % vs previous quarter using LAG()

-- 9) Aggregate summary report using ROLLUP: (region, warehouse, month) -> orders_count, shipments_count, end_inventory

-- 10) Partitioning strategy suggestion (no SQL) — implement partitioned table example:
-- CREATE TABLE shipments_y2025m10 PARTITION OF shipments FOR VALUES FROM ('2025-10-01') TO ('2025-11-01');

-- 11) Index suggestions: create partial index for in-transit shipments:
-- CREATE INDEX idx_ship_in_transit_src_dest ON shipments (source_warehouse, dest_warehouse) WHERE status = 'IN_TRANSIT';

-- 12) Materialized view example (pre-aggregated monthly shipments by region)
-- CREATE MATERIALIZED VIEW mv_monthly_shipments AS
-- SELECT date_trunc('month', ship_date) AS mth,
--        sr.region_name AS source_region, dr.region_name AS dest_region, SUM(shipped_qty) AS total_shipped
-- FROM shipments sh
-- JOIN warehouses sw ON sh.source_warehouse = sw.warehouse_id
-- JOIN warehouses dw ON sh.dest_warehouse = dw.warehouse_id
-- JOIN regions sr ON sw.region_id = sr.region_id
-- JOIN regions dr ON dw.region_id = dr.region_id
-- GROUP BY 1,2,3;

-- 13) Use EXPLAIN ANALYZE on any heavy query above and provide before/after plans after adding suggested indexes or partitioning.

-- End of file
