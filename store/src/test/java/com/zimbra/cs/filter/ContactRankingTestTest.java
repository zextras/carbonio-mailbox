// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.zextras.mailbox.util.AccountUtil;
import com.zimbra.common.util.ArrayUtil;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.filter.jsieve.ContactRankingTest;
import com.zimbra.cs.mailbox.ContactRankings;
import com.zimbra.cs.mailbox.DeliveryContext;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.mailbox.Message;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.mime.ParsedMessage;
import com.zimbra.cs.service.util.ItemId;
import java.util.Collections;
import java.util.List;
import javax.mail.internet.InternetAddress;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link ContactRankingTest}.
 *
 * @author ysasaki
 */
public final class ContactRankingTestTest {

    @BeforeAll
    public static void init() throws Exception {
        MailboxTestUtil.initServer();
    }

    @BeforeEach
    public void setUp() throws Exception {
        MailboxTestUtil.clearData();
    }

 @Test
 void filter() throws Exception {
  Account account = AccountUtil.createAccount();
  RuleManager.clearCachedRules(account);
  ContactRankings.increment(account.getId(), Collections.singleton(new InternetAddress("test1@zimbra.com")));

  account.setMailSieveScript("if contact_ranking :in \"From\" { tag \"Priority\"; }");
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);
  List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(new OperationContext(mbox), mbox,
    new ParsedMessage("From: test1@zimbra.com".getBytes(), false), 0, account.getName(),
    new DeliveryContext(), Mailbox.ID_FOLDER_INBOX, true);
  assertEquals(1, ids.size());
  Message msg = mbox.getMessageById(null, ids.get(0).getId());
  assertEquals("Priority", ArrayUtil.getFirstElement(msg.getTags()));
 }

}
