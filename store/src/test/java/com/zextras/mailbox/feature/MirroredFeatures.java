package com.zextras.mailbox.feature;

import com.zimbra.cs.account.Entry;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.ldap.LdapProvisioning;
import java.util.HashMap;
import java.util.Map;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.postgresql.ds.PGSimpleDataSource;
import org.testcontainers.postgresql.PostgreSQLContainer;

final class MirroredFeatures {

  final DSLContext db;
  final PostgresAccountFeatures accounts;
  final PostgresCosFeatures coses;
  final PostgresDomainDefaultCos domains;

  private MirroredFeatures(DSLContext db) {
    this.db = db;
    this.accounts = new PostgresAccountFeatures(db);
    this.coses = new PostgresCosFeatures(db);
    this.domains = new PostgresDomainDefaultCos(db);
  }

  static MirroredFeatures enable(PostgreSQLContainer postgres) {
    PGSimpleDataSource dataSource = new PGSimpleDataSource();
    dataSource.setUrl(postgres.getJdbcUrl());
    dataSource.setUser(postgres.getUsername());
    dataSource.setPassword(postgres.getPassword());
    new MailboxSchemaMigration(dataSource).migrate();
    MirroredFeatures features = new MirroredFeatures(DSL.using(dataSource, SQLDialect.POSTGRES));
    ldapProvisioning()
        .setFeaturesMirror(
            new FeaturesMirror(features.accounts, features.coses, features.domains));
    return features;
  }

  static void disable() {
    ldapProvisioning().setFeaturesMirror(null);
  }

  void clear() {
    db.truncate(PostgresAccountFeatures.ACCOUNT_FEATURES).execute();
    db.truncate(PostgresCosFeatures.COS_FEATURES).execute();
    db.truncate(PostgresDomainDefaultCos.DOMAIN_DEFAULT_COS).execute();
  }

  static void modify(Entry entry, String attribute, String value) throws Exception {
    Map<String, Object> changes = new HashMap<>();
    changes.put(attribute, value);
    Provisioning.getInstance().modifyAttrs(entry, changes);
  }

  private static LdapProvisioning ldapProvisioning() {
    return (LdapProvisioning) Provisioning.getInstance();
  }
}
