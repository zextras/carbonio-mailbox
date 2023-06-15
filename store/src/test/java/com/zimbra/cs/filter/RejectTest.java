// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.filter;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.Rule;
import org.junit.jupiter.api.*;
import org.junit.rules.MethodRule;

import com.google.common.collect.Maps;

import static org.junit.jupiter.api.Assertions.*;

import com.zimbra.common.account.Key;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.lmtpserver.LmtpAddress;
import com.zimbra.cs.lmtpserver.LmtpEnvelope;
import com.zimbra.cs.mailbox.DeliveryContext;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.mailbox.Message;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.mime.ParsedMessage;
import com.zimbra.cs.service.mail.SendMsgTest.DirectInsertionMailboxManager;
import com.zimbra.cs.service.util.ItemId;
import com.zimbra.cs.util.ZTestWatchman;

public class RejectTest {

     public String testName;
    @Rule public MethodRule watchman = new ZTestWatchman();
    
    private static String sampleBaseMsg = "Received: from edge01e.zimbra.com ([127.0.0.1])\n"
            + "\tby localhost (edge01e.zimbra.com [127.0.0.1]) (amavisd-new, port 10032)\n"
            + "\twith ESMTP id DN6rfD1RkHD7; Fri, 24 Jun 2016 01:45:31 -0400 (EDT)\n"
            + "Received: from localhost (localhost [127.0.0.1])\n"
            + "\tby edge01e.zimbra.com (Postfix) with ESMTP id 9245B13575C;\n"
            + "\tFri, 24 Jun 2016 01:45:31 -0400 (EDT)\n"
            + "from: testRej2@zimbra.com\n"
            + "Subject: example\n"
            + "to: testRej@zimbra.com\n";

    // RFC 5429 2.2.1
    private String filterScript = "require [\"reject\"];\n"
            + "if header :contains \"from\" \"testRej2@zimbra.com\" {\n"
            + "  reject text:\r\n"
            + "I am not taking mail from you, and I don’t\n"
            + "want your birdseed, either!\r\n"
            + ".\r\n"
            + "  ;\n"
            + "}";

    @BeforeAll
    public static void init() throws Exception {
        MailboxTestUtil.initServer();

        // this MailboxManager does everything except actually send mail
        MailboxManager.setInstance(new DirectInsertionMailboxManager());

    }

 @BeforeEach
 public void setUp(TestInfo testInfo) throws Exception {
  Optional<Method> testMethod = testInfo.getTestMethod();
  if (testMethod.isPresent()) {
   this.testName = testMethod.get().getName();
  }
  System.out.println( testName);
  Provisioning prov = Provisioning.getInstance();
  Map<String, Object> attrs = Maps.newHashMap();

  attrs = Maps.newHashMap();
  attrs.put(Provisioning.A_zimbraId, UUID.randomUUID().toString());
  attrs.put(Provisioning.A_zimbraSieveRejectMailEnabled, "TRUE");
  prov.createAccount("testRej@zimbra.com", "secret", attrs);

  attrs = Maps.newHashMap();
  attrs.put(Provisioning.A_zimbraId, UUID.randomUUID().toString());
  attrs.put(Provisioning.A_zimbraSieveRejectMailEnabled, "TRUE");
  prov.createAccount("testRej2@zimbra.com", "secret", attrs);

 }

    /*
     * MDN should be sent to the envelope from (testRej2@zimbra.com)
     */
    @Disabled /*Bug ZCS-1708 */
    public void testNotemptyEnvelopeFrom() {
        try {
            Account acct1 = Provisioning.getInstance().get(Key.AccountBy.name, "testRej@zimbra.com");
            Account acct2 = Provisioning.getInstance().get(Key.AccountBy.name, "testRej2@zimbra.com");

            Mailbox mbox1 = MailboxManager.getInstance().getMailboxByAccount(acct1);
            Mailbox mbox2 = MailboxManager.getInstance().getMailboxByAccount(acct2);

            RuleManager.clearCachedRules(acct1);

            LmtpEnvelope env = new LmtpEnvelope();
            LmtpAddress sender = new LmtpAddress("<testRej2@zimbra.com>", new String[] { "BODY", "SIZE" }, null);
            LmtpAddress recipient = new LmtpAddress("<testRej@zimbra.com>", null, null);
            env.setSender(sender);
            env.addLocalRecipient(recipient);

            acct1.setMailSieveScript(filterScript);
            List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(
                    new OperationContext(mbox1), mbox1, new ParsedMessage(
                            sampleBaseMsg.getBytes(), false), 0, acct1.getName(),
                            env, new DeliveryContext(),
                            Mailbox.ID_FOLDER_INBOX, true);
            assertEquals(0, ids.size());
            Integer item = mbox2.getItemIds(null, Mailbox.ID_FOLDER_INBOX)
                    .getIds(MailItem.Type.MESSAGE).get(0);
            Message mdnMsg = mbox2.getMessageById(null, item);
            String ctStr = mdnMsg.getMimeMessage().getContentType().toLowerCase();
            boolean isReport = ctStr.startsWith("multipart/report;");
            boolean isMdn = ctStr.indexOf("report-type=disposition-notification") < 0 ? false : true;
            assertEquals(isReport & isMdn, true);
        } catch (Exception e) {
            fail("No exception should be thrown: " + e.getMessage());
        }
    }

