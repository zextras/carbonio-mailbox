package com.zextras.mailbox.feature;

import static com.zextras.mailbox.feature.MirroredAttributes.modify;
import static org.assertj.core.api.Assertions.assertThat;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Cos;
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
class AccountFeatureQueriesIT extends MailboxTestSuite {

  private static final String MOBILE_SYNC = "zimbraFeatureMobileSyncEnabled";

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
  void clear() {
    mirrored.clear();
  }

  @Test
  void featureOnCos_CountsAccountsOfThatCos() throws Exception {
    Cos cos = cosWithMobileSync("TRUE");
    accountIn(cos);
    accountIn(cos);

    assertThat(queries.countEnabled(MOBILE_SYNC)).isEqualTo(2);
  }

  @Test
  void accountOverride_WinsOverCos() throws Exception {
    Cos cos = cosWithMobileSync("TRUE");
    accountIn(cos);
    Account disabled = accountIn(cos);

    modify(disabled, MOBILE_SYNC, "FALSE");

    assertThat(queries.countEnabled(MOBILE_SYNC)).isEqualTo(1);
  }

  @Test
  void flipCosFeature_ChangesCount() throws Exception {
    Cos cos = cosWithMobileSync("TRUE");
    accountIn(cos);
    accountIn(cos);

    modify(cos, MOBILE_SYNC, "FALSE");

    assertThat(queries.countEnabled(MOBILE_SYNC)).isZero();
  }

  @Test
  void featureOnAccountOnly_IsCounted() throws Exception {
    Cos cos = cosWithMobileSync("FALSE");
    Account enabled = accountIn(cos);

    modify(enabled, MOBILE_SYNC, "TRUE");

    assertThat(queries.countEnabled(MOBILE_SYNC)).isEqualTo(1);
  }

  private static Cos cosWithMobileSync(String enabled) throws Exception {
    Cos cos =
        Provisioning.getInstance().createCos(UUID.randomUUID().toString(), new HashMap<>());
    modify(cos, MOBILE_SYNC, enabled);
    return cos;
  }

  private static Account accountIn(Cos cos) throws Exception {
    Account account = createAccount().create();
    modify(account, "zimbraCOSId", cos.getId());
    return account;
  }
}
