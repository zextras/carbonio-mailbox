package com.zextras.mailbox.feature;

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
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.impl.SQLDataType;

public class PostgresDomainDefaultCos {

  static final Table<Record> DOMAIN_DEFAULT_COS =
      table(name(MailboxSchemaMigration.SCHEMA, "domain_default_cos"));
  static final Field<String> DOMAIN_NAME =
      field(name("domain_default_cos", "domain_name"), SQLDataType.VARCHAR);
  static final Field<String> DEFAULT_COS_ID =
      field(name("domain_default_cos", "default_cos_id"), SQLDataType.VARCHAR);
  static final Field<String> ENTRY_CSN =
      field(name("domain_default_cos", "entry_csn"), SQLDataType.VARCHAR);
  static final Field<OffsetDateTime> SYNCED_AT =
      field(name("domain_default_cos", "synced_at"), SQLDataType.TIMESTAMPWITHTIMEZONE);

  private final DSLContext db;

  public PostgresDomainDefaultCos(DSLContext db) {
    this.db = db;
  }

  public void save(DomainDefaultCos domain) {
    db.insertInto(DOMAIN_DEFAULT_COS)
        .set(DOMAIN_NAME, domain.domainName())
        .set(DEFAULT_COS_ID, domain.defaultCosId())
        .set(ENTRY_CSN, domain.entryCsn())
        .onConflict(DOMAIN_NAME)
        .doUpdate()
        .set(DEFAULT_COS_ID, excluded(DEFAULT_COS_ID))
        .set(ENTRY_CSN, excluded(ENTRY_CSN))
        .set(SYNCED_AT, currentOffsetDateTime())
        .where(
            ENTRY_CSN
                .lt(excluded(ENTRY_CSN))
                .or(
                    ENTRY_CSN
                        .eq(excluded(ENTRY_CSN))
                        .and(DEFAULT_COS_ID.isDistinctFrom(excluded(DEFAULT_COS_ID)))))
        .execute();
  }

  public void delete(String domainName) {
    db.deleteFrom(DOMAIN_DEFAULT_COS).where(DOMAIN_NAME.eq(domainName)).execute();
  }

  public List<String> namesSyncedBefore(OffsetDateTime start) {
    return db.select(DOMAIN_NAME)
        .from(DOMAIN_DEFAULT_COS)
        .where(SYNCED_AT.lt(start))
        .fetch(DOMAIN_NAME);
  }

  public Optional<DomainDefaultCos> find(String domainName) {
    return db.select(DOMAIN_NAME, DEFAULT_COS_ID, ENTRY_CSN)
        .from(DOMAIN_DEFAULT_COS)
        .where(DOMAIN_NAME.eq(domainName))
        .fetchOptional(
            row ->
                new DomainDefaultCos(
                    row.get(DOMAIN_NAME), row.get(DEFAULT_COS_ID), row.get(ENTRY_CSN)));
  }
}
