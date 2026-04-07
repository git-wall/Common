-- PostgreSQL initialization script

-- Create database
CREATE DATABASE app_db;

-- Connect to the database
\c app_db

-- Create user
CREATE USER app_user WITH PASSWORD 'app_password';

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE app_db TO app_user;
ALTER DATABASE app_db OWNER TO app_user;

-- Create extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_stat_statements";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Set some recommended PostgreSQL configurations
ALTER SYSTEM SET max_connections = '100';
ALTER SYSTEM SET shared_buffers = '128MB';
ALTER SYSTEM SET effective_cache_size = '512MB';
ALTER SYSTEM SET work_mem = '4MB';
ALTER SYSTEM SET maintenance_work_mem = '64MB';
ALTER SYSTEM SET random_page_cost = '1.1';
ALTER SYSTEM SET effective_io_concurrency = '200';
ALTER SYSTEM SET wal_buffers = '16MB';
ALTER SYSTEM SET default_statistics_target = '100';
ALTER SYSTEM SET checkpoint_completion_target = '0.9';
ALTER SYSTEM SET autovacuum = 'on';

-- Create a function for updating timestamps
CREATE OR REPLACE FUNCTION update_modified_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Example of creating a trigger for timestamp updates
-- This would be applied to tables after they are created
-- CREATE TRIGGER update_customer_modtime BEFORE UPDATE ON customers
--    FOR EACH ROW EXECUTE FUNCTION update_modified_column();