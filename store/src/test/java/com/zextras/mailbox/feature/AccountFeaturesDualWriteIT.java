package com.zextras.mailbox.feature;

import static org.assertj.core.api.Assertions.assertThat;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.ldap.LdapProvisioning;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.postgresql.ds.PGSimpleDataSource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@Testcontainers
class AccountFeaturesDualWriteIT extends MailboxTestSuite {

  private static final String MOBILE_SYNC = "zimbraFeatureMobileSyncEnabled";

  @Container
  private static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

  private static PostgresAccountFeatures accountFeatures;

  @BeforeAll
  static void setUp() {
    PGSimpleDataSource dataSource = new PGSimpleDataSource();
    dataSource.setUrl(postgres.getJdbcUrl());
    dataSource.setUser(postgres.getUsername());
    dataSource.setPassword(postgres.getPassword());
    new MailboxSchemaMigration(dataSource).migrate();
    accountFeatures = new PostgresAccountFeatures(DSL.using(dataSource, SQLDialect.POSTGRES));
    ldapProvisioning().setAccountFeaturesMirror(new AccountFeaturesMirror(accountFeatures));
  }

  @AfterAll
  static void tearDown() {
    ldapProvisioning().setAccountFeaturesMirror(null);
  }

  @Test
  void enableFeature_IsInPostgres() throws Exception {
    Account account = createAccount().create();

    setMobileSync(account, "TRUE");

    assertThat(featuresOf(account)).containsEntry(MOBILE_SYNC, List.of("TRUE"));
  }

  @Test
  void flipFeature_PostgresHasLatest() throws Exception {
    Account account = createAccount().create();

    setMobileSync(account, "TRUE");
    setMobileSync(account, "FALSE");

    assertThat(featuresOf(account)).containsEntry(MOBILE_SYNC, List.of("FALSE"));
  }

  @Test
  void removeFeature_IsGoneFromPostgres() throws Exception {
    Account account = createAccount().create();
    setMobileSync(account, "TRUE");

    modify(account, "-" + MOBILE_SYNC, "TRUE");

    assertThat(featuresOf(account)).doesNotContainKey(MOBILE_SYNC);
  }

  @Test
  void modifyNonFeature_WritesNothing() throws Exception {
    Account account = createAccount().create();

    modify(account, "displayName", "Someone");

    assertThat(accountFeatures.find(account.getId())).isEmpty();
  }

  private static void setMobileSync(Account account, String enabled) throws Exception {
    modify(account, MOBILE_SYNC, enabled);
  }

  private static void modify(Account account, String attribute, String value) throws Exception {
    Provisioning.getInstance().modifyAttrs(account, new HashMap<>(Map.of(attribute, value)));
  }

  private static Map<String, List<String>> featuresOf(Account account) {
    return accountFeatures.find(account.getId()).orElseThrow().features();
  }

  private static LdapProvisioning ldapProvisioning() {
    return (LdapProvisioning) Provisioning.getInstance();
  }
}
