// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.filter;

import static org.junit.jupiter.api.Assertions.*;

import com.zimbra.common.util.ArrayUtil;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.filter.jsieve.MeTest;
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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link MeTest}.
 *
 * @author ysasaki
 */
public final class MeTestTest {
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
 void meInTo() throws Exception {
  
  RuleManager.clearCachedRules(account);

  account.setMailSieveScript("if me :in \"To\" { tag \"Priority\"; }");
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);
  List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(new OperationContext(mbox), mbox,
    new ParsedMessage("To: test@zimbra.com".getBytes(), false), 0, account.getName(),
    new DeliveryContext(), Mailbox.ID_FOLDER_INBOX, true);
  assertEquals(1, ids.size());
  Message msg = mbox.getMessageById(null, ids.get(0).getId());
  assertEquals("Priority", ArrayUtil.getFirstElement(msg.getTags()));
 }

 @Test
 void meInToMultiRecipient() throws Exception {
  
  RuleManager.clearCachedRules(account);

  account.setMailSieveScript("if me :in \"To\" { tag \"Priority\"; }");
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);
  List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(new OperationContext(mbox), mbox,
    new ParsedMessage("To: foo@zimbra.com, test@zimbra.com, bar@zimbra.com".getBytes(), false), 0, account.getName(),
    new DeliveryContext(), Mailbox.ID_FOLDER_INBOX, true);
  assertEquals(1, ids.size());
  Message msg = mbox.getMessageById(null, ids.get(0).getId());
  assertEquals("Priority", ArrayUtil.getFirstElement(msg.getTags()));
 }

 @Test
 void quotedMultiRecipientTo() throws Exception {
  
  RuleManager.clearCachedRules(account);

  account.setMailSieveScript("if me :in \"To\" { tag \"Priority\"; }");
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);
  List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(new OperationContext(mbox), mbox,
    new ParsedMessage("To: \"bar, foo\" <foo@zimbra.com>, \"user, test\" <test@zimbra.com>, \"aaa bbb\" <aaabbb@test.com>".getBytes(), false), 0, account.getName(),
    new DeliveryContext(), Mailbox.ID_FOLDER_INBOX, true);
  assertEquals(1, ids.size());
  Message msg = mbox.getMessageById(null, ids.get(0).getId());
  assertEquals("Priority", ArrayUtil.getFirstElement(msg.getTags()));
 }


 @Test
 void meInToOrCc() throws Exception {
  
  RuleManager.clearCachedRules(account);

  account.setMailSieveScript("if me :in \"To,Cc\" { tag \"Priority\"; }");
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);

  List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(new OperationContext(mbox), mbox,
    new ParsedMessage("To: test@zimbra.com".getBytes(), false), 0, account.getName(),
    new DeliveryContext(), Mailbox.ID_FOLDER_INBOX, true);
  assertEquals(1, ids.size());
  Message msg = mbox.getMessageById(null, ids.get(0).getId());
  assertEquals("Priority", ArrayUtil.getFirstElement(msg.getTags()));

  ids = RuleManager.applyRulesToIncomingMessage(new OperationContext(mbox), mbox,
    new ParsedMessage("Cc: test@zimbra.com".getBytes(), false), 0, account.getName(),
    new DeliveryContext(), Mailbox.ID_FOLDER_INBOX, true);
  assertEquals(1, ids.size());
  msg = mbox.getMessageById(null, ids.get(0).getId());
  assertEquals("Priority", ArrayUtil.getFirstElement(msg.getTags()));
 }

 @Test
 void meInToOrCcMultiRecipient() throws Exception {
  
  RuleManager.clearCachedRules(account);

  account.setMailSieveScript("if me :in \"To,Cc\" { tag \"Priority\"; }");
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);

  List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(new OperationContext(mbox), mbox,
    new ParsedMessage("To: foo@zimbra.com, test@zimbra.com".getBytes(), false), 0, account.getName(),
    new DeliveryContext(), Mailbox.ID_FOLDER_INBOX, true);
  assertEquals(1, ids.size());
  Message msg = mbox.getMessageById(null, ids.get(0).getId());
  assertEquals("Priority", ArrayUtil.getFirstElement(msg.getTags()));

  ids = RuleManager.applyRulesToIncomingMessage(new OperationContext(mbox), mbox,
    new ParsedMessage("Cc: foo@zimbra.com, test@zimbra.com".getBytes(), false), 0, account.getName(),
    new DeliveryContext(), Mailbox.ID_FOLDER_INBOX, true);
  assertEquals(1, ids.size());
  msg = mbox.getMessageById(null, ids.get(0).getId());
  assertEquals("Priority", ArrayUtil.getFirstElement(msg.getTags()));
 }

 @Test
 void quotedMultiRecipientToOrCc() throws Exception {
  
  RuleManager.clearCachedRules(account);

  account.setMailSieveScript("if me :in \"To,Cc\" { tag \"Priority\"; }");
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);

  List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(new OperationContext(mbox), mbox,
    new ParsedMessage("To: \"bar, foo\" <foo@zimbra.com>, \"test user\" <test@zimbra.com>".getBytes(), false), 0, account.getName(),
    new DeliveryContext(), Mailbox.ID_FOLDER_INBOX, true);
  assertEquals(1, ids.size());
  Message msg = mbox.getMessageById(null, ids.get(0).getId());
  assertEquals("Priority", ArrayUtil.getFirstElement(msg.getTags()));

  ids = RuleManager.applyRulesToIncomingMessage(new OperationContext(mbox), mbox,
    new ParsedMessage("Cc: \"foo bar\" <foo@zimbra.com>, \"user, test\" <test@zimbra.com>".getBytes(), false), 0, account.getName(),
    new DeliveryContext(), Mailbox.ID_FOLDER_INBOX, true);
  assertEquals(1, ids.size());
  msg = mbox.getMessageById(null, ids.get(0).getId());
  assertEquals("Priority", ArrayUtil.getFirstElement(msg.getTags()));
 }

}
