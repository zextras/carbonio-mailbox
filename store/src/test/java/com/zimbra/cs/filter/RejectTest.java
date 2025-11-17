// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only
// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.filter;

import static org.junit.jupiter.api.Assertions.*;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.lmtpserver.LmtpAddress;
import com.zimbra.cs.lmtpserver.LmtpEnvelope;
import com.zimbra.cs.mailbox.DeliveryContext;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.Message;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.mime.ParsedMessage;
import com.zimbra.cs.service.mail.DirectInsertionMailboxManager;
import com.zimbra.cs.service.util.ItemId;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.*;

public class RejectTest extends MailboxTestSuite {

  private static final String SAMPLE_BASE_MSG =
      "Received: from edge01e.zimbra.com ([127.0.0.1])\n"
          + "\tby localhost (edge01e.zimbra.com [127.0.0.1]) (amavisd-new, port 10032)\n"
          + "\twith ESMTP id DN6rfD1RkHD7; Fri, 24 Jun 2016 01:45:31 -0400 (EDT)\n"
          + "Received: from localhost (localhost [127.0.0.1])\n"
          + "\tby edge01e.zimbra.com (Postfix) with ESMTP id 9245B13575C;\n"
          + "\tFri, 24 Jun 2016 01:45:31 -0400 (EDT)\n"
          + "from: testRej2@zimbra.com\n"
          + "Subject: example\n"
          + "to: testRej@zimbra.com\n";

  // RFC 5429 2.2.1
  private static final String FILTER_SCRIPT =
      "require [\"reject\"];\n"
          + "if header :contains \"from\" \"testRej2@zimbra.com\" {\n"
          + "  reject text:\r\n"
          + "I am not taking mail from you, and I don't\n"
          + "want your birdseed, either!\r\n"
          + ".\r\n"
          + "  ;\n"
          + "}";

  private Account acct1;
  private Account acct2;
  private Mailbox mbox1;
  private Mailbox mbox2;

  @BeforeAll
  public static void init() throws Exception {
    final Provisioning provisioning = Provisioning.getInstance();
    provisioning.createDomain("zimbra.com", new HashMap<>());

    // this MailboxManager does everything except actually send mail
    MailboxManager.setInstance(new DirectInsertionMailboxManager());
  }

  @BeforeEach
  public void setUp() throws Exception {
    // Create fresh accounts for each test
    String testId = UUID.randomUUID().toString().substring(0, 8);

    acct1 =
        createAccount()
            .withDomain("zimbra.com")
            .withUsername("testRej_" + testId)
            .withPassword("secret")
            .withAttribute(Provisioning.A_zimbraId, UUID.randomUUID().toString())
            .withAttribute(Provisioning.A_zimbraSieveRejectMailEnabled, "TRUE")
            .create();

    acct2 =
        createAccount()
            .withDomain("zimbra.com")
            .withUsername("testRej2_" + testId)
            .withPassword("secret")
            .withAttribute(Provisioning.A_zimbraId, UUID.randomUUID().toString())
            .withAttribute(Provisioning.A_zimbraSieveRejectMailEnabled, "TRUE")
            .create();

    mbox1 = MailboxManager.getInstance().getMailboxByAccount(acct1);
    mbox2 = MailboxManager.getInstance().getMailboxByAccount(acct2);

    // Clear any cached rules
    RuleManager.clearCachedRules(acct1);
  }

  @AfterEach
  public void tearDown() throws Exception {

    if (acct1 != null) {
      try {
        Provisioning.getInstance().deleteAccount(acct1.getId());
      } catch (Exception e) {
        // Ignore cleanup errors
      }
    }
    if (acct2 != null) {
      try {
        Provisioning.getInstance().deleteAccount(acct2.getId());
      } catch (Exception e) {
        // Ignore cleanup errors
      }
    }
  }

  private LmtpEnvelope createEnvelope(String sender, String recipient) {
    LmtpEnvelope env = new LmtpEnvelope();
    LmtpAddress senderAddr = new LmtpAddress(sender, new String[] {"BODY", "SIZE"}, null);
    LmtpAddress recipientAddr = new LmtpAddress(recipient, null, null);
    env.setSender(senderAddr);
    env.addLocalRecipient(recipientAddr);
    return env;
  }

  private String createMessageWithCustomFrom(String fromAddress) {
    return SAMPLE_BASE_MSG.replace("from: testRej2@zimbra.com", "from: " + fromAddress);
  }

