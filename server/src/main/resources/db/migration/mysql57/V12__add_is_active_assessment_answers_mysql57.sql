-- V12: Add missing is_active to assessment_answers for MySQL 5.7/8
-- Conditional ALTER so it is safe to run repeatedly.

SET @db := DATABASE();

SET @sql := IF ((SELECT COUNT(*) FROM information_schema.COLUMNS 
                 WHERE TABLE_SCHEMA=@db AND TABLE_NAME='assessment_answers' AND COLUMN_NAME='is_active') = 0,
                'ALTER TABLE assessment_answers ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE',
                'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
