CREATE TABLE account_features (
  account_id TEXT PRIMARY KEY,
  entry_csn TEXT NOT NULL,
  features JSONB NOT NULL,
  synced_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
