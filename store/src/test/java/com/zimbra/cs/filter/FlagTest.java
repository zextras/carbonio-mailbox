// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.filter;

import com.zimbra.common.filter.Sieve.Flag;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.MockProvisioning;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.DeliveryContext;
import com.zimbra.cs.mailbox.Flag.FlagInfo;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.mailbox.Message;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.mime.ParsedMessage;
import com.zimbra.cs.service.util.ItemId;
import com.zimbra.cs.util.ZTestWatchman;
import java.util.HashMap;
import java.util.List;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.junit.rules.TestName;

/**
 * Unit test for {@link Flag}.
 *
 * @author ysasaki
 */
public final class FlagTest {

  @Rule public TestName testName = new TestName();
  @Rule public MethodRule watchman = new ZTestWatchman();

  @BeforeClass
  public static void init() throws Exception {
    MailboxTestUtil.initServer();
    Provisioning prov = Provisioning.getInstance();
    prov.createAccount("test@zimbra.com", "secret", new HashMap<String, Object>());
  }

  @Before
  public void setUp() throws Exception {
    System.out.println(testName.getMethodName());
  }

  @Test
  public void priority() throws Exception {
    Account account = Provisioning.getInstance().getAccount(MockProvisioning.DEFAULT_ACCOUNT_ID);
    RuleManager.clearCachedRules(account);
    account.setMailSieveScript("if header \"Subject\" \"important\" { flag \"priority\"; }");
    Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);

    // Precedence: bulk
    List<ItemId> ids =
        RuleManager.applyRulesToIncomingMessage(
            new OperationContext(mbox),
            mbox,
            new ParsedMessage("From: test@zimbra.com\nSubject: important".getBytes(), false),
            0,
            account.getName(),
            new DeliveryContext(),
            Mailbox.ID_FOLDER_INBOX,
            true);
    Assert.assertEquals(1, ids.size());
    Message msg = mbox.getMessageById(null, ids.get(0).getId());
    Assert.assertTrue(msg.isTagged(FlagInfo.PRIORITY));
  }

  @After
  public void tearDown() {
    try {
      MailboxTestUtil.clearData();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
