package com.zextras.mailbox.feature;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.postgresql.ds.PGSimpleDataSource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@Testcontainers
class PostgresAccountFeaturesIT {

  private static final String OLDER_CSN = "20261109120000.000000Z#000000#001#000000";
  private static final String NEWER_CSN = "20261109120001.000000Z#000000#001#000000";

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
  }

  @Test
  void save_FindsFeatures() {
    AccountFeatures features = mobileSync("a1", OLDER_CSN, "TRUE");

    accountFeatures.save(features);

    assertThat(accountFeatures.find("a1")).contains(features);
  }

  @Test
  void saveNewerCsn_ReplacesFeatures() {
    accountFeatures.save(mobileSync("a2", OLDER_CSN, "TRUE"));
    AccountFeatures newer = mobileSync("a2", NEWER_CSN, "FALSE");

    accountFeatures.save(newer);

    assertThat(accountFeatures.find("a2")).contains(newer);
  }

  @Test
  void saveOlderCsn_KeepsNewerFeatures() {
    AccountFeatures newer = mobileSync("a3", NEWER_CSN, "FALSE");
    accountFeatures.save(newer);

    accountFeatures.save(mobileSync("a3", OLDER_CSN, "TRUE"));

    assertThat(accountFeatures.find("a3")).contains(newer);
  }

  @Test
  void findMissing_IsEmpty() {
    assertThat(accountFeatures.find("missing")).isEmpty();
  }

  private static AccountFeatures mobileSync(String accountId, String entryCsn, String enabled) {
    return new AccountFeatures(
        accountId, entryCsn, Map.of("zimbraFeatureMobileSyncEnabled", List.of(enabled)));
  }
}
