package com.zextras.mailbox.feature;

import static com.zextras.mailbox.feature.AttributesJson.fromJson;

import java.util.Optional;
import org.jooq.DSLContext;
import org.jooq.JSONB;
import org.jooq.Record;

public class PostgresEntries {

  private static final String ACCOUNT_BY_NAME =
      """
      SELECT dn, attributes FROM mailbox.account_attributes
      WHERE attributes -> 'zimbraMailDeliveryAddress' @> jsonb_build_array({0})
         OR attributes -> 'zimbraMailAlias' @> jsonb_build_array({0})
         OR attributes -> 'zimbraOldMailAddress' @> jsonb_build_array({0})
      LIMIT 1
      """;

  private final DSLContext db;

  public PostgresEntries(DSLContext db) {
    this.db = db;
  }

  public Optional<StoredEntry> accountById(String id) {
    return fetch(
        "SELECT dn, attributes FROM mailbox.account_attributes WHERE account_id = {0}", id);
  }

  public Optional<StoredEntry> accountByName(String name) {
    return fetch(ACCOUNT_BY_NAME, name);
  }

  public Optional<StoredEntry> cosById(String id) {
    return fetch("SELECT dn, attributes FROM mailbox.cos_attributes WHERE cos_id = {0}", id);
  }

  public Optional<StoredEntry> cosByName(String name) {
    return fetch("SELECT dn, attributes FROM mailbox.cos_attributes WHERE name = {0}", name);
  }

  public Optional<StoredEntry> domainByName(String name) {
    return fetch(
        "SELECT dn, attributes FROM mailbox.domain_attributes WHERE domain_name = {0}", name);
  }

  public Optional<StoredEntry> domainById(String id) {
    return fetch(
        """
        SELECT dn, attributes FROM mailbox.domain_attributes
        WHERE attributes -> 'zimbraId' @> jsonb_build_array({0})
        """,
        id);
  }

  private Optional<StoredEntry> fetch(String sql, String key) {
    return db.resultQuery(sql, key)
        .fetchOptional()
        .filter(row -> row.get(0) != null)
        .map(PostgresEntries::entry);
  }

  private static StoredEntry entry(Record row) {
    return new StoredEntry(row.get(0, String.class), fromJson(row.get(1, JSONB.class)));
  }
}
