package com.zextras.mailbox.feature;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Entry;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.StoreAttributeManager;
import com.zimbra.cs.account.ldap.LdapProvisioning;
import java.util.HashMap;
import java.util.Map;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.postgresql.ds.PGSimpleDataSource;
import org.testcontainers.postgresql.PostgreSQLContainer;

final class MirroredAttributes {

  final PGSimpleDataSource dataSource;
  final DSLContext db;
  final PostgresAccountAttributes accounts;
  final PostgresCosAttributes coses;
  final PostgresDomainDefaultCos domains;
  final AttributesMirror mirror;

  private MirroredAttributes(PGSimpleDataSource dataSource) {
    this.dataSource = dataSource;
    this.db = DSL.using(dataSource, SQLDialect.POSTGRES);
    this.accounts = new PostgresAccountAttributes(db);
    this.coses = new PostgresCosAttributes(db);
    this.domains = new PostgresDomainDefaultCos(db);
    this.mirror = new AttributesMirror(accounts, coses, domains);
  }

  static MirroredAttributes enable(PostgreSQLContainer postgres) {
    PGSimpleDataSource dataSource = new PGSimpleDataSource();
    dataSource.setUrl(postgres.getJdbcUrl());
    dataSource.setUser(postgres.getUsername());
    dataSource.setPassword(postgres.getPassword());
    new MailboxSchemaMigration(dataSource).migrate();
    MirroredAttributes features = new MirroredAttributes(dataSource);
    features.resume();
    return features;
  }

  void pause() {
    ldapProvisioning().setAttributesMirror(null);
  }

  void resume() {
    ldapProvisioning().setAttributesMirror(mirror);
  }

  AccountFeatureQueries queries() {
    try {
      return new AccountFeatureQueries(db, StoreAttributeManager.getInstance());
    } catch (ServiceException e) {
      throw new IllegalStateException(e);
    }
  }

  AttributesReconcile reconcile() {
    return new AttributesReconcile(ldapProvisioning(), db);
  }

  static void disable() {
    ldapProvisioning().setAttributesMirror(null);
  }

  void clear() {
    db.truncate(PostgresAccountAttributes.ACCOUNT_ATTRIBUTES).execute();
    db.truncate(PostgresCosAttributes.COS_ATTRIBUTES).execute();
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