  /*
   * MDN should be sent to the envelope from (testRej2@zimbra.com)
   */
  @Test
  void testNotEmptyEnvelopeFrom() throws Exception {
    // Use account-specific addresses
    String sender = "<" + acct2.getName() + ">";
    String recipient = "<" + acct1.getName() + ">";

    LmtpEnvelope env = createEnvelope(sender, recipient);

    // Update filter script to use the specific test account
    String accountSpecificScript = FILTER_SCRIPT.replace("testRej2@zimbra.com", acct2.getName());

    acct1.setMailSieveScript(accountSpecificScript);

    List<ItemId> ids =
        RuleManager.applyRulesToIncomingMessage(
            new OperationContext(mbox1),
            mbox1,
            new ParsedMessage(createMessageWithCustomFrom(acct2.getName()).getBytes(), false),
            0,
            acct1.getName(),
            env,
            new DeliveryContext(),
            Mailbox.ID_FOLDER_INBOX,
            true);

    assertEquals(0, ids.size(), "Message should have been rejected");

    // Verify MDN was sent
    List<Integer> items =
        mbox2.getItemIds(null, Mailbox.ID_FOLDER_INBOX).getIds(MailItem.Type.MESSAGE);
    assertNotNull(items, "MDN should have been sent");
    assertEquals(1, items.size(), "Should have one MDN message");

    Message mdnMsg = mbox2.getMessageById(null, items.get(0));
    String ctStr = mdnMsg.getMimeMessage().getContentType().toLowerCase();
    boolean isReport = ctStr.startsWith("multipart/report;");
    boolean isMdn = ctStr.contains("report-type=disposition-notification");
    assertTrue(isReport && isMdn, "Should be a disposition notification report");
  }

  /*
   * MDN should be sent to the envelope from (testRej2@zimbra.com)
   */
  @Test
  void testNotEmptyEnvelopeFromAndUsingVariables() throws Exception {
    String sender = "<" + acct2.getName() + ">";
    String recipient = "<" + acct1.getName() + ">";

    String filterScript =
        "require [\"log\", \"variables\", \"envelope\" , \"reject\"];\n"
            + "if envelope :matches [\"To\"] \"*\" {"
            + "set \"rcptto\" \"hello\";}\n"
            + "if header :matches [\"From\"] \"*\" {"
            + "set \"fromheader\" \""
            + acct2.getName()
            + "\";}\n" // Use specific account
            + "if header :matches [\"Subject\"] \"*\" {"
            + "set \"subjectheader\" \"New Subject\";}\n"
            + "set \"bodyparam\" text: # This is a comment\r\n"
            + "Message delivered to  ${rcptto}\n"
            + "Sent : ${fromheader}\n"
            + "Subject : ${subjectheader} \n"
            + ".\r\n"
            + ";\n"
            + "log \"Subject: ${subjectheader}\"; \n"
            + "reject \"${bodyparam}\"; \n";

    LmtpEnvelope env = createEnvelope(sender, recipient);

    acct1.setMailSieveScript(filterScript);
    acct1.setMail(acct1.getName()); // Use account's own email

    List<ItemId> ids =
        RuleManager.applyRulesToIncomingMessage(
            new OperationContext(mbox1),
            mbox1,
            new ParsedMessage(createMessageWithCustomFrom(acct2.getName()).getBytes(), false),
            0,
            acct1.getName(),
            env,
            new DeliveryContext(),
            Mailbox.ID_FOLDER_INBOX,
            true);

    assertEquals(0, ids.size(), "Message should have been rejected");

    // Verify MDN was sent
    List<Integer> items =
        mbox2.getItemIds(null, Mailbox.ID_FOLDER_INBOX).getIds(MailItem.Type.MESSAGE);
    assertNotNull(items, "MDN should have been sent");
    assertEquals(1, items.size(), "Should have one MDN message");

    Message mdnMsg = mbox2.getMessageById(null, items.get(0));
    String ctStr = mdnMsg.getMimeMessage().getContentType().toLowerCase();
    boolean isReport = ctStr.startsWith("multipart/report;");
    boolean isMdn = ctStr.contains("report-type=disposition-notification");
    assertTrue(isReport && isMdn, "Should be a disposition notification report");
  }

