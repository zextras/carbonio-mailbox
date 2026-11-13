ALTER TABLE account_features ADD COLUMN domain_name TEXT;

CREATE INDEX account_features_domain_name ON account_features (domain_name);

ALTER TABLE cos_features ADD COLUMN name TEXT;

CREATE INDEX cos_features_name ON cos_features (name);

CREATE TABLE domain_default_cos (
  domain_name TEXT PRIMARY KEY,
  default_cos_id TEXT,
  entry_csn TEXT NOT NULL,
  synced_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
