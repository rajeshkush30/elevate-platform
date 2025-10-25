-- MySQL 5.7-compatible conditional adds for auditing columns on segments and stages
-- Uses information_schema checks and dynamic SQL to avoid duplicate column errors.

-- Helper: current schema
SET @db := DATABASE();

-- ========== segments.created_at ==========
SET @sql := IF ((SELECT COUNT(*) FROM information_schema.COLUMNS 
                 WHERE TABLE_SCHEMA=@db AND TABLE_NAME='segments' AND COLUMN_NAME='created_at') = 0,
                'ALTER TABLE segments ADD COLUMN created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)',
                'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ========== segments.updated_at ==========
SET @sql := IF ((SELECT COUNT(*) FROM information_schema.COLUMNS 
                 WHERE TABLE_SCHEMA=@db AND TABLE_NAME='segments' AND COLUMN_NAME='updated_at') = 0,
                'ALTER TABLE segments ADD COLUMN updated_at DATETIME(6) NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(6)',
                'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ========== segments.created_by ==========
SET @sql := IF ((SELECT COUNT(*) FROM information_schema.COLUMNS 
                 WHERE TABLE_SCHEMA=@db AND TABLE_NAME='segments' AND COLUMN_NAME='created_by') = 0,
                'ALTER TABLE segments ADD COLUMN created_by VARCHAR(255) NULL',
                'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ========== segments.updated_by ==========
SET @sql := IF ((SELECT COUNT(*) FROM information_schema.COLUMNS 
                 WHERE TABLE_SCHEMA=@db AND TABLE_NAME='segments' AND COLUMN_NAME='updated_by') = 0,
                'ALTER TABLE segments ADD COLUMN updated_by VARCHAR(255) NULL',
                'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ========== segments.is_active ==========
SET @sql := IF ((SELECT COUNT(*) FROM information_schema.COLUMNS 
                 WHERE TABLE_SCHEMA=@db AND TABLE_NAME='segments' AND COLUMN_NAME='is_active') = 0,
                'ALTER TABLE segments ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE',
                'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ====================== STAGES ======================

-- ========== stages.created_at ==========
SET @sql := IF ((SELECT COUNT(*) FROM information_schema.COLUMNS 
                 WHERE TABLE_SCHEMA=@db AND TABLE_NAME='stages' AND COLUMN_NAME='created_at') = 0,
                'ALTER TABLE stages ADD COLUMN created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)',
                'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ========== stages.updated_at ==========
SET @sql := IF ((SELECT COUNT(*) FROM information_schema.COLUMNS 
                 WHERE TABLE_SCHEMA=@db AND TABLE_NAME='stages' AND COLUMN_NAME='updated_at') = 0,
                'ALTER TABLE stages ADD COLUMN updated_at DATETIME(6) NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(6)',
                'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ========== stages.created_by ==========
SET @sql := IF ((SELECT COUNT(*) FROM information_schema.COLUMNS 
                 WHERE TABLE_SCHEMA=@db AND TABLE_NAME='stages' AND COLUMN_NAME='created_by') = 0,
                'ALTER TABLE stages ADD COLUMN created_by VARCHAR(255) NULL',
                'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ========== stages.updated_by ==========
SET @sql := IF ((SELECT COUNT(*) FROM information_schema.COLUMNS 
                 WHERE TABLE_SCHEMA=@db AND TABLE_NAME='stages' AND COLUMN_NAME='updated_by') = 0,
                'ALTER TABLE stages ADD COLUMN updated_by VARCHAR(255) NULL',
                'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ========== stages.is_active ==========
SET @sql := IF ((SELECT COUNT(*) FROM information_schema.COLUMNS 
                 WHERE TABLE_SCHEMA=@db AND TABLE_NAME='stages' AND COLUMN_NAME='is_active') = 0,
                'ALTER TABLE stages ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE',
                'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
