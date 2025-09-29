// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.filter;

import static org.junit.jupiter.api.Assertions.*;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.common.filter.Sieve.Flag;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.mailbox.DeliveryContext;
import com.zimbra.cs.mailbox.Flag.FlagInfo;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.Message;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.mime.ParsedMessage;
import com.zimbra.cs.service.util.ItemId;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link Flag}.
 *
 * @author ysasaki
 */
public final class FlagTest extends MailboxTestSuite {

  public String testName;

  @Test
  void priority() throws Exception {
    Account account = createAccount().create();
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
    assertEquals(1, ids.size());
    Message msg = mbox.getMessageById(null, ids.get(0).getId());
    assertTrue(msg.isTagged(FlagInfo.PRIORITY));
  }
}
