ALTER TABLE account_attributes ADD COLUMN dn TEXT;

ALTER TABLE cos_attributes ADD COLUMN dn TEXT;

ALTER TABLE domain_default_cos RENAME TO domain_attributes;

ALTER TABLE domain_attributes ADD COLUMN dn TEXT;

ALTER TABLE domain_attributes ADD COLUMN attributes JSONB NOT NULL DEFAULT '{}';
