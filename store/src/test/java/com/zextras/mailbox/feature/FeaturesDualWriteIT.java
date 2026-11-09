package com.zextras.mailbox.feature;

import static com.zextras.mailbox.feature.MirroredFeatures.modify;
import static org.assertj.core.api.Assertions.assertThat;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Cos;
import com.zimbra.cs.account.Provisioning;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@Testcontainers
class FeaturesDualWriteIT extends MailboxTestSuite {

  private static final String MOBILE_SYNC = "zimbraFeatureMobileSyncEnabled";

  @Container
  private static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

  private static MirroredFeatures mirrored;

  @BeforeAll
  static void setUp() {
    mirrored = MirroredFeatures.enable(postgres);
  }

  @AfterAll
  static void tearDown() {
    MirroredFeatures.disable();
  }

  @Test
  void enableFeature_IsInPostgres() throws Exception {
    Account account = createAccount().create();

    modify(account, MOBILE_SYNC, "TRUE");

    assertThat(mirrored.accounts.find(account.getId()).orElseThrow().features())
        .containsEntry(MOBILE_SYNC, List.of("TRUE"));
  }

  @Test
  void flipFeature_PostgresHasLatest() throws Exception {
    Account account = createAccount().create();

    modify(account, MOBILE_SYNC, "TRUE");
    modify(account, MOBILE_SYNC, "FALSE");

    assertThat(mirrored.accounts.find(account.getId()).orElseThrow().features())
        .containsEntry(MOBILE_SYNC, List.of("FALSE"));
  }

  @Test
  void removeFeature_IsGoneFromPostgres() throws Exception {
    Account account = createAccount().create();
    modify(account, MOBILE_SYNC, "TRUE");

    modify(account, "-" + MOBILE_SYNC, "TRUE");

    assertThat(mirrored.accounts.find(account.getId()).orElseThrow().features())
        .doesNotContainKey(MOBILE_SYNC);
  }

  @Test
  void modifyNonFeature_WritesNothing() throws Exception {
    Account account = createAccount().create();

    modify(account, "displayName", "Someone");

    assertThat(mirrored.accounts.find(account.getId())).isEmpty();
  }

  @Test
  void assignCos_IsInPostgres() throws Exception {
    Cos cos = createCos();
    Account account = createAccount().create();

    modify(account, "zimbraCOSId", cos.getId());

    assertThat(mirrored.accounts.find(account.getId()).orElseThrow().cosId())
        .isEqualTo(cos.getId());
  }

  @Test
  void flipCosFeature_PostgresHasLatest() throws Exception {
    Cos cos = createCos();

    modify(cos, MOBILE_SYNC, "TRUE");
    modify(cos, MOBILE_SYNC, "FALSE");

    assertThat(mirrored.coses.find(cos.getId()).orElseThrow().features())
        .containsEntry(MOBILE_SYNC, List.of("FALSE"));
  }

  private static Cos createCos() throws Exception {
    return Provisioning.getInstance().createCos(UUID.randomUUID().toString(), new HashMap<>());
  }
}
