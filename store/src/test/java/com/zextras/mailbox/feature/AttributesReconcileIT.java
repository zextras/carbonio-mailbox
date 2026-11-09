package com.zextras.mailbox.feature;

import static com.zextras.mailbox.feature.MirroredAttributes.modify;
import static org.assertj.core.api.Assertions.assertThat;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Cos;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Provisioning;
import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@Testcontainers
class AttributesReconcileIT extends MailboxTestSuite {

  private static final String MOBILE_SYNC = "zimbraFeatureMobileSyncEnabled";
  private static final String FUTURE_CSN = "20991231235959.999999Z#000000#000#000000";

  @Container
  private static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

  private static MirroredAttributes mirrored;

  @BeforeAll
  static void setUp() {
    mirrored = MirroredAttributes.enable(postgres);
  }

  @AfterAll
  static void tearDown() {
    MirroredAttributes.disable();
  }

  @BeforeEach
  void clear() {
    mirrored.clear();
  }

  @AfterEach
  void resume() {
    mirrored.resume();
  }

  @Test
  void reconcile_FillsEntriesCreatedWhileMirrorWasOff() throws Exception {
    mirrored.pause();
    Cos cos = createCos();
    modify(cos, MOBILE_SYNC, "TRUE");
    Domain domain = createDomain();
    modify(domain, "zimbraDomainDefaultCOSId", cos.getId());
    Account account = createAccount().withDomain(domain.getName()).create();
    mirrored.resume();

    mirrored.reconcile().run();

    assertThat(mirrored.accounts.find(account.getId()).orElseThrow().domainName())
        .isEqualTo(domain.getName());
    assertThat(mirrored.domains.find(domain.getName()).orElseThrow().defaultCosId())
        .isEqualTo(cos.getId());
    assertThat(mirrored.coses.find(cos.getId()).orElseThrow().attributes())
        .containsEntry(MOBILE_SYNC, List.of("TRUE"));
    assertThat(mirrored.queries().effectiveValue(account.getId(), MOBILE_SYNC))
        .contains("TRUE");
  }

  @Test
  void reconcile_RemovesRowsOfMissingEntries() {
    mirrored.accounts.save(new AccountAttributes("gone", "gone.com", null, FUTURE_CSN, Map.of()));

    mirrored.reconcile().run();

    assertThat(mirrored.accounts.find("gone")).isEmpty();
  }

  @Test
  void reconcile_KeepsNewerRow() throws Exception {
    Account account = createAccount().create();
    mirrored.accounts.save(
        new AccountAttributes(
            account.getId(), account.getDomainName(), null, FUTURE_CSN, Map.of()));

    mirrored.reconcile().run();

    assertThat(mirrored.accounts.find(account.getId()).orElseThrow().entryCsn())
        .isEqualTo(FUTURE_CSN);
  }

  @Test
  void reconcile_SkipsWhileAnotherNodeRunsIt() throws Exception {
    mirrored.accounts.save(new AccountAttributes("gone", "gone.com", null, FUTURE_CSN, Map.of()));

    try (Connection otherNode = mirrored.dataSource.getConnection();
        Statement lock = otherNode.createStatement()) {
      lock.execute("SELECT pg_advisory_lock(4706)");

      mirrored.reconcile().run();
    }

    assertThat(mirrored.accounts.find("gone")).isPresent();
  }

  private static Cos createCos() throws Exception {
    return Provisioning.getInstance().createCos(UUID.randomUUID().toString(), new HashMap<>());
  }

  private static Domain createDomain() throws Exception {
    return Provisioning.getInstance().createDomain(UUID.randomUUID() + ".com", new HashMap<>());
  }
}
