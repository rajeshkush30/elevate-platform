CREATE TABLE leads (
  id BIGSERIAL PRIMARY KEY,
  zoho_lead_id TEXT,
  first_name TEXT NOT NULL,
  last_name TEXT,
  email TEXT NOT NULL,
  phone TEXT,
  company TEXT,
  ainosse_score INTEGER,
  status TEXT NOT NULL,
  invite_token TEXT,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE UNIQUE INDEX ux_leads_email ON leads(email);
