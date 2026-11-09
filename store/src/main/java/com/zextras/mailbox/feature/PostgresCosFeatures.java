package com.zextras.mailbox.feature;

import static com.zextras.mailbox.feature.FeaturesJson.fromJson;
import static com.zextras.mailbox.feature.FeaturesJson.toJson;
import static org.jooq.impl.DSL.currentOffsetDateTime;
import static org.jooq.impl.DSL.excluded;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.name;
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

public class PostgresCosFeatures {

  static final Table<Record> COS_FEATURES =
      table(name(MailboxSchemaMigration.SCHEMA, "cos_features"));
  static final Field<String> COS_ID = field(name("cos_features", "cos_id"), SQLDataType.VARCHAR);
  static final Field<String> NAME = field(name("cos_features", "name"), SQLDataType.VARCHAR);
  static final Field<String> ENTRY_CSN =
      field(name("cos_features", "entry_csn"), SQLDataType.VARCHAR);
  static final Field<JSONB> FEATURES = field(name("cos_features", "features"), SQLDataType.JSONB);
  static final Field<OffsetDateTime> SYNCED_AT =
      field(name("cos_features", "synced_at"), SQLDataType.TIMESTAMPWITHTIMEZONE);

  private final DSLContext db;

  public PostgresCosFeatures(DSLContext db) {
    this.db = db;
  }

  public void save(CosFeatures cos) {
    db.insertInto(COS_FEATURES)
        .set(COS_ID, cos.cosId())
        .set(NAME, cos.name())
        .set(ENTRY_CSN, cos.entryCsn())
        .set(FEATURES, toJson(cos.features()))
        .onConflict(COS_ID)
        .doUpdate()
        .set(NAME, excluded(NAME))
        .set(ENTRY_CSN, excluded(ENTRY_CSN))
        .set(FEATURES, excluded(FEATURES))
        .set(SYNCED_AT, currentOffsetDateTime())
        .where(ENTRY_CSN.lt(excluded(ENTRY_CSN)))
        .execute();
  }

  public void delete(String cosId) {
    db.deleteFrom(COS_FEATURES).where(COS_ID.eq(cosId)).execute();
  }

  public List<String> idsSyncedBefore(OffsetDateTime start) {
    return db.select(COS_ID).from(COS_FEATURES).where(SYNCED_AT.lt(start)).fetch(COS_ID);
  }

  public Optional<CosFeatures> find(String cosId) {
    return db.select(COS_ID, NAME, ENTRY_CSN, FEATURES)
        .from(COS_FEATURES)
        .where(COS_ID.eq(cosId))
        .fetchOptional(
            row ->
                new CosFeatures(
                    row.get(COS_ID),
                    row.get(NAME),
                    row.get(ENTRY_CSN),
                    fromJson(row.get(FEATURES))));
  }
}
