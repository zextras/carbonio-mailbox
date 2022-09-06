// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.filter;

import com.zimbra.common.util.ArrayUtil;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.MockProvisioning;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.filter.jsieve.TwitterTest;
import com.zimbra.cs.mailbox.DeliveryContext;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.mailbox.Message;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.mime.ParsedMessage;
import com.zimbra.cs.service.util.ItemId;
import java.util.HashMap;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit test for {@link TwitterTest}.
 *
 * @author ysasaki
 */
public final class TwitterTestTest {

  @BeforeClass
  public static void init() throws Exception {
    MailboxTestUtil.initServer();
    Provisioning prov = Provisioning.getInstance();
    prov.createAccount("test@zimbra.com", "secret", new HashMap<String, Object>());
  }

  @Before
  public void setUp() throws Exception {
    MailboxTestUtil.clearData();
  }

  @Test
  public void test() throws Exception {
    Account account = Provisioning.getInstance().getAccount(MockProvisioning.DEFAULT_ACCOUNT_ID);
    RuleManager.clearCachedRules(account);
    account.setMailSieveScript("if twitter { tag \"twitter\"; }");
    Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);

    // direct message email: the old way the filter worked
    List<ItemId> ids =
        RuleManager.applyRulesToIncomingMessage(
            new OperationContext(mbox),
            mbox,
            new ParsedMessage(
                "From: Twitter <dm-lfnfnxv=mvzoen.pbz-4fa92@postmaster.twitter.com>\n".getBytes(),
                false),
            0,
            account.getName(),
            new DeliveryContext(),
            Mailbox.ID_FOLDER_INBOX,
            true);
    Assert.assertEquals(1, ids.size());
    Message msg = mbox.getMessageById(null, ids.get(0).getId());
    Assert.assertEquals(null, ArrayUtil.getFirstElement(msg.getTags()));

    // mention email : the old way the filter worked
    ids =
        RuleManager.applyRulesToIncomingMessage(
            new OperationContext(mbox),
            mbox,
            new ParsedMessage(
                "From: Twitter <mention-lfnfnxv=mvzoen.pbz-4fa92@postmaster.twitter.com>\n"
                    .getBytes(),
                false),
            0,
            account.getName(),
            new DeliveryContext(),
            Mailbox.ID_FOLDER_INBOX,
            true);
    Assert.assertEquals(1, ids.size());
    msg = mbox.getMessageById(null, ids.get(0).getId());
    Assert.assertEquals(null, ArrayUtil.getFirstElement(msg.getTags()));

    ids =
        RuleManager.applyRulesToIncomingMessage(
            new OperationContext(mbox),
            mbox,
            new ParsedMessage(
                "From: sharuki2 (via Twitter) <notify@twitter.com>\n".getBytes(), false),
            0,
            account.getName(),
            new DeliveryContext(),
            Mailbox.ID_FOLDER_INBOX,
            true);
    Assert.assertEquals(1, ids.size());
    msg = mbox.getMessageById(null, ids.get(0).getId());
    Assert.assertEquals("twitter", ArrayUtil.getFirstElement(msg.getTags()));
  }
}
