-- V8__add_missing_audit_columns.sql
-- This migration safely adds any missing audit columns to the segments table

-- Create a stored procedure to safely add columns
DELIMITER //
CREATE PROCEDURE AddColumnIfNotExists(
    IN tableName VARCHAR(100),
    IN columnName VARCHAR(100),
    IN columnDefinition VARCHAR(1000)
)
BEGIN
    DECLARE column_count INT;
    
    -- Check if column exists
    SELECT COUNT(*)
    INTO column_count
    FROM information_schema.COLUMNS
    WHERE 
        TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = tableName
        AND COLUMN_NAME = columnName;
        
    -- Add column if it doesn't exist
    IF column_count = 0 THEN
        SET @sql = CONCAT('ALTER TABLE ', tableName, ' ADD COLUMN ', columnName, ' ', columnDefinition);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END //
DELIMITER ;

-- Add missing columns
CALL AddColumnIfNotExists('segments', 'created_at', 'TIMESTAMP DEFAULT CURRENT_TIMESTAMP');
CALL AddColumnIfNotExists('segments', 'created_by', 'VARCHAR(255)');
CALL AddColumnIfNotExists('segments', 'updated_at', 'TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP');
CALL AddColumnIfNotExists('segments', 'updated_by', 'VARCHAR(255)');

-- Clean up
DROP PROCEDURE IF EXISTS AddColumnIfNotExists;
