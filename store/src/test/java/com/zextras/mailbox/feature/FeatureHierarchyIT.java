package com.zextras.mailbox.feature;

import static com.zextras.mailbox.feature.MirroredAttributes.modify;
import static org.assertj.core.api.Assertions.assertThat;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.common.account.Key;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Cos;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Provisioning;
import java.util.HashMap;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@Testcontainers
class FeatureHierarchyIT extends MailboxTestSuite {

  private static final String MOBILE_SYNC = "zimbraFeatureMobileSyncEnabled";
  private static final String CONTACTS = "zimbraFeatureContactsEnabled";

  @Container
  private static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

  private static MirroredAttributes mirrored;
  private static AccountFeatureQueries queries;

  @BeforeAll
  static void setUp() {
    mirrored = MirroredAttributes.enable(postgres);
    queries = mirrored.queries();
  }

  @AfterAll
  static void tearDown() {
    MirroredAttributes.disable();
  }

  @BeforeEach
  void clear() throws Exception {
    mirrored.clear();
    modify(defaultCos(), MOBILE_SYNC, "FALSE");
  }

  @Test
  void accountValue_WinsOverItsCos() throws Exception {
    Account account = accountIn(domainWithDefault(cosWithMobileSync("TRUE")));
    modify(account, "zimbraCOSId", cosWithMobileSync("TRUE").getId());

    modify(account, MOBILE_SYNC, "FALSE");

    assertEffectiveValue(account, "FALSE");
  }

  @Test
  void accountCos_WinsOverDomainDefaultCos() throws Exception {
    Account account = accountIn(domainWithDefault(cosWithMobileSync("TRUE")));

    modify(account, "zimbraCOSId", cosWithMobileSync("FALSE").getId());

    assertEffectiveValue(account, "FALSE");
  }

  @Test
  void noAccountCos_UsesDomainDefaultCos() throws Exception {
    Account account = accountIn(domainWithDefault(cosWithMobileSync("TRUE")));

    assertEffectiveValue(account, "TRUE");
  }

  @Test
  void noDomainDefaultCos_UsesDefaultCos() throws Exception {
    modify(defaultCos(), MOBILE_SYNC, "TRUE");

    Account account = accountIn(domainWithoutDefault());

    assertEffectiveValue(account, "TRUE");
  }

  @Test
  void changeDomainDefaultCos_FollowsNewCos() throws Exception {
    Domain domain = domainWithDefault(cosWithMobileSync("TRUE"));
    Account account = accountIn(domain);

    modify(domain, "zimbraDomainDefaultCOSId", cosWithMobileSync("FALSE").getId());

    assertEffectiveValue(account, "FALSE");
  }

  @Test
  void flipDefaultCos_ChangesCount() throws Exception {
    accountIn(domainWithoutDefault());
    accountIn(domainWithoutDefault());

    modify(defaultCos(), MOBILE_SYNC, "TRUE");

    assertThat(queries.countEnabled(MOBILE_SYNC)).isEqualTo(2);
  }

  @Test
  void featureUnsetEverywhere_UsesAttributeDefault() throws Exception {
    Cos cos = Provisioning.getInstance().createCos(UUID.randomUUID().toString(), new HashMap<>());
    modify(cos, CONTACTS, "");
    Account account = accountIn(domainWithoutDefault());

    modify(account, "zimbraCOSId", cos.getId());

    Provisioning.getInstance().reload(account);
    assertThat(account.getAttr(CONTACTS)).isNull();
    assertThat(account.isFeatureContactsEnabled()).isTrue();
    assertThat(queries.effectiveValue(account.getId(), CONTACTS)).contains("TRUE");
  }

  private static void assertEffectiveValue(Account account, String expected) throws Exception {
    Provisioning.getInstance().reload(account);
    assertThat(account.getAttr(MOBILE_SYNC)).isEqualTo(expected);
    assertThat(queries.effectiveValue(account.getId(), MOBILE_SYNC)).contains(expected);
    assertThat(queries.effectiveCosId(account.getId()))
        .contains(Provisioning.getInstance().getCOS(account).getId());
  }

  private static Cos defaultCos() throws Exception {
    return Provisioning.getInstance().get(Key.CosBy.name, Provisioning.DEFAULT_COS_NAME);
  }

  private static Cos cosWithMobileSync(String enabled) throws Exception {
    Cos cos = Provisioning.getInstance().createCos(UUID.randomUUID().toString(), new HashMap<>());
    modify(cos, MOBILE_SYNC, enabled);
    return cos;
  }

  private static Domain domainWithDefault(Cos cos) throws Exception {
    Domain domain = domainWithoutDefault();
    modify(domain, "zimbraDomainDefaultCOSId", cos.getId());
    return domain;
  }

  private static Domain domainWithoutDefault() throws Exception {
    return Provisioning.getInstance().createDomain(UUID.randomUUID() + ".com", new HashMap<>());
  }

  private static Account accountIn(Domain domain) throws Exception {
    return createAccount().withDomain(domain.getName()).create();
  }
}
