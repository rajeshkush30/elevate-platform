-- V13: Add nullable questionnaire_id to assessments and FK to questionnaires (MySQL 5.7 Compatible)

-- Add column only if missing
ALTER TABLE assessments
  ADD COLUMN questionnaire_id BIGINT NULL;

-- Add index only if missing
ALTER TABLE assessments
  ADD INDEX idx_assessments_questionnaire (questionnaire_id);

-- Add FK only if both tables exist
ALTER TABLE assessments
  ADD CONSTRAINT fk_assessments_questionnaire
  FOREIGN KEY (questionnaire_id) REFERENCES questionnaires(id);
