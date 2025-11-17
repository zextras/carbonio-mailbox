// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.filter;

import static org.junit.jupiter.api.Assertions.*;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.common.util.ArrayUtil;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.filter.jsieve.LinkedInTest;
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
 * Unit test for {@link LinkedInTest}.
 *
 * @author ysasaki
 */
public final class LinkedInTestTest extends MailboxTestSuite {

  @Test
  void test() throws Exception {
    Account account = createAccount().create();
    RuleManager.clearCachedRules(account);
    account.setMailSieveScript("if linkedin { tag \"linkedin\"; }");
    Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);

    // connections@linkedin.com
    List<ItemId> ids =
        RuleManager.applyRulesToIncomingMessage(
            new OperationContext(mbox),
            mbox,
            new ParsedMessage(
                ("Sender: messages-noreply@bounce.linkedin.com\n"
                        + "From: LinkedIn Connections <connections@linkedin.com>")
                    .getBytes(),
                false),
            0,
            account.getName(),
            new DeliveryContext(),
            Mailbox.ID_FOLDER_INBOX,
            true);
    assertEquals(1, ids.size());
    Message msg = mbox.getMessageById(null, ids.get(0).getId());
    assertEquals("linkedin", ArrayUtil.getFirstElement(msg.getTags()));

    // deals
    ids =
        RuleManager.applyRulesToIncomingMessage(
            new OperationContext(mbox),
            mbox,
            new ParsedMessage(
                ("Sender: messages-noreply@bounce.linkedin.com\n"
                        + "From: Yuichi Sasaki via LinkedIn <member@linkedin.com>")
                    .getBytes(),
                false),
            0,
            account.getName(),
            new DeliveryContext(),
            Mailbox.ID_FOLDER_INBOX,
            true);
    assertEquals(1, ids.size());
    msg = mbox.getMessageById(null, ids.get(0).getId());
    assertEquals("linkedin", ArrayUtil.getFirstElement(msg.getTags()));
  }
}
