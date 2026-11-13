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
import org.jooq.Query;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.impl.SQLDataType;

public class PostgresDomainAttributes {

  static final Table<Record> DOMAIN_ATTRIBUTES =
      table(name(MailboxSchemaMigration.SCHEMA, "domain_attributes"));
  static final Field<String> DOMAIN_NAME =
      field(name("domain_attributes", "domain_name"), SQLDataType.VARCHAR);
  static final Field<String> DN = field(name("domain_attributes", "dn"), SQLDataType.VARCHAR);
  static final Field<String> DEFAULT_COS_ID =
      field(name("domain_attributes", "default_cos_id"), SQLDataType.VARCHAR);
  static final Field<String> ENTRY_CSN =
      field(name("domain_attributes", "entry_csn"), SQLDataType.VARCHAR);
  static final Field<JSONB> ATTRIBUTES =
      field(name("domain_attributes", "attributes"), SQLDataType.JSONB);
  static final Field<OffsetDateTime> SYNCED_AT =
      field(name("domain_attributes", "synced_at"), SQLDataType.TIMESTAMPWITHTIMEZONE);

  private final DSLContext db;

  public PostgresDomainAttributes(DSLContext db) {
    this.db = db;
  }

  public void save(DomainAttributes domain) {
    upsert(domain).execute();
  }

  public void saveAll(List<DomainAttributes> rows) {
    if (!rows.isEmpty()) {
      db.batch(rows.stream().map(this::upsert).toList()).execute();
    }
  }

  private Query upsert(DomainAttributes domain) {
    return db.insertInto(DOMAIN_ATTRIBUTES)
        .set(DOMAIN_NAME, domain.domainName())
        .set(DN, domain.dn())
        .set(DEFAULT_COS_ID, domain.defaultCosId())
        .set(ENTRY_CSN, domain.entryCsn())
        .set(ATTRIBUTES, toJson(domain.attributes()))
        .onConflict(DOMAIN_NAME)
        .doUpdate()
        .set(DN, excluded(DN))
        .set(DEFAULT_COS_ID, excluded(DEFAULT_COS_ID))
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
                            row(DN, DEFAULT_COS_ID, ATTRIBUTES)
                                .isDistinctFrom(
                                    row(
                                        excluded(DN),
                                        excluded(DEFAULT_COS_ID),
                                        excluded(ATTRIBUTES))))));
  }

  public void delete(String domainName) {
    db.deleteFrom(DOMAIN_ATTRIBUTES).where(DOMAIN_NAME.eq(domainName)).execute();
  }

  public List<String> namesSyncedBefore(OffsetDateTime start) {
    return db.select(DOMAIN_NAME)
        .from(DOMAIN_ATTRIBUTES)
        .where(SYNCED_AT.lt(start))
        .fetch(DOMAIN_NAME);
  }

  public Optional<DomainAttributes> find(String domainName) {
    return db.select(DOMAIN_NAME, DN, DEFAULT_COS_ID, ENTRY_CSN, ATTRIBUTES)
        .from(DOMAIN_ATTRIBUTES)
        .where(DOMAIN_NAME.eq(domainName))
        .fetchOptional(
            row ->
                new DomainAttributes(
                    row.get(DOMAIN_NAME),
                    row.get(DN),
                    row.get(DEFAULT_COS_ID),
                    row.get(ENTRY_CSN),
                    fromJson(row.get(ATTRIBUTES))));
  }
}
