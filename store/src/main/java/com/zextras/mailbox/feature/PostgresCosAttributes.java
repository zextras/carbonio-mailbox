package com.zextras.mailbox.feature;

import static com.zextras.mailbox.feature.AttributesJson.fromJson;
import static com.zextras.mailbox.feature.AttributesJson.toJson;
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

public class PostgresCosAttributes {

  static final Table<Record> COS_ATTRIBUTES =
      table(name(MailboxSchemaMigration.SCHEMA, "cos_attributes"));
  static final Field<String> COS_ID = field(name("cos_attributes", "cos_id"), SQLDataType.VARCHAR);
  static final Field<String> NAME = field(name("cos_attributes", "name"), SQLDataType.VARCHAR);
  static final Field<String> ENTRY_CSN =
      field(name("cos_attributes", "entry_csn"), SQLDataType.VARCHAR);
  static final Field<JSONB> ATTRIBUTES = field(name("cos_attributes", "attributes"), SQLDataType.JSONB);
  static final Field<OffsetDateTime> SYNCED_AT =
      field(name("cos_attributes", "synced_at"), SQLDataType.TIMESTAMPWITHTIMEZONE);

  private final DSLContext db;

  public PostgresCosAttributes(DSLContext db) {
    this.db = db;
  }

  public void save(CosAttributes cos) {
    db.insertInto(COS_ATTRIBUTES)
        .set(COS_ID, cos.cosId())
        .set(NAME, cos.name())
        .set(ENTRY_CSN, cos.entryCsn())
        .set(ATTRIBUTES, toJson(cos.attributes()))
        .onConflict(COS_ID)
        .doUpdate()
        .set(NAME, excluded(NAME))
        .set(ENTRY_CSN, excluded(ENTRY_CSN))
        .set(ATTRIBUTES, excluded(ATTRIBUTES))
        .set(SYNCED_AT, currentOffsetDateTime())
        .where(ENTRY_CSN.lt(excluded(ENTRY_CSN)))
        .execute();
  }

  public void delete(String cosId) {
    db.deleteFrom(COS_ATTRIBUTES).where(COS_ID.eq(cosId)).execute();
  }

  public List<String> idsSyncedBefore(OffsetDateTime start) {
    return db.select(COS_ID).from(COS_ATTRIBUTES).where(SYNCED_AT.lt(start)).fetch(COS_ID);
  }

  public Optional<CosAttributes> find(String cosId) {
    return db.select(COS_ID, NAME, ENTRY_CSN, ATTRIBUTES)
        .from(COS_ATTRIBUTES)
        .where(COS_ID.eq(cosId))
        .fetchOptional(
            row ->
                new CosAttributes(
                    row.get(COS_ID),
                    row.get(NAME),
                    row.get(ENTRY_CSN),
                    fromJson(row.get(ATTRIBUTES))));
  }
}
