-- V15: Update client_assessments status column to use ENUM type
ALTER TABLE client_assessments 
MODIFY COLUMN status ENUM('ASSIGNED', 'IN_PROGRESS', 'SUBMITTED', 'SCORED') NOT NULL DEFAULT 'ASSIGNED' 
COMMENT 'Assessment status: ASSIGNED, IN_PROGRESS, SUBMITTED, SCORED';
