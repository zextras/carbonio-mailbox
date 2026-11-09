package com.zextras.mailbox.feature;

import static org.jooq.impl.DSL.currentOffsetDateTime;
import static org.jooq.impl.DSL.excluded;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.name;
import static org.jooq.impl.DSL.table;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.UncheckedIOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.JSONB;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.impl.SQLDataType;

public class PostgresAccountFeatures {

  private static final Table<Record> ACCOUNT_FEATURES =
      table(name(MailboxSchemaMigration.SCHEMA, "account_features"));
  private static final Field<String> ACCOUNT_ID =
      field(name("account_features", "account_id"), SQLDataType.VARCHAR);
  private static final Field<String> ENTRY_CSN =
      field(name("account_features", "entry_csn"), SQLDataType.VARCHAR);
  private static final Field<JSONB> FEATURES =
      field(name("account_features", "features"), SQLDataType.JSONB);
  private static final Field<OffsetDateTime> SYNCED_AT =
      field(name("account_features", "synced_at"), SQLDataType.TIMESTAMPWITHTIMEZONE);

  private static final TypeReference<Map<String, List<String>>> FEATURE_MAP =
      new TypeReference<>() {};

  private final DSLContext db;
  private final ObjectMapper json = new ObjectMapper();

  public PostgresAccountFeatures(DSLContext db) {
    this.db = db;
  }

  public void save(AccountFeatures account) {
    db.insertInto(ACCOUNT_FEATURES)
        .set(ACCOUNT_ID, account.accountId())
        .set(ENTRY_CSN, account.entryCsn())
        .set(FEATURES, toJson(account.features()))
        .onConflict(ACCOUNT_ID)
        .doUpdate()
        .set(ENTRY_CSN, excluded(ENTRY_CSN))
        .set(FEATURES, excluded(FEATURES))
        .set(SYNCED_AT, currentOffsetDateTime())
        .where(ENTRY_CSN.lt(excluded(ENTRY_CSN)))
        .execute();
  }

  public Optional<AccountFeatures> find(String accountId) {
    return db.select(ACCOUNT_ID, ENTRY_CSN, FEATURES)
        .from(ACCOUNT_FEATURES)
        .where(ACCOUNT_ID.eq(accountId))
        .fetchOptional(
            row ->
                new AccountFeatures(
                    row.get(ACCOUNT_ID), row.get(ENTRY_CSN), fromJson(row.get(FEATURES))));
  }

  private JSONB toJson(Map<String, List<String>> features) {
    try {
      return JSONB.valueOf(json.writeValueAsString(features));
    } catch (JsonProcessingException e) {
      throw new UncheckedIOException(e);
    }
  }

  private Map<String, List<String>> fromJson(JSONB features) {
    try {
      return json.readValue(features.data(), FEATURE_MAP);
    } catch (JsonProcessingException e) {
      throw new UncheckedIOException(e);
    }
  }
}
