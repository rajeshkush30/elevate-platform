-- V9__backfill_audit_columns_segments_stages_mysql57.sql (MySQL-only)
-- Ensure audit columns exist on segments and stages for MySQL 5.7/8 using conditional ALTERs.
-- This migration is idempotent and safe to run multiple times.

-- Select the current schema
SET @db := DATABASE();

-- ===================== SEGMENTS =====================

-- segments.created_at
SET @sql := IF ((SELECT COUNT(*) FROM information_schema.COLUMNS 
                 WHERE TABLE_SCHEMA=@db AND TABLE_NAME='segments' AND COLUMN_NAME='created_at') = 0,
                'ALTER TABLE segments ADD COLUMN created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)',
                'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- segments.updated_at
SET @sql := IF ((SELECT COUNT(*) FROM information_schema.COLUMNS 
                 WHERE TABLE_SCHEMA=@db AND TABLE_NAME='segments' AND COLUMN_NAME='updated_at') = 0,
                'ALTER TABLE segments ADD COLUMN updated_at DATETIME(6) NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(6)',
                'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- segments.created_by
SET @sql := IF ((SELECT COUNT(*) FROM information_schema.COLUMNS 
                 WHERE TABLE_SCHEMA=@db AND TABLE_NAME='segments' AND COLUMN_NAME='created_by') = 0,
                'ALTER TABLE segments ADD COLUMN created_by VARCHAR(255) NULL',
                'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- segments.updated_by
SET @sql := IF ((SELECT COUNT(*) FROM information_schema.COLUMNS 
                 WHERE TABLE_SCHEMA=@db AND TABLE_NAME='segments' AND COLUMN_NAME='updated_by') = 0,
                'ALTER TABLE segments ADD COLUMN updated_by VARCHAR(255) NULL',
                'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- segments.is_active
SET @sql := IF ((SELECT COUNT(*) FROM information_schema.COLUMNS 
                 WHERE TABLE_SCHEMA=@db AND TABLE_NAME='segments' AND COLUMN_NAME='is_active') = 0,
                'ALTER TABLE segments ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE',
                'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ===================== STAGES =====================

-- stages.created_at
SET @sql := IF ((SELECT COUNT(*) FROM information_schema.COLUMNS 
                 WHERE TABLE_SCHEMA=@db AND TABLE_NAME='stages' AND COLUMN_NAME='created_at') = 0,
                'ALTER TABLE stages ADD COLUMN created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)',
                'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- stages.updated_at
SET @sql := IF ((SELECT COUNT(*) FROM information_schema.COLUMNS 
                 WHERE TABLE_SCHEMA=@db AND TABLE_NAME='stages' AND COLUMN_NAME='updated_at') = 0,
                'ALTER TABLE stages ADD COLUMN updated_at DATETIME(6) NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(6)',
                'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- stages.created_by
SET @sql := IF ((SELECT COUNT(*) FROM information_schema.COLUMNS 
                 WHERE TABLE_SCHEMA=@db AND TABLE_NAME='stages' AND COLUMN_NAME='created_by') = 0,
                'ALTER TABLE stages ADD COLUMN created_by VARCHAR(255) NULL',
                'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- stages.updated_by
SET @sql := IF ((SELECT COUNT(*) FROM information_schema.COLUMNS 
                 WHERE TABLE_SCHEMA=@db AND TABLE_NAME='stages' AND COLUMN_NAME='updated_by') = 0,
                'ALTER TABLE stages ADD COLUMN updated_by VARCHAR(255) NULL',
                'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- stages.is_active
SET @sql := IF ((SELECT COUNT(*) FROM information_schema.COLUMNS 
                 WHERE TABLE_SCHEMA=@db AND TABLE_NAME='stages' AND COLUMN_NAME='is_active') = 0,
                'ALTER TABLE stages ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE',
                'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
