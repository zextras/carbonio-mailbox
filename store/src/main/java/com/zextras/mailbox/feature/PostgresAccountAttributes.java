package com.zextras.mailbox.feature;

import static com.zextras.mailbox.feature.AttributesJson.fromJson;
import static com.zextras.mailbox.feature.AttributesJson.toJson;
import static org.jooq.impl.DSL.currentOffsetDateTime;
import static org.jooq.impl.DSL.excluded;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.name;
import static org.jooq.impl.DSL.row;
import static org.jooq.impl.DSL.table;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.JSONB;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.impl.SQLDataType;

public class PostgresAccountAttributes {

  static final Table<Record> ACCOUNT_ATTRIBUTES =
      table(name(MailboxSchemaMigration.SCHEMA, "account_attributes"));
  static final Field<String> ACCOUNT_ID =
      field(name("account_attributes", "account_id"), SQLDataType.VARCHAR);
  static final Field<String> DOMAIN_NAME =
      field(name("account_attributes", "domain_name"), SQLDataType.VARCHAR);
  static final Field<String> COS_ID =
      field(name("account_attributes", "cos_id"), SQLDataType.VARCHAR);
  static final Field<String> ENTRY_CSN =
      field(name("account_attributes", "entry_csn"), SQLDataType.VARCHAR);
  static final Field<JSONB> ATTRIBUTES =
      field(name("account_attributes", "attributes"), SQLDataType.JSONB);
  static final Field<OffsetDateTime> SYNCED_AT =
      field(name("account_attributes", "synced_at"), SQLDataType.TIMESTAMPWITHTIMEZONE);

  private final DSLContext db;

  public PostgresAccountAttributes(DSLContext db) {
    this.db = db;
  }

  public void save(AccountAttributes account) {
    db.insertInto(ACCOUNT_ATTRIBUTES)
        .set(ACCOUNT_ID, account.accountId())
        .set(DOMAIN_NAME, account.domainName())
        .set(COS_ID, account.cosId())
        .set(ENTRY_CSN, account.entryCsn())
        .set(ATTRIBUTES, toJson(account.attributes()))
        .onConflict(ACCOUNT_ID)
        .doUpdate()
        .set(DOMAIN_NAME, excluded(DOMAIN_NAME))
        .set(COS_ID, excluded(COS_ID))
        .set(ENTRY_CSN, excluded(ENTRY_CSN))
        .set(ATTRIBUTES, excluded(ATTRIBUTES))
        .set(SYNCED_AT, currentOffsetDateTime())
        .where(
            ENTRY_CSN
                .lt(excluded(ENTRY_CSN))
                .or(
                    ENTRY_CSN
                        .eq(excluded(ENTRY_CSN))
                        .and(
                            row(DOMAIN_NAME, COS_ID, ATTRIBUTES)
                                .isDistinctFrom(
                                    row(
                                        excluded(DOMAIN_NAME),
                                        excluded(COS_ID),
                                        excluded(ATTRIBUTES))))))
        .execute();
  }

  public void delete(String accountId) {
    db.deleteFrom(ACCOUNT_ATTRIBUTES).where(ACCOUNT_ID.eq(accountId)).execute();
  }

  public List<String> idsSyncedBefore(OffsetDateTime start) {
    return db.select(ACCOUNT_ID).from(ACCOUNT_ATTRIBUTES).where(SYNCED_AT.lt(start)).fetch(ACCOUNT_ID);
  }

  public Optional<AccountAttributes> find(String accountId) {
    return db.select(ACCOUNT_ID, DOMAIN_NAME, COS_ID, ENTRY_CSN, ATTRIBUTES)
        .from(ACCOUNT_ATTRIBUTES)
        .where(ACCOUNT_ID.eq(accountId))
        .fetchOptional(
            row ->
                new AccountAttributes(
                    row.get(ACCOUNT_ID),
                    row.get(DOMAIN_NAME),
                    row.get(COS_ID),
                    row.get(ENTRY_CSN),
                    fromJson(row.get(ATTRIBUTES))));
  }
}
