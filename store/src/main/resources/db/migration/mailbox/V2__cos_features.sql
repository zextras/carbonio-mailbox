ALTER TABLE account_features ADD COLUMN cos_id TEXT;

CREATE INDEX account_features_cos_id ON account_features (cos_id);

CREATE TABLE cos_features (
  cos_id TEXT PRIMARY KEY,
  entry_csn TEXT NOT NULL,
  features JSONB NOT NULL,
  synced_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
