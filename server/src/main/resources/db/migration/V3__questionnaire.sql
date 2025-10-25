-- Flyway migration: create questions and questionnaire_submissions tables

CREATE TABLE IF NOT EXISTS questions (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  text TEXT NOT NULL,
  options TEXT,
  weight INT
);

CREATE TABLE IF NOT EXISTS questionnaire_submissions (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT,
  answers_json TEXT,
  score INT,
  stage VARCHAR(100),
  summary TEXT,
  created_at TIMESTAMP
);
