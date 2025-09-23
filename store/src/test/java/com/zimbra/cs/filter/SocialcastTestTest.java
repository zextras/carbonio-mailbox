// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.filter;

import static org.junit.jupiter.api.Assertions.*;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.common.util.ArrayUtil;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.filter.jsieve.SocialcastTest;
import com.zimbra.cs.mailbox.DeliveryContext;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.Message;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.mime.ParsedMessage;
import com.zimbra.cs.service.util.ItemId;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link SocialcastTest}.
 *
 * @author ysasaki
 */
public final class SocialcastTestTest extends MailboxTestSuite {

 @Test
 void test() throws Exception {
  Account account = createAccount().create();
  RuleManager.clearCachedRules(account);
  account.setMailSieveScript("if socialcast { tag \"socialcast\"; }");
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);

  // bulk from socialcast
  List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(new OperationContext(mbox), mbox,
    new ParsedMessage("From: do-not-reply@socialcast.com\nSubject: test".getBytes(), false),
    0, account.getName(), new DeliveryContext(), Mailbox.ID_FOLDER_INBOX, true);
  assertEquals(1, ids.size());
  Message msg = mbox.getMessageById(null, ids.get(0).getId());
  assertEquals(msg.getTags().length, 0);

  // notification from socialcast
  ids = RuleManager.applyRulesToIncomingMessage(new OperationContext(mbox), mbox, new ParsedMessage(
      "From: do-not-reply@socialcast.com\nReply-To: share@socialcast.com\nSubject: test".getBytes(), false),
    0, account.getName(), new DeliveryContext(), Mailbox.ID_FOLDER_INBOX, true);
  assertEquals(1, ids.size());
  msg = mbox.getMessageById(null, ids.get(0).getId());
  assertEquals("socialcast", ArrayUtil.getFirstElement(msg.getTags()));
 }

}
