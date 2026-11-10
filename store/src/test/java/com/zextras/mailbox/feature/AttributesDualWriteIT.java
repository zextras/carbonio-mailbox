package com.zextras.mailbox.feature;

import static com.zextras.mailbox.feature.MirroredAttributes.modify;
import static org.assertj.core.api.Assertions.assertThat;

import com.zextras.mailbox.MailboxTestSuite;
import com.zextras.mailbox.util.EntryCsnInterceptor;
import com.zimbra.common.account.Key;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Cos;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.ldap.LdapProv;
import com.zimbra.cs.account.ldap.entry.LdapEntry;
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
  void flipFeature_SavesFromModifyResponseWithoutRead() throws Exception {
    Account account = createAccount().create();
    long readsBefore = EntryCsnInterceptor.entryCsnReads();

    modify(account, MOBILE_SYNC, "FALSE");

    assertThat(EntryCsnInterceptor.entryCsnReads()).isEqualTo(readsBefore);
    assertThat(mirrored.accounts.find(account.getId()).orElseThrow().attributes())
        .containsEntry(MOBILE_SYNC, List.of("FALSE"));
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
  void modifyDisplayName_IsInPostgres() throws Exception {
    Account account = createAccount().create();

    modify(account, "displayName", "Someone");

    assertThat(mirrored.accounts.find(account.getId()).orElseThrow().attributes())
        .containsEntry("displayName", List.of("Someone"));
  }

  @Test
  void createAccount_StoresDn() throws Exception {
    Account account = createAccount().create();

    assertThat(mirrored.accounts.find(account.getId()).orElseThrow().dn())
        .isEqualTo(((LdapEntry) account).getDN());
  }

  @Test
  void createAccount_CopiesAllAttributes() throws Exception {
    Account account = createAccount().create();

    assertThat(mirrored.accounts.find(account.getId()).orElseThrow().attributes())
        .containsEntry("zimbraId", List.of(account.getId()))
        .containsEntry("zimbraMailDeliveryAddress", List.of(account.getName()))
        .containsKey("objectClass")
        .doesNotContainKey(AttributesMirror.ENTRY_CSN);
  }

  @Test
  void modifyCosDescription_IsInPostgres() throws Exception {
    Cos cos = createCos();

    modify(cos, "description", "Gold");

    assertThat(mirrored.coses.find(cos.getId()).orElseThrow().attributes())
        .containsEntry("description", List.of("Gold"));
  }

  @Test
  void modifyDomain_IsInPostgres() throws Exception {
    Domain domain = createDomain();

    modify(domain, "zimbraVirtualHostname", "mail." + domain.getName());

    DomainAttributes row = mirrored.domains.find(domain.getName()).orElseThrow();
    assertThat(row.dn()).isEqualTo(((LdapEntry) domain).getDN());
    assertThat(row.attributes())
        .containsEntry("zimbraVirtualHostname", List.of("mail." + domain.getName()));
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

  @Test
  void setPassword_IsInPostgres() throws Exception {
    Account account = createAccount().create();
    List<String> before =
        mirrored.accounts.find(account.getId()).orElseThrow().attributes().get("userPassword");

    Provisioning.getInstance().setPassword(account, "changed-" + UUID.randomUUID());

    assertThat(
            mirrored.accounts.find(account.getId()).orElseThrow().attributes().get("userPassword"))
        .isNotNull()
        .isNotEqualTo(before);
  }

  @Test
  void renameAccount_StoresNewDn() throws Exception {
    Account account = createAccount().create();
    String newName = UUID.randomUUID() + "@" + account.getDomainName();

    Provisioning.getInstance().renameAccount(account.getId(), newName);

    Account renamed = Provisioning.getInstance().getAccountById(account.getId());
    AccountAttributes row = mirrored.accounts.find(account.getId()).orElseThrow();
    assertThat(row.dn()).isEqualTo(((LdapEntry) renamed).getDN());
    assertThat(row.attributes()).containsEntry("zimbraMailDeliveryAddress", List.of(newName));
  }

  @Test
  void renameCos_StoresNewDn() throws Exception {
    Cos cos = createCos();
    String newName = UUID.randomUUID().toString();

    Provisioning.getInstance().renameCos(cos.getId(), newName);

    CosAttributes row = mirrored.coses.find(cos.getId()).orElseThrow();
    assertThat(row.name()).isEqualTo(newName);
    assertThat(row.dn())
        .isEqualTo(((LdapEntry) Provisioning.getInstance().getCosById(cos.getId())).getDN());
  }

  @Test
  void renameDomain_MovesDomainRow() throws Exception {
    Domain domain = createDomain();
    String newName = UUID.randomUUID() + ".com";

    ((LdapProv) Provisioning.getInstance()).renameDomain(domain.getId(), newName);

    Domain renamed = Provisioning.getInstance().get(Key.DomainBy.name, newName);
    assertThat(mirrored.domains.find(domain.getName())).isEmpty();
    assertThat(mirrored.domains.find(newName).orElseThrow().dn())
        .isEqualTo(((LdapEntry) renamed).getDN());
  }

  private static Cos createCos() throws Exception {
    return Provisioning.getInstance().createCos(UUID.randomUUID().toString(), new HashMap<>());
  }

  private static Domain createDomain() throws Exception {
    return Provisioning.getInstance().createDomain(UUID.randomUUID() + ".com", new HashMap<>());
  }
}
