-- Seed sample questionnaire questions
INSERT INTO questions (text, options, weight) VALUES
('How clear is your value proposition to customers?', 'Not clear:0\nSomewhat clear:1\nClear:2', 1),
('How strong is your sales pipeline?', 'Weak:0\nModerate:1\nStrong:2', 1),
('Do you have documented processes and SOPs?', 'No:0\nPartial:1\nYes:2', 1),
('How would you rate your leadership capability?', 'Low:0\nAverage:1\nHigh:2', 1),
('Do you have repeatable customer acquisition channels?', 'No:0\nSome:1\nYes:2', 1)
ON CONFLICT DO NOTHING;
