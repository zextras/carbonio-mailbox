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
class PostgresAccountAttributesIT {

  private static final String OLDER_CSN = "20261109120000.000000Z#000000#001#000000";
  private static final String NEWER_CSN = "20261109120001.000000Z#000000#001#000000";

  @Container
  private static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

  private static PostgresAccountAttributes accountFeatures;

  @BeforeAll
  static void setUp() {
    PGSimpleDataSource dataSource = new PGSimpleDataSource();
    dataSource.setUrl(postgres.getJdbcUrl());
    dataSource.setUser(postgres.getUsername());
    dataSource.setPassword(postgres.getPassword());
    new MailboxSchemaMigration(dataSource).migrate();
    accountFeatures = new PostgresAccountAttributes(DSL.using(dataSource, SQLDialect.POSTGRES));
  }

  @Test
  void save_FindsFeatures() {
    AccountAttributes features = mobileSync("a1", OLDER_CSN, "TRUE");

    accountFeatures.save(features);

    assertThat(accountFeatures.find("a1")).contains(features);
  }

  @Test
  void saveNewerCsn_ReplacesFeatures() {
    accountFeatures.save(mobileSync("a2", OLDER_CSN, "TRUE"));
    AccountAttributes newer = mobileSync("a2", NEWER_CSN, "FALSE");

    accountFeatures.save(newer);

    assertThat(accountFeatures.find("a2")).contains(newer);
  }

  @Test
  void saveOlderCsn_KeepsNewerFeatures() {
    AccountAttributes newer = mobileSync("a3", NEWER_CSN, "FALSE");
    accountFeatures.save(newer);

    accountFeatures.save(mobileSync("a3", OLDER_CSN, "TRUE"));

    assertThat(accountFeatures.find("a3")).contains(newer);
  }

  @Test
  void saveSameCsnMoreAttributes_ReplacesRow() {
    accountFeatures.save(mobileSync("a4", OLDER_CSN, "TRUE"));
    AccountAttributes moreAttributes =
        new AccountAttributes(
            "a4",
            "uid=a4,ou=people,dc=test,dc=com",
            "test.com",
            "cos-1",
            OLDER_CSN,
            Map.of(
                "zimbraFeatureMobileSyncEnabled", List.of("TRUE"),
                "zimbraAccountStatus", List.of("active")));

    accountFeatures.save(moreAttributes);

    assertThat(accountFeatures.find("a4")).contains(moreAttributes);
  }

  @Test
  void findMissing_IsEmpty() {
    assertThat(accountFeatures.find("missing")).isEmpty();
  }

  private static AccountAttributes mobileSync(String accountId, String entryCsn, String enabled) {
    return new AccountAttributes(
        accountId,
        "uid=" + accountId + ",ou=people,dc=test,dc=com",
        "test.com",
        "cos-1",
        entryCsn,
        Map.of("zimbraFeatureMobileSyncEnabled", List.of(enabled)));
  }
}
