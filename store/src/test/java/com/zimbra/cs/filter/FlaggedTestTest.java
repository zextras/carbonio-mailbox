// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.filter;

import static org.junit.jupiter.api.Assertions.*;

import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.filter.jsieve.FlaggedTest;
import com.zimbra.cs.mailbox.DeliveryContext;
import com.zimbra.cs.mailbox.DeliveryOptions;
import com.zimbra.cs.mailbox.Flag;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.mailbox.Message;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.mime.ParsedMessage;
import com.zimbra.cs.service.util.ItemId;
import java.util.HashMap;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link FlaggedTest}.
 *
 * @author ysasaki
 */
public final class FlaggedTestTest {
  private static Account account;

  @BeforeAll
  public static void init() throws Exception {
    MailboxTestUtil.initServer();
    Provisioning prov = Provisioning.getInstance();
    account = prov.createAccount("test@zimbra.com", "secret", new HashMap<String, Object>());
  }

    @BeforeEach
    public void setUp() throws Exception {
        MailboxTestUtil.clearData();
    }

 @Test
 void incoming() throws Exception {
  RuleManager.clearCachedRules(account);
  account.setMailSieveScript("if me :in \"To\" { flag \"priority\"; }\n" +
    "if flagged \"priority\" { stop; }\n" +
    "if header :contains \"Subject\" \"test\" { fileinto \"test\"; }");
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);

  List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(new OperationContext(mbox), mbox,
    new ParsedMessage("From: sender@zimbra.com\nTo: test@zimbra.com\nSubject: test".getBytes(), false),
    0, account.getName(), new DeliveryContext(), Mailbox.ID_FOLDER_INBOX, true);
  assertEquals(1, ids.size());
  Message msg = mbox.getMessageById(null, ids.get(0).getId());
  assertTrue(msg.isTagged(Flag.FlagInfo.PRIORITY));
  assertEquals(Mailbox.ID_FOLDER_INBOX, msg.getFolderId());
 }

 @Test
 void existing() throws Exception {
  RuleManager.clearCachedRules(account);
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);
  OperationContext octx = new OperationContext(mbox);
  Message msg = mbox.addMessage(octx,
    new ParsedMessage("From: sender@zimbra.com\nTo: test@zimbra.com\nSubject: test".getBytes(), false),
    new DeliveryOptions().setFolderId(Mailbox.ID_FOLDER_INBOX).setFlags(Flag.BITMASK_PRIORITY),
    new DeliveryContext());

  boolean filtered = RuleManager.applyRulesToExistingMessage(new OperationContext(mbox), mbox, msg.getId(),
    RuleManager.parse("if flagged \"priority\" { stop; }\n" +
      "if header :contains \"Subject\" \"test\" { fileinto \"test\"; }"));
  assertEquals(false, filtered);
  assertEquals(Mailbox.ID_FOLDER_INBOX, mbox.getMessageById(octx, msg.getId()).getFolderId());
 }

}
