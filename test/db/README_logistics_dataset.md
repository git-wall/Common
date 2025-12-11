
Logistics & Supply Chain Analytics Dataset (PostgreSQL)
Generated on: 2025-10-14 12:00:00

Files:
- logistics_dataset_postgres.sql   : DDL + INSERTs (~2000 insert rows)
- logistics_queries_senior.sql     : Senior-level challenges with templates/hints
- README_logistics_dataset.md      : this file

How to load:
1) In a terminal with PostgreSQL available, run:
   psql -U <your_user> -d <your_db> -f /path/to/logistics_dataset_postgres.sql

2) After import, run some quick checks:
   SELECT count(*) FROM regions;
   SELECT count(*) FROM warehouses;
   SELECT count(*) FROM products;
   SELECT count(*) FROM orders;
   SELECT count(*) FROM shipments;

Performance tips included in queries file:
- Create indexes on frequently filtered columns:
  * shipments(status), shipments(ship_date), shipments(source_warehouse), shipments(dest_warehouse)
  * orders(order_date), orders(warehouse_id), orders(status)
- Consider RANGE partitioning on ship_date / order_date by month if data grows large.
- Use MATERIALIZED VIEWs for heavy aggregations refreshed nightly.

Notes:
- Data is randomly generated but structured realistically.
- Timestamps fixed relative to 2025-10-14 for reproducibility.

