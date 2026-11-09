package com.zextras.mailbox.feature;

import static com.zextras.mailbox.feature.MirroredAttributes.modify;
import static org.assertj.core.api.Assertions.assertThat;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Cos;
import com.zimbra.cs.account.Domain;
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
class AttributesDualWriteIT extends MailboxTestSuite {

  private static final String MOBILE_SYNC = "zimbraFeatureMobileSyncEnabled";

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

  @Test
  void enableFeature_IsInPostgres() throws Exception {
    Account account = createAccount().create();

    modify(account, MOBILE_SYNC, "TRUE");

    assertThat(mirrored.accounts.find(account.getId()).orElseThrow().attributes())
        .containsEntry(MOBILE_SYNC, List.of("TRUE"));
  }

  @Test
  void flipFeature_PostgresHasLatest() throws Exception {
    Account account = createAccount().create();

    modify(account, MOBILE_SYNC, "TRUE");
    modify(account, MOBILE_SYNC, "FALSE");

    assertThat(mirrored.accounts.find(account.getId()).orElseThrow().attributes())
        .containsEntry(MOBILE_SYNC, List.of("FALSE"));
  }

  @Test
  void removeFeature_IsGoneFromPostgres() throws Exception {
    Account account = createAccount().create();
    modify(account, MOBILE_SYNC, "TRUE");

    modify(account, "-" + MOBILE_SYNC, "TRUE");

    assertThat(mirrored.accounts.find(account.getId()).orElseThrow().attributes())
        .doesNotContainKey(MOBILE_SYNC);
  }

  @Test
  void createAccount_IsInPostgres() throws Exception {
    Account account = createAccount().create();

    assertThat(mirrored.accounts.find(account.getId()).orElseThrow().domainName())
        .isEqualTo(account.getDomainName());
  }

  @Test
  void modifyNonFeature_KeepsMirroredRow() throws Exception {
    Account account = createAccount().create();
    String mirroredCsn = mirrored.accounts.find(account.getId()).orElseThrow().entryCsn();

    modify(account, "displayName", "Someone");

    assertThat(mirrored.accounts.find(account.getId()).orElseThrow().entryCsn())
        .isEqualTo(mirroredCsn);
  }

  @Test
  void setDomainDefaultCos_IsInPostgres() throws Exception {
    Cos cos = createCos();
    Domain domain = createDomain();

    modify(domain, "zimbraDomainDefaultCOSId", cos.getId());

    assertThat(mirrored.domains.find(domain.getName()).orElseThrow().defaultCosId())
        .isEqualTo(cos.getId());
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

    assertThat(mirrored.coses.find(cos.getId()).orElseThrow().attributes())
        .containsEntry(MOBILE_SYNC, List.of("FALSE"));
  }

  @Test
  void seatAttributes_AreInPostgres() throws Exception {
    Account account = createAccount().create();

    modify(account, "carbonioFeatureFilesEnabled", "FALSE");
    modify(account, "zimbraIsSystemResource", "TRUE");
    modify(account, "zimbraAccountStatus", "locked");

    assertThat(mirrored.accounts.find(account.getId()).orElseThrow().attributes())
        .containsEntry("carbonioFeatureFilesEnabled", List.of("FALSE"))
        .containsEntry("zimbraIsSystemResource", List.of("TRUE"))
        .containsEntry("zimbraAccountStatus", List.of("locked"));
  }

  @Test
  void deleteAccount_IsGoneFromPostgres() throws Exception {
    Account account = createAccount().create();
    assertThat(mirrored.accounts.find(account.getId())).isPresent();

    Provisioning.getInstance().deleteAccount(account.getId());

    assertThat(mirrored.accounts.find(account.getId())).isEmpty();
  }

  @Test
  void deleteCos_IsGoneFromPostgres() throws Exception {
    Cos cos = createCos();
    assertThat(mirrored.coses.find(cos.getId())).isPresent();

    Provisioning.getInstance().deleteCos(cos.getId());

    assertThat(mirrored.coses.find(cos.getId())).isEmpty();
  }

  @Test
  void deleteDomain_IsGoneFromPostgres() throws Exception {
    Domain domain = createDomain();
    assertThat(mirrored.domains.find(domain.getName())).isPresent();

    Provisioning.getInstance().deleteDomain(domain.getId());

    assertThat(mirrored.domains.find(domain.getName())).isEmpty();
  }

  private static Cos createCos() throws Exception {
    return Provisioning.getInstance().createCos(UUID.randomUUID().toString(), new HashMap<>());
  }

  private static Domain createDomain() throws Exception {
    return Provisioning.getInstance().createDomain(UUID.randomUUID() + ".com", new HashMap<>());
  }
}
