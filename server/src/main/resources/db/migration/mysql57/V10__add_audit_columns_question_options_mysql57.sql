-- V10__add_audit_columns_question_options_mysql57.sql (MySQL-only)
-- Ensure audit columns exist on question_options for MySQL 5.7/8 using conditional ALTERs.
-- Idempotent and safe on existing data.

SET @db := DATABASE();

-- question_options.created_at (NOT NULL with default to satisfy entity validation)
SET @sql := IF ((SELECT COUNT(*) FROM information_schema.COLUMNS 
                 WHERE TABLE_SCHEMA=@db AND TABLE_NAME='question_options' AND COLUMN_NAME='created_at') = 0,
                'ALTER TABLE question_options ADD COLUMN created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)',
                'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- question_options.updated_at
SET @sql := IF ((SELECT COUNT(*) FROM information_schema.COLUMNS 
                 WHERE TABLE_SCHEMA=@db AND TABLE_NAME='question_options' AND COLUMN_NAME='updated_at') = 0,
                'ALTER TABLE question_options ADD COLUMN updated_at DATETIME(6) NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(6)',
                'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- question_options.created_by
SET @sql := IF ((SELECT COUNT(*) FROM information_schema.COLUMNS 
                 WHERE TABLE_SCHEMA=@db AND TABLE_NAME='question_options' AND COLUMN_NAME='created_by') = 0,
                'ALTER TABLE question_options ADD COLUMN created_by VARCHAR(255) NULL',
                'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- question_options.updated_by
SET @sql := IF ((SELECT COUNT(*) FROM information_schema.COLUMNS 
                 WHERE TABLE_SCHEMA=@db AND TABLE_NAME='question_options' AND COLUMN_NAME='updated_by') = 0,
                'ALTER TABLE question_options ADD COLUMN updated_by VARCHAR(255) NULL',
                'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- question_options.is_active
SET @sql := IF ((SELECT COUNT(*) FROM information_schema.COLUMNS 
                 WHERE TABLE_SCHEMA=@db AND TABLE_NAME='question_options' AND COLUMN_NAME='is_active') = 0,
                'ALTER TABLE question_options ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE',
                'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Backfill only where needed (non-destructive)
UPDATE question_options 
SET created_at = IFNULL(created_at, CURRENT_TIMESTAMP(6)),
    updated_at = IFNULL(updated_at, CURRENT_TIMESTAMP(6)),
    is_active  = IFNULL(is_active, TRUE);
