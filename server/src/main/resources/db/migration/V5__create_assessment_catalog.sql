-- Create core catalog and assessment tables to match JPA entities, with auditing columns

-- modules
CREATE TABLE IF NOT EXISTS modules (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  created_by VARCHAR(255),
  updated_by VARCHAR(255),
  is_active BOOLEAN DEFAULT TRUE,
  name VARCHAR(255) NOT NULL,
  description TEXT,
  order_index INT,
  INDEX idx_modules_order (order_index)
) ENGINE=InnoDB;

-- segments
CREATE TABLE IF NOT EXISTS segments (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  created_by VARCHAR(255),
  updated_by VARCHAR(255),
  is_active BOOLEAN DEFAULT TRUE,
  module_id BIGINT NOT NULL,
  name VARCHAR(255) NOT NULL,
  description TEXT,
  order_index INT,
  CONSTRAINT fk_segments_module FOREIGN KEY (module_id) REFERENCES modules(id) ON DELETE CASCADE,
  INDEX idx_segments_module (module_id),
  INDEX idx_segments_order (order_index)
) ENGINE=InnoDB;

-- questionnaires
CREATE TABLE IF NOT EXISTS questionnaires (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  created_by VARCHAR(255),
  updated_by VARCHAR(255),
  is_active BOOLEAN DEFAULT TRUE,
  name VARCHAR(255) NOT NULL,
  version VARCHAR(100) NOT NULL
) ENGINE=InnoDB;

-- assessment_questions
CREATE TABLE IF NOT EXISTS assessment_questions (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  created_by VARCHAR(255),
  updated_by VARCHAR(255),
  is_active BOOLEAN DEFAULT TRUE,
  questionnaire_id BIGINT NOT NULL,
  segment_id BIGINT NULL,
  text TEXT NOT NULL,
  weight DOUBLE,
  options_json TEXT,
  type VARCHAR(50),
  order_index INT,
  CONSTRAINT fk_q_questionnaire FOREIGN KEY (questionnaire_id) REFERENCES questionnaires(id) ON DELETE CASCADE,
  CONSTRAINT fk_q_segment FOREIGN KEY (segment_id) REFERENCES segments(id) ON DELETE SET NULL,
  INDEX idx_questions_segment (segment_id),
  INDEX idx_questions_order (order_index)
) ENGINE=InnoDB;

-- question_options
CREATE TABLE IF NOT EXISTS question_options (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  created_by VARCHAR(255),
  updated_by VARCHAR(255),
  is_active BOOLEAN DEFAULT TRUE,
  question_id BIGINT NOT NULL,
  label VARCHAR(255) NOT NULL,
  value VARCHAR(255),
  order_index INT,
  CONSTRAINT fk_opt_question FOREIGN KEY (question_id) REFERENCES assessment_questions(id) ON DELETE CASCADE,
  INDEX idx_options_question (question_id),
  INDEX idx_options_order (order_index)
) ENGINE=InnoDB;

-- Add auditing columns to legacy 'questions' table if it exists (for compatibility)
ALTER TABLE questions ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NULL;
ALTER TABLE questions ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NULL;
ALTER TABLE questions ADD COLUMN IF NOT EXISTS created_by VARCHAR(255);
ALTER TABLE questions ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);
ALTER TABLE questions ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE;
