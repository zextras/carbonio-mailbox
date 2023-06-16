// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.filter;

import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.zimbra.common.util.ArrayUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.MockProvisioning;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.filter.jsieve.FacebookTest;
import com.zimbra.cs.mailbox.DeliveryContext;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.mailbox.Message;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.mime.ParsedMessage;
import com.zimbra.cs.service.util.ItemId;

/**
 * Unit test for {@link FacebookTest}.
 *
 * @author ysasaki
 */
public final class FacebookTestTest {

    @BeforeAll
    public static void init() throws Exception {
        MailboxTestUtil.initServer();
        Provisioning prov = Provisioning.getInstance();
        prov.createAccount("test@zimbra.com", "secret", new HashMap<String, Object>());
    }

    @BeforeEach
    public void setUp() throws Exception {
        MailboxTestUtil.clearData();
    }

 @Test
 void test() throws Exception {
  Account account = Provisioning.getInstance().getAccount(MockProvisioning.DEFAULT_ACCOUNT_ID);
  RuleManager.clearCachedRules(account);
  account.setMailSieveScript("if facebook { tag \"facebook\"; }");
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);

  // notifications
  List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(new OperationContext(mbox), mbox,
    new ParsedMessage("From: \"Facebook\" <notification+mhw1-ivm@facebookmail.com>\n".getBytes(), false),
    0, account.getName(), new DeliveryContext(), Mailbox.ID_FOLDER_INBOX, true);
  assertEquals(1, ids.size());
  Message msg = mbox.getMessageById(null, ids.get(0).getId());
  assertEquals("facebook", ArrayUtil.getFirstElement(msg.getTags()));

  // deals
  ids = RuleManager.applyRulesToIncomingMessage(new OperationContext(mbox), mbox,
    new ParsedMessage("From: \"Facebook Deals\" <deals+mhw1-ivm@facebookmail.com>\n".getBytes(), false),
    0, account.getName(), new DeliveryContext(), Mailbox.ID_FOLDER_INBOX, true);
  assertEquals(1, ids.size());
  msg = mbox.getMessageById(null, ids.get(0).getId());
  assertEquals(msg.getTags().length, 0);
 }

}
