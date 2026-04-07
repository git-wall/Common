-- MySQL initialization script

-- Create database
CREATE DATABASE IF NOT EXISTS app_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Create user and grant privileges
CREATE USER IF NOT EXISTS 'app_user'@'%' IDENTIFIED BY 'app_password';
GRANT ALL PRIVILEGES ON app_db.* TO 'app_user'@'%';
FLUSH PRIVILEGES;

-- Use the database
USE app_db;

-- Enable performance schema (optional)
SET GLOBAL performance_schema = ON;

-- Set some recommended MySQL configurations
SET GLOBAL max_connections = 150;
SET GLOBAL innodb_buffer_pool_size = 134217728; -- 128MB
SET GLOBAL innodb_log_file_size = 50331648; -- 48MB
SET GLOBAL innodb_flush_log_at_trx_commit = 1; -- ACID compliant
SET GLOBAL sql_mode = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION';

-- Create a stored procedure for cleaning up old data (example)
DELIMITER //
CREATE PROCEDURE IF NOT EXISTS cleanup_old_data()
BEGIN
    -- Delete records older than 1 year
    DELETE FROM audit_logs WHERE created_at < DATE_SUB(NOW(), INTERVAL 1 YEAR);
    
    -- Archive completed orders older than 6 months
    INSERT INTO archived_orders SELECT * FROM orders 
    WHERE status = 'COMPLETED' AND created_at < DATE_SUB(NOW(), INTERVAL 6 MONTH);
    
    DELETE FROM orders 
    WHERE status = 'COMPLETED' AND created_at < DATE_SUB(NOW(), INTERVAL 6 MONTH);
END //
DELIMITER ;

-- Create an event to run the cleanup procedure monthly
CREATE EVENT IF NOT EXISTS monthly_cleanup
ON SCHEDULE EVERY 1 MONTH
DO
    CALL cleanup_old_data();