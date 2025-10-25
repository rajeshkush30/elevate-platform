-- Flyway V11 (MySQL 5.7/8): Assessment schema
-- Same as parent V11, placed under mysql57-specific location so we can scope Flyway to MySQL-safe scripts only.

-- 1) Assessments: template attached to a Stage
CREATE TABLE IF NOT EXISTS assessments (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  stage_id BIGINT NOT NULL,
  name VARCHAR(255) NOT NULL,
  description TEXT NULL,
  is_active BIT(1) NOT NULL DEFAULT b'1',
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at DATETIME(6) NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(6),
  created_by VARCHAR(255) NULL,
  updated_by VARCHAR(255) NULL,
  CONSTRAINT fk_assessments_stage
    FOREIGN KEY (stage_id) REFERENCES stages(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2) Client assignment instance
CREATE TABLE IF NOT EXISTS client_assessments (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  client_id BIGINT NOT NULL,
  assessment_id BIGINT NOT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'ASSIGNED', -- ASSIGNED, IN_PROGRESS, SUBMITTED, SCORED
  due_date DATE NULL,
  started_at DATETIME(6) NULL,
  submitted_at DATETIME(6) NULL,
  score DECIMAL(10,2) NULL,
  is_active BIT(1) NOT NULL DEFAULT b'1',
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at DATETIME(6) NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(6),
  created_by VARCHAR(255) NULL,
  updated_by VARCHAR(255) NULL,
  INDEX idx_client_assessments_client (client_id),
  INDEX idx_client_assessments_assessment (assessment_id),
  CONSTRAINT fk_client_assessment_assessment
    FOREIGN KEY (assessment_id) REFERENCES assessments(id),
  CONSTRAINT fk_client_assessment_user
    FOREIGN KEY (client_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3) Answers (free text/number) per client assessment
CREATE TABLE IF NOT EXISTS assessment_answers (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  client_assessment_id BIGINT NOT NULL,
  question_id BIGINT NOT NULL,
  answer_text TEXT NULL,  -- used for TEXT/NUMBER
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at DATETIME(6) NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(6),
  created_by VARCHAR(255) NULL,
  updated_by VARCHAR(255) NULL,
  UNIQUE KEY uq_client_q (client_assessment_id, question_id),
  INDEX idx_answers_client_assessment (client_assessment_id),
  INDEX idx_answers_question (question_id),
  CONSTRAINT fk_answer_client_assessment
    FOREIGN KEY (client_assessment_id) REFERENCES client_assessments(id),
  CONSTRAINT fk_answer_question
    FOREIGN KEY (question_id) REFERENCES assessment_questions(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4) Selected options for SINGLE/MULTI choice
CREATE TABLE IF NOT EXISTS assessment_answer_options (
  answer_id BIGINT NOT NULL,
  option_id BIGINT NOT NULL,
  PRIMARY KEY (answer_id, option_id),
  INDEX idx_answer_options_answer (answer_id),
  INDEX idx_answer_options_option (option_id),
  CONSTRAINT fk_answer_options_answer
    FOREIGN KEY (answer_id) REFERENCES assessment_answers(id) ON DELETE CASCADE,
  CONSTRAINT fk_answer_options_option
    FOREIGN KEY (option_id) REFERENCES question_options(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
