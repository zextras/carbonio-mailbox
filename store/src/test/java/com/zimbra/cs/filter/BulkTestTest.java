// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.filter;

import static com.zimbra.cs.filter.jsieve.BulkTest.AUTO_REPLIED_ZIMBRA_READ_RECEIPT;
import static org.junit.jupiter.api.Assertions.*;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.common.util.ArrayUtil;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.filter.jsieve.BulkTest;
import com.zimbra.cs.mailbox.DeliveryContext;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.Message;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.mime.ParsedMessage;
import com.zimbra.cs.service.util.ItemId;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link BulkTest}.
 *
 * @author ysasaki
 */
public final class BulkTestTest extends MailboxTestSuite {
    private static Account account;
    private static Mailbox mailbox;

    @BeforeAll
    public static void init() throws Exception {
        account = createAccount().create();
        RuleManager.clearCachedRules(account);
        account.setMailSieveScript("if bulk { tag \"bulk\"; }");
        mailbox = MailboxManager.getInstance().getMailboxByAccount(account);
    }

 @Test
 void precedence() throws Exception {
  List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(new OperationContext(mailbox), mailbox,
    new ParsedMessage("From: sender@zimbra.com\nPrecedence: bulk\nSubject: bulk".getBytes(), false),
    0, account.getName(), new DeliveryContext(), Mailbox.ID_FOLDER_INBOX, true);
  assertEquals(1, ids.size());
  Message msg = mailbox.getMessageById(null, ids.get(0).getId());
  assertEquals("bulk", ArrayUtil.getFirstElement(msg.getTags()));
 }

 @Test
 void zimbraOOO() throws Exception {
  //negative test; don't mark these as bulk
  List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(new OperationContext(mailbox), mailbox,
    new ParsedMessage("From: sender@zimbra.com\nPrecedence: bulk\nAuto-Submitted: auto-replied (zimbra; vacation)\nSubject: bulk".getBytes(), false),
    0, account.getName(), new DeliveryContext(), Mailbox.ID_FOLDER_INBOX, true);
  assertEquals(1, ids.size());
  Message msg = mailbox.getMessageById(null, ids.get(0).getId());
  assertNull(ArrayUtil.getFirstElement(msg.getTags()));
 }

  @Test
  void shouldFilterOutBulkReadReceiptDeliveryReport() throws Exception {
    List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(new OperationContext(mailbox), mailbox,
        new ParsedMessage(("From: sender@zimbra.com\nPrecedence: bulk\nAuto-Submitted:" + AUTO_REPLIED_ZIMBRA_READ_RECEIPT + "\nSubject: bulk").getBytes(), false),
        0, account.getName(), new DeliveryContext(), Mailbox.ID_FOLDER_INBOX, true);
    assertEquals(1, ids.size());
    Message msg = mailbox.getMessageById(null, ids.get(0).getId());
    assertNull(ArrayUtil.getFirstElement(msg.getTags()));
  }

 @Test
 void abuse() throws Exception {
  List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(new OperationContext(mailbox), mailbox,
    new ParsedMessage("From: sender@zimbra.com\nX-Report-Abuse: test\nSubject: bulk".getBytes(), false),
    0, account.getName(), new DeliveryContext(), Mailbox.ID_FOLDER_INBOX, true);
  assertEquals(1, ids.size());
  Message msg = mailbox.getMessageById(null, ids.get(0).getId());
  assertEquals("bulk", ArrayUtil.getFirstElement(msg.getTags()));
 }

 @Test
 void campaign() throws Exception {
  List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(new OperationContext(mailbox), mailbox,
    new ParsedMessage("From: sender@zimbra.com\nX-CampaignId: test\nSubject: bulk".getBytes(), false),
    0, account.getName(), new DeliveryContext(), Mailbox.ID_FOLDER_INBOX, true);
  assertEquals(1, ids.size());
  Message msg = mailbox.getMessageById(null, ids.get(0).getId());
  assertEquals("bulk", ArrayUtil.getFirstElement(msg.getTags()));
 }

 @Test
 void unsubscribe() throws Exception {
  List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(new OperationContext(mailbox), mailbox,
    new ParsedMessage("To: list@zimbra.com\nList-Unsubscribe: test\nSubject: bulk".getBytes(), false),
    0, account.getName(), new DeliveryContext(), Mailbox.ID_FOLDER_INBOX, true);
  assertEquals(1, ids.size());
  Message msg = mailbox.getMessageById(null, ids.get(0).getId());
  assertEquals(0, msg.getTags().length);

  ids = RuleManager.applyRulesToIncomingMessage(new OperationContext(mailbox), mailbox,
    new ParsedMessage(("To: " + account.getName() + "\nList-Unsubscribe: test\nSubject: bulk").getBytes(), false),
    0, account.getName(), new DeliveryContext(), Mailbox.ID_FOLDER_INBOX, true);
  assertEquals(1, ids.size());
  msg = mailbox.getMessageById(null, ids.get(0).getId());
  assertEquals("bulk", ArrayUtil.getFirstElement(msg.getTags()));
 }

 @Test
 void proofPoint() throws Exception {
  List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(new OperationContext(mailbox), mailbox,
    new ParsedMessage(("To: list@zimbra.com\nX-Proofpoint-Spam-Details: rule=tag_notspam policy=tag " +
      "score=0 spamscore=0 ipscore=0 suspectscore=49 phishscore=0 bulkscore=100 adultscore=0 " +
      "classifier=spam adjust=0 reason=mlx engine=6.0.2-1012030000 definitions=main-1108230088\n" +
      "Subject: bulk").getBytes(), false),
    0, account.getName(), new DeliveryContext(), Mailbox.ID_FOLDER_INBOX, true);
  assertEquals(1, ids.size());
  Message msg = mailbox.getMessageById(null, ids.get(0).getId());
  assertEquals("bulk", ArrayUtil.getFirstElement(msg.getTags()));
 }
}
