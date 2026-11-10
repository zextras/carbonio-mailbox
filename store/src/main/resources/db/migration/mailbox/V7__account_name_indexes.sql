CREATE INDEX account_attributes_delivery_address
  ON account_attributes USING GIN ((attributes -> 'zimbraMailDeliveryAddress') jsonb_path_ops);

CREATE INDEX account_attributes_mail_alias
  ON account_attributes USING GIN ((attributes -> 'zimbraMailAlias') jsonb_path_ops);

CREATE INDEX account_attributes_old_mail_address
  ON account_attributes USING GIN ((attributes -> 'zimbraOldMailAddress') jsonb_path_ops);