 /*
  * MDN should be sent to the envelope from (testRej2@zimbra.com)
  */
 @Test
 void testNotemptyEnvelopeFromAndUsingVariables() {

  try {
   filterScript = "require [\"log\", \"variables\", \"envelope\" , \"reject\"];\n"
     + "if envelope :matches [\"To\"] \"*\" {"
     + "set \"rcptto\" \"hello\";}\n"
     + "if header :matches [\"From\"] \"*\" {"
     + "set \"fromheader\" \"testRej2@zimbra.com\";}\n"
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
   Account acct1 = Provisioning.getInstance().get(Key.AccountBy.name, "testRej@zimbra.com");
   Account acct2 = Provisioning.getInstance().get(Key.AccountBy.name, "testRej2@zimbra.com");

   Mailbox mbox1 = MailboxManager.getInstance().getMailboxByAccount(acct1);
   Mailbox mbox2 = MailboxManager.getInstance().getMailboxByAccount(acct2);

   RuleManager.clearCachedRules(acct1);

   LmtpEnvelope env = new LmtpEnvelope();
   LmtpAddress sender = new LmtpAddress("<testRej2@zimbra.com>", new String[]{"BODY", "SIZE"}, null);
   LmtpAddress recipient = new LmtpAddress("<testRej@zimbra.com>", null, null);
   env.setSender(sender);
   env.addLocalRecipient(recipient);
//            String raw = "From: sender@in.telligent.com\n" 
//   					+ "To: coyote@ACME.Example.COM\n"
//   					+ "Subject: test\n" + "\n" + "Hello World.";

   acct1.setMailSieveScript(filterScript);
   acct1.setMail("testRej@zimbra.com");
   List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(
     new OperationContext(mbox1), mbox1, new ParsedMessage(
       sampleBaseMsg.getBytes(), false), 0, acct1.getName(),
     env, new DeliveryContext(),
     Mailbox.ID_FOLDER_INBOX, true);
   assertEquals(0, ids.size());

   Integer item = mbox2.getItemIds(null, Mailbox.ID_FOLDER_INBOX)
     .getIds(MailItem.Type.MESSAGE).get(0);
   Message mdnMsg = mbox2.getMessageById(null, item);
   String ctStr = mdnMsg.getMimeMessage().getContentType().toLowerCase();
   boolean isReport = ctStr.startsWith("multipart/report;");
   boolean isMdn = ctStr.indexOf("report-type=disposition-notification") < 0 ? false : true;
   assertEquals(isReport & isMdn, true);
  } catch (Exception e) {
   e.printStackTrace();
   fail("No exception should be thrown: " + e.getMessage());
  }
 }

