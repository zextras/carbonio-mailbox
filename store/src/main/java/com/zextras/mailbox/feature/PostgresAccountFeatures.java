package com.zextras.mailbox.feature;

import static com.zextras.mailbox.feature.FeaturesJson.fromJson;
import static com.zextras.mailbox.feature.FeaturesJson.toJson;
import static org.jooq.impl.DSL.currentOffsetDateTime;
import static org.jooq.impl.DSL.excluded;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.name;
import static org.jooq.impl.DSL.table;

import java.time.OffsetDateTime;
import java.util.Optional;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.JSONB;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.impl.SQLDataType;

public class PostgresAccountFeatures {

  static final Table<Record> ACCOUNT_FEATURES =
      table(name(MailboxSchemaMigration.SCHEMA, "account_features"));
  static final Field<String> ACCOUNT_ID =
      field(name("account_features", "account_id"), SQLDataType.VARCHAR);
  static final Field<String> COS_ID =
      field(name("account_features", "cos_id"), SQLDataType.VARCHAR);
  static final Field<String> ENTRY_CSN =
      field(name("account_features", "entry_csn"), SQLDataType.VARCHAR);
  static final Field<JSONB> FEATURES =
      field(name("account_features", "features"), SQLDataType.JSONB);
  static final Field<OffsetDateTime> SYNCED_AT =
      field(name("account_features", "synced_at"), SQLDataType.TIMESTAMPWITHTIMEZONE);

  private final DSLContext db;

  public PostgresAccountFeatures(DSLContext db) {
    this.db = db;
  }

  public void save(AccountFeatures account) {
    db.insertInto(ACCOUNT_FEATURES)
        .set(ACCOUNT_ID, account.accountId())
        .set(COS_ID, account.cosId())
        .set(ENTRY_CSN, account.entryCsn())
        .set(FEATURES, toJson(account.features()))
        .onConflict(ACCOUNT_ID)
        .doUpdate()
        .set(COS_ID, excluded(COS_ID))
        .set(ENTRY_CSN, excluded(ENTRY_CSN))
        .set(FEATURES, excluded(FEATURES))
        .set(SYNCED_AT, currentOffsetDateTime())
        .where(ENTRY_CSN.lt(excluded(ENTRY_CSN)))
        .execute();
  }

  public Optional<AccountFeatures> find(String accountId) {
    return db.select(ACCOUNT_ID, COS_ID, ENTRY_CSN, FEATURES)
        .from(ACCOUNT_FEATURES)
        .where(ACCOUNT_ID.eq(accountId))
        .fetchOptional(
            row ->
                new AccountFeatures(
                    row.get(ACCOUNT_ID),
                    row.get(COS_ID),
                    row.get(ENTRY_CSN),
                    fromJson(row.get(FEATURES))));
  }
}
