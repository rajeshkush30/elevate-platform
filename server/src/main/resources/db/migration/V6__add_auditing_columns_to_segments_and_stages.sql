-- Add missing auditing columns to segments and stages to align with BaseEntity mapping
-- This migration is safe to run on databases where tables already exist without these columns.

-- Segments auditing columns
ALTER TABLE segments
  ADD COLUMN IF NOT EXISTS created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP(6) NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(6),
  ADD COLUMN IF NOT EXISTS created_by VARCHAR(255) NULL,
  ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255) NULL,
  ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT TRUE;

-- Stages auditing columns (prevent future runtime errors if missing)
ALTER TABLE stages
  ADD COLUMN IF NOT EXISTS created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP(6) NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(6),
  ADD COLUMN IF NOT EXISTS created_by VARCHAR(255) NULL,
  ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255) NULL,
  ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT TRUE;