  /*
   * MDN should be sent to the return-path from (testRej2@zimbra.com)
   */
  @Test
  void testEmptyEnvelopeFrom() throws Exception {
    String sampleMsg =
        "Return-Path: " + acct2.getName() + "\n" + createMessageWithCustomFrom(acct2.getName());

    String recipient = "<" + acct1.getName() + ">";
    LmtpEnvelope env = createEnvelope("<>", recipient);

    // Update filter script to use the specific test account
    String accountSpecificScript = FILTER_SCRIPT.replace("testRej2@zimbra.com", acct2.getName());

    acct1.setMailSieveScript(accountSpecificScript);

    List<ItemId> ids =
        RuleManager.applyRulesToIncomingMessage(
            new OperationContext(mbox1),
            mbox1,
            new ParsedMessage(sampleMsg.getBytes(), false),
            0,
            acct1.getName(),
            env,
            new DeliveryContext(),
            Mailbox.ID_FOLDER_INBOX,
            true);

    assertEquals(0, ids.size(), "Message should have been rejected");

    // Verify MDN was sent
    List<Integer> items =
        mbox2.getItemIds(null, Mailbox.ID_FOLDER_INBOX).getIds(MailItem.Type.MESSAGE);
    assertNotNull(items, "MDN should have been sent");
    assertEquals(1, items.size(), "Should have one MDN message");

    Message mdnMsg = mbox2.getMessageById(null, items.get(0));
    String ctStr = mdnMsg.getMimeMessage().getContentType().toLowerCase();
    boolean isReport = ctStr.startsWith("multipart/report;");
    boolean isMdn = ctStr.contains("report-type=disposition-notification");
    assertTrue(isReport && isMdn, "Should be a disposition notification report");
  }

  /*
   * MDN should not to be sent, and the message should be delivered to testRej@zimbra.com
   *
   * The following exception will be thrown:
   * javax.mail.MessagingException: Neither 'envelope from' nor 'Return-Path' specified. Can't locate the address to reject to (No MDN sent)
   */
  @Test
  void testEmptyEnvelopeFromAndEmptyReturnPath() throws Exception {
    String recipient = "<" + acct1.getName() + ">";
    LmtpEnvelope env = createEnvelope("<>", recipient);

    // Update filter script to use the specific test account
    String accountSpecificScript = FILTER_SCRIPT.replace("testRej2@zimbra.com", acct2.getName());

    acct1.setMailSieveScript(accountSpecificScript);

    List<ItemId> ids =
        RuleManager.applyRulesToIncomingMessage(
            new OperationContext(mbox1),
            mbox1,
            new ParsedMessage(createMessageWithCustomFrom(acct2.getName()).getBytes(), false),
            0,
            acct1.getName(),
            env,
            new DeliveryContext(),
            Mailbox.ID_FOLDER_INBOX,
            true);

    // Message should be delivered since MDN cannot be sent
    assertEquals(1, ids.size(), "Message should have been delivered");

    // Verify no MDN was sent
    List<Integer> items =
        mbox2.getItemIds(null, Mailbox.ID_FOLDER_INBOX).getIds(MailItem.Type.MESSAGE);
    assertTrue(items == null || items.isEmpty(), "No MDN should have been sent");

    // Verify message was delivered to recipient
    List<Integer> recipientItems =
        mbox1.getItemIds(null, Mailbox.ID_FOLDER_INBOX).getIds(MailItem.Type.MESSAGE);
    assertNotNull(recipientItems, "Message should be in recipient's inbox");
    assertEquals(1, recipientItems.size(), "Should have one message in inbox");

    Message msg = mbox1.getMessageById(null, recipientItems.get(0));
    assertEquals("example", msg.getSubject(), "Subject should match");
  }

  @Test
  void testSieveRejectEnabledIsFalse() throws Exception {
    String sender = "<" + acct2.getName() + ">";
    String recipient = "<" + acct1.getName() + ">";

    LmtpEnvelope env = createEnvelope(sender, recipient);

    // Disable reject for this specific test
    acct1.setSieveRejectMailEnabled(false);

    // Update filter script to use the specific test account
    String accountSpecificScript = FILTER_SCRIPT.replace("testRej2@zimbra.com", acct2.getName());

    acct1.setMailSieveScript(accountSpecificScript);

    List<ItemId> ids =
        RuleManager.applyRulesToIncomingMessage(
            new OperationContext(mbox1),
            mbox1,
            new ParsedMessage(createMessageWithCustomFrom(acct2.getName()).getBytes(), false),
            0,
            acct1.getName(),
            env,
            new DeliveryContext(),
            Mailbox.ID_FOLDER_INBOX,
            true);

    // Message should be delivered since reject is disabled
    assertEquals(1, ids.size(), "Message should have been delivered");

    // Verify message was delivered
    List<Integer> items =
        mbox1.getItemIds(null, Mailbox.ID_FOLDER_INBOX).getIds(MailItem.Type.MESSAGE);
    assertNotNull(items, "Message should be in inbox");
    assertEquals(1, items.size(), "Should have one message in inbox");

    Message msg = mbox1.getMessageById(null, items.get(0));
    assertEquals("example", msg.getSubject(), "Subject should match");

    // Verify no MDN was sent
    List<Integer> mdnItems =
        mbox2.getItemIds(null, Mailbox.ID_FOLDER_INBOX).getIds(MailItem.Type.MESSAGE);
    assertTrue(mdnItems == null || mdnItems.isEmpty(), "No MDN should have been sent");
  }
}
