// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.filter;

import static org.junit.jupiter.api.Assertions.*;

import com.zextras.mailbox.util.AccountUtil;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.mailbox.DeliveryContext;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.mailbox.Tag;
import com.zimbra.cs.mime.ParsedMessage;
import com.zimbra.cs.service.util.ItemId;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TagTest {

  @BeforeAll
  public static void init() throws Exception {
    MailboxTestUtil.initServer();
  }

  @BeforeEach
  public void setUp() throws Exception {
    MailboxTestUtil.clearData();
  }

 @Test
 void test() throws Exception {
  try {
   Account account = AccountUtil.createAccount();
   RuleManager.clearCachedRules(account);
   account.setMailSieveScript("tag \"Hello World\";");
   Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);

   List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(new OperationContext(mbox), mbox,
     new ParsedMessage("From: sender@zimbra.com\nSubject: test1".getBytes(), false),
     0, account.getName(), new DeliveryContext(), Mailbox.ID_FOLDER_INBOX, true);
   assertEquals(1, ids.size());
   Tag tag = mbox.getTagByName(null, "Hello World");
   assertTrue(tag.isListed());

   // Send one more message to verify that no exception occurs
   ids = RuleManager.applyRulesToIncomingMessage(new OperationContext(mbox), mbox,
     new ParsedMessage("From: sender@zimbra.com\nSubject: test2".getBytes(), false),
     0, account.getName(), new DeliveryContext(), Mailbox.ID_FOLDER_INBOX, true);
   assertEquals(1, ids.size());
   assertTrue(tag.isListed());
  } catch (Exception e) {
   e.printStackTrace();
   fail("No exception should be thrown");
  }
 }

 @Test
 void testMimeVariable() throws Exception {
  try {
   Account account = AccountUtil.createAccount();
   RuleManager.clearCachedRules(account);
   account.setMailSieveScript("require \"variables\"; tag \"${subject} World\";");
   Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);

   List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(new OperationContext(mbox), mbox,
     new ParsedMessage("From: sender@zimbra.com\nSubject: Hello".getBytes(), false),
     0, account.getName(), new DeliveryContext(), Mailbox.ID_FOLDER_INBOX, true);
   assertEquals(1, ids.size());
   Tag tag = mbox.getTagByName(null, "Hello World");
   assertTrue(tag.isListed());
  } catch (Exception e) {
   e.printStackTrace();
   fail("No exception should be thrown");
  }
 }
}