 /*
  * MDN should be sent to the return-path from (testRej2@zimbra.com)
  */
 @Test
 void testEmptyEnvelopeFrom() {
  String sampleMsg = "Return-Path: testRej2@zimbra.com\n" + sampleBaseMsg;

  try {
   Account acct1 = Provisioning.getInstance().get(Key.AccountBy.name, "testRej@zimbra.com");
   Account acct2 = Provisioning.getInstance().get(Key.AccountBy.name, "testRej2@zimbra.com");

   Mailbox mbox1 = MailboxManager.getInstance().getMailboxByAccount(acct1);
   Mailbox mbox2 = MailboxManager.getInstance().getMailboxByAccount(acct2);

   RuleManager.clearCachedRules(acct1);

   LmtpEnvelope env = new LmtpEnvelope();
   LmtpAddress sender = new LmtpAddress("<>", new String[]{"BODY", "SIZE"}, null);
   LmtpAddress recipient = new LmtpAddress("<testRej@zimbra.com>", null, null);
   env.setSender(sender);
   env.addLocalRecipient(recipient);

   acct1.setMailSieveScript(filterScript);
   List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(
     new OperationContext(mbox1), mbox1, new ParsedMessage(
       sampleMsg.getBytes(), false), 0, acct1.getName(),
     env, new DeliveryContext(),
     Mailbox.ID_FOLDER_INBOX, true);
   assertEquals(0, ids.size());
   Integer item = mbox2.getItemIds(null, Mailbox.ID_FOLDER_INBOX)
     .getIds(MailItem.Type.MESSAGE).get(0);
   Message mdnMsg = mbox2.getMessageById(null, item);
   String ctStr = mdnMsg.getMimeMessage().getContentType().toLowerCase();
   boolean isReport = ctStr.startsWith("multipart/report;");
   boolean isMdn = ctStr.indexOf("report-type=disposition-notification") < 0 ? false : true;
   assertEquals(isReport & isMdn, true);
  } catch (Exception e) {
   fail("No exception should be thrown: " + e.getMessage());
  }
 }

 /*
  * MDN should not to be sent, and the message should be delivered to testRej@zimbra.com
  *
  * The following exception will be thrown:
  * javax.mail.MessagingException: Neither 'envelope from' nor 'Return-Path' specified. Can't locate the address to reject to (No MDN sent)
  */
 @Test
 void testEmptyEnvelopeFromAndEmptyReturnPath() {
  try {
   Account acct1 = Provisioning.getInstance().get(Key.AccountBy.name, "testRej@zimbra.com");
   Account acct2 = Provisioning.getInstance().get(Key.AccountBy.name, "testRej2@zimbra.com");

   Mailbox mbox1 = MailboxManager.getInstance().getMailboxByAccount(acct1);
   Mailbox mbox2 = MailboxManager.getInstance().getMailboxByAccount(acct2);

   RuleManager.clearCachedRules(acct1);

   LmtpEnvelope env = new LmtpEnvelope();
   LmtpAddress sender = new LmtpAddress("<>", new String[]{"BODY", "SIZE"}, null);
   LmtpAddress recipient = new LmtpAddress("<testRej@zimbra.com>", null, null);
   env.setSender(sender);
   env.addLocalRecipient(recipient);

   acct1.setMailSieveScript(filterScript);
   List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(
     new OperationContext(mbox1), mbox1, new ParsedMessage(
       sampleBaseMsg.getBytes(), false), 0, acct1.getName(),
     env, new DeliveryContext(),
     Mailbox.ID_FOLDER_INBOX, true);
   assertEquals(1, ids.size());
   List<Integer> items = mbox2.getItemIds(null, Mailbox.ID_FOLDER_INBOX)
     .getIds(MailItem.Type.MESSAGE);
   assertNull(items);
  } catch (Exception e) {
   fail("No exception should be thrown: " + e.getMessage());
  }
 }

 @Test
 void testSieveRejectEnabledIsFalse() {
  try {
   Account acct1 = Provisioning.getInstance().get(Key.AccountBy.name, "testRej@zimbra.com");
   acct1.setSieveRejectMailEnabled(false);

   Mailbox mbox1 = MailboxManager.getInstance().getMailboxByAccount(acct1);

   RuleManager.clearCachedRules(acct1);

   LmtpEnvelope env = new LmtpEnvelope();
   LmtpAddress sender = new LmtpAddress("<testRej2@zimbra.com>", new String[]{"BODY", "SIZE"}, null);
   LmtpAddress recipient = new LmtpAddress("<testRej@zimbra.com>", null, null);
   env.setSender(sender);
   env.addLocalRecipient(recipient);

   acct1.setMailSieveScript(filterScript);
   List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(
     new OperationContext(mbox1), mbox1, new ParsedMessage(
       sampleBaseMsg.getBytes(), false), 0, acct1.getName(),
     env, new DeliveryContext(),
     Mailbox.ID_FOLDER_INBOX, true);
   assertEquals(1, ids.size());
   Integer item = mbox1.getItemIds(null, Mailbox.ID_FOLDER_INBOX)
     .getIds(MailItem.Type.MESSAGE).get(0);
   Message msg = mbox1.getMessageById(null, item);
   assertEquals("example", msg.getSubject());
  } catch (Exception e) {
   fail("No exception should be thrown: " + e.getMessage());
  }
 }
    
    @AfterEach
    public void tearDown() {
        try {
            MailboxTestUtil.clearData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
