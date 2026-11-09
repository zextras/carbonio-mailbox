package com.zextras.mailbox.feature;

import static com.zextras.mailbox.feature.MirroredAttributes.modify;
import static org.assertj.core.api.Assertions.assertThat;

import com.zextras.mailbox.MailboxTestSuite;
import com.zextras.mailbox.feature.SeatCountQuery.GlobalDefaults;
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
class SeatCountQueryIT extends MailboxTestSuite {

  private static final GlobalDefaults ALL_OFF = new GlobalDefaults(false, false, false, false);

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

  @Test
  void count_FollowsSeatRules() throws Exception {
    Cos cos = Provisioning.getInstance().createCos(UUID.randomUUID().toString(), new HashMap<>());
    modify(cos, SeatCountQuery.SYNC, "TRUE");
    modify(cos, SeatCountQuery.FILES, "FALSE");
    modify(cos, SeatCountQuery.MAILS_APP, "FALSE");
    modify(cos, SeatCountQuery.WSC, "FALSE");
    accountIn(cos);
    Account overridden = accountIn(cos);
    modify(overridden, SeatCountQuery.SYNC, "FALSE");
    Account resource = accountIn(cos);
    modify(resource, "zimbraIsSystemResource", "TRUE");
    Account closed = accountIn(cos);
    modify(closed, "zimbraAccountStatus", "closed");

    SeatCounts counts = new SeatCountQuery(mirrored.db).count(ALL_OFF);

    assertThat(counts).isEqualTo(new SeatCounts(2, 2, 0, 0, 0));
  }

  @Test
  void count_UsesGlobalDefaultWhenNoLevelSetsIt() throws Exception {
    Cos cos = Provisioning.getInstance().createCos(UUID.randomUUID().toString(), new HashMap<>());
    modify(cos, SeatCountQuery.WSC, "");
    accountIn(cos);

    SeatCounts counts =
        new SeatCountQuery(mirrored.db).count(new GlobalDefaults(false, false, false, true));

    assertThat(counts.wsc()).isEqualTo(1);
  }

  private static Account accountIn(Cos cos) throws Exception {
    Account account = createAccount().create();
    modify(account, "zimbraCOSId", cos.getId());
    return account;
  }
}
