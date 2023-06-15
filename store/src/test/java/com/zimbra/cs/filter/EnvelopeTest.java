// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.filter;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.Rule;
import org.junit.jupiter.api.*;
import org.junit.rules.MethodRule;

import com.google.common.collect.Maps;

import static org.junit.jupiter.api.Assertions.*;

import com.zimbra.common.util.ArrayUtil;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.lmtpserver.LmtpAddress;
import com.zimbra.cs.lmtpserver.LmtpEnvelope;
import com.zimbra.cs.mailbox.DeliveryContext;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.mailbox.Message;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.mime.ParsedMessage;
import com.zimbra.cs.service.util.ItemId;
import com.zimbra.cs.util.ZTestWatchman;

public class EnvelopeTest {
    private static String sampleMsg =
              "from: tim@example.com\n"
            + "to: testEnv@zimbra.com\n"
            + "Subject: Example\n";

     public String testName;
    @Rule public MethodRule watchman = new ZTestWatchman();
    
    
    @BeforeAll
    public static void init() throws Exception {
        MailboxTestUtil.initServer();
        
    }

 @BeforeEach
 public void setUp(TestInfo testInfo) throws Exception {
  Optional<Method> testMethod = testInfo.getTestMethod();
  if (testMethod.isPresent()) {
   this.testName = testMethod.get().getName();
  }
  System.out.println( testName);
  Provisioning prov = Provisioning.getInstance();
  HashMap<String, Object> attrs = new HashMap<String, Object>();
  prov.createAccount("testEnv@zimbra.com", "secret", attrs);
  prov.createAccount("original@zimbra.com", "secret", attrs);
 }

 @Test
 void testFrom() {
  // RFC 5228 5.4. Test envelope example
  String filterScript = "require \"envelope\";\n"
    + "if envelope :all :is \"from\" \"tim@example.com\" {\n"
    + "discard;\n"
    + "}";

  LmtpEnvelope env = new LmtpEnvelope();
  LmtpAddress sender = new LmtpAddress("<tim@example.com>", new String[]{"BODY", "SIZE"}, null);
  LmtpAddress recipient = new LmtpAddress("<testEnv@zimbra.com>", null, null);
  env.setSender(sender);
  env.addLocalRecipient(recipient);

  try {
   Account account = Provisioning.getInstance().getAccountByName("testEnv@zimbra.com");
   RuleManager.clearCachedRules(account);
   Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(
     account);

   account.setMailSieveScript(filterScript);
   List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(
     new OperationContext(mbox), mbox,
     new ParsedMessage(sampleMsg.getBytes(), false), 0,
     account.getName(), env,
     new DeliveryContext(),
     Mailbox.ID_FOLDER_INBOX, true);
   assertEquals(0, ids.size());
  } catch (Exception e) {
   e.printStackTrace();
   fail("No exception should be thrown");
  }
 }

 @Test
 void testTo() {
  String filterScript = "require \"envelope\";\n"
    + "if envelope :all :is \"to\" \"testEnv@zimbra.com\" {\n"
    + "  tag \"To\";\n"
    + "}";

  LmtpEnvelope env = new LmtpEnvelope();
  LmtpAddress sender = new LmtpAddress("<tim@example.com>", new String[]{"BODY", "SIZE"}, null);
  LmtpAddress recipient = new LmtpAddress("<testEnv@zimbra.com>", null, null);
  env.setSender(sender);
  env.addLocalRecipient(recipient);

  try {
   Account account = Provisioning.getInstance().getAccountByName("testEnv@zimbra.com");
   RuleManager.clearCachedRules(account);
   Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(
     account);

   account.setMailSieveScript(filterScript);
   account.setMail("testEnv@zimbra.com");
   List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(
     new OperationContext(mbox), mbox,
     new ParsedMessage(sampleMsg.getBytes(), false), 0,
     account.getName(), env,
     new DeliveryContext(),
     Mailbox.ID_FOLDER_INBOX, true);
   assertEquals(1, ids.size());
   Message msg = mbox.getMessageById(null, ids.get(0).getId());
   assertEquals("To", ArrayUtil.getFirstElement(msg.getTags()));
  } catch (Exception e) {
   fail("No exception should be thrown");
  }
 }

 @Test
 void testTo_BccTo() {
  /*
   * RFC 5228 5.4.
   * ----
   * If the SMTP transaction involved several RCPT commands, only the data
   * from the RCPT command that caused delivery to this user is available
   * in the "to" part of the envelope.
   * ----
   * The bcc recipient (who is specified by RCPT command but not on the
   * message header) should not be matched by the 'envelope' test.
    */
  String filterScript = "require \"envelope\";\n"
    + "if envelope :all :is \"to\" \"bccTo@zimbra.com\" {\n"
    + "  tag \"Bcc To\";\n"
    + "}";

  LmtpEnvelope env = new LmtpEnvelope();
  LmtpAddress sender = new LmtpAddress("<tim@example.com>", new String[]{"BODY", "SIZE"}, null);
  env.setSender(sender);

  // To address
  LmtpAddress recipient = new LmtpAddress("<testEnv@zimbra.com>", null, null);
  env.addLocalRecipient(recipient);
  // Bcc address
  recipient = new LmtpAddress("<bccTo@zimbra.com>", null, null);
  env.addLocalRecipient(recipient);

  try {
   Account account = Provisioning.getInstance().getAccountByName("testEnv@zimbra.com");
   RuleManager.clearCachedRules(account);
   Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(
     account);

   account.setMailSieveScript(filterScript);
   List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(
     new OperationContext(mbox), mbox,
     new ParsedMessage(sampleMsg.getBytes(), false), 0,
     account.getName(), env,
     new DeliveryContext(),
     Mailbox.ID_FOLDER_INBOX, true);
   assertEquals(1, ids.size());
   Message msg = mbox.getMessageById(null, ids.get(0).getId());
   assertNull(ArrayUtil.getFirstElement(msg.getTags()));
  } catch (Exception e) {
   fail("No exception should be thrown");
  }
 }

 @Test
 void testMailFrom() {
  /*
   * Check 'ADDRESS-PART' and 'MATCH-TYPE' work
    */
  String filterScript = "require \"envelope\";\n"
    + "if envelope :domain :contains \"from\" \"example\" {\n"
    + "  tag \"From\";\n"
    + "}";

  LmtpEnvelope env = new LmtpEnvelope();
  LmtpAddress sender = new LmtpAddress("<tim@example.com>", new String[]{"BODY", "SIZE"}, null);
  LmtpAddress recipient = new LmtpAddress("<xyz@zimbra.com>", null, null);
  env.setSender(sender);
  env.addLocalRecipient(recipient);

  try {
   Account account = Provisioning.getInstance().getAccountByName("testEnv@zimbra.com");
   RuleManager.clearCachedRules(account);
   Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(
     account);

   account.setMailSieveScript(filterScript);
   List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(
     new OperationContext(mbox), mbox,
     new ParsedMessage(sampleMsg.getBytes(), false), 0,
     account.getName(), env,
     new DeliveryContext(),
     Mailbox.ID_FOLDER_INBOX, true);
   assertEquals(1, ids.size());
   Message msg = mbox.getMessageById(null, ids.get(0).getId());
   assertEquals("From", ArrayUtil.getFirstElement(msg.getTags()));
  } catch (Exception e) {
   fail("No exception should be thrown");
  }
 }

 @Test
 void testMailFromBackslash() {
  String filterScript = "require \"envelope\";\n"
    + "if envelope :all :is \"from\" \"ti\\\\m@example.com\" {\n"
    + "  tag \"From\";\n"
    + "}";

  LmtpEnvelope env = new LmtpEnvelope();
  LmtpAddress sender = new LmtpAddress("<\"ti\\\\m\"@example.com>", new String[]{"BODY", "SIZE"}, null);
  LmtpAddress recipient = new LmtpAddress("<xyz@zimbra.com>", null, null);
  env.setSender(sender);
  env.addLocalRecipient(recipient);

  try {
   Account account = Provisioning.getInstance().getAccountByName("testEnv@zimbra.com");
   RuleManager.clearCachedRules(account);
   Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(
     account);

   account.setMailSieveScript(filterScript);
   List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(
     new OperationContext(mbox), mbox,
     new ParsedMessage(sampleMsg.getBytes(), false), 0,
     account.getName(), env,
     new DeliveryContext(),
     Mailbox.ID_FOLDER_INBOX, true);
   assertEquals(1, ids.size());
   Message msg = mbox.getMessageById(null, ids.get(0).getId());
   assertEquals("From", ArrayUtil.getFirstElement(msg.getTags()));
  } catch (Exception e) {
   fail("No exception should be thrown");
  }
 }

 @Test
 void testMailFromDot() {
  String filterScript = "require \"envelope\";\n"
    + "if envelope :all :is \"from\" \"ti.m@example.com\" {\n"
    + "  tag \"From\";\n"
    + "}";

  LmtpEnvelope env = new LmtpEnvelope();
  LmtpAddress sender = new LmtpAddress("<ti.m@example.com>", new String[]{"BODY", "SIZE"}, null);
  LmtpAddress recipient = new LmtpAddress("<xyz@zimbra.com>", null, null);
  env.setSender(sender);
  env.addLocalRecipient(recipient);

  try {
   Account account = Provisioning.getInstance().getAccountByName("testEnv@zimbra.com");
   RuleManager.clearCachedRules(account);
   Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(
     account);

   account.setMailSieveScript(filterScript);
   List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(
     new OperationContext(mbox), mbox,
     new ParsedMessage(sampleMsg.getBytes(), false), 0,
     account.getName(), env,
     new DeliveryContext(),
     Mailbox.ID_FOLDER_INBOX, true);
   assertEquals(1, ids.size());
   Message msg = mbox.getMessageById(null, ids.get(0).getId());
   assertEquals("From", ArrayUtil.getFirstElement(msg.getTags()));
  } catch (Exception e) {
   fail("No exception should be thrown");
  }
 }

 @Test
 void testMailFromDoubleQuote() {
  String filterScript = "require \"envelope\";\n"
    + "if envelope :all :is \"from\" \"ti\\\"m@example.com\" {\n"
    + "  tag \"From\";\n"
    + "}";

  LmtpEnvelope env = new LmtpEnvelope();
  LmtpAddress sender = new LmtpAddress("<\"ti\\\"m\"@example.com>", new String[]{"BODY", "SIZE"}, null);
  LmtpAddress recipient = new LmtpAddress("<xyz@zimbra.com>", null, null);
  env.setSender(sender);
  env.addLocalRecipient(recipient);

  try {
   Account account = Provisioning.getInstance().getAccountByName("testEnv@zimbra.com");
   RuleManager.clearCachedRules(account);
   Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(
     account);

   account.setMailSieveScript(filterScript);
   List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(
     new OperationContext(mbox), mbox,
     new ParsedMessage(sampleMsg.getBytes(), false), 0,
     account.getName(), env,
     new DeliveryContext(),
     Mailbox.ID_FOLDER_INBOX, true);
   assertEquals(1, ids.size());
   Message msg = mbox.getMessageById(null, ids.get(0).getId());
   assertEquals("From", ArrayUtil.getFirstElement(msg.getTags()));
  } catch (Exception e) {
   fail("No exception should be thrown");
  }
 }

 @Test
 void testMailFromSingleQuote() {
  String filterScript = "require \"envelope\";\n"
    + "if envelope :all :is \"from\" \"ti'm@example.com\" {\n"
    + "  tag \"From\";\n"
    + "}";

  LmtpEnvelope env = new LmtpEnvelope();
  LmtpAddress sender = new LmtpAddress("<ti'm@example.com>", new String[]{"BODY", "SIZE"}, null);
  LmtpAddress recipient = new LmtpAddress("<xyz@zimbra.com>", null, null);
  env.setSender(sender);
  env.addLocalRecipient(recipient);

  try {
   Account account = Provisioning.getInstance().getAccountByName("testEnv@zimbra.com");
   RuleManager.clearCachedRules(account);
   Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(
     account);

   account.setMailSieveScript(filterScript);
   List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(
     new OperationContext(mbox), mbox,
     new ParsedMessage(sampleMsg.getBytes(), false), 0,
     account.getName(), env,
     new DeliveryContext(),
     Mailbox.ID_FOLDER_INBOX, true);
   assertEquals(1, ids.size());
   Message msg = mbox.getMessageById(null, ids.get(0).getId());
   assertEquals("From", ArrayUtil.getFirstElement(msg.getTags()));
  } catch (Exception e) {
   fail("No exception should be thrown");
  }
 }

 @Test
 void testMailFromQuestionMark() {
  String filterScript = "require \"envelope\";\n"
    + "if envelope :all :is \"from\" \"ti?m@example.com\" {\n"
    + "  tag \"From\";\n"
    + "}";

  LmtpEnvelope env = new LmtpEnvelope();
  LmtpAddress sender = new LmtpAddress("<ti?m@example.com>", new String[]{"BODY", "SIZE"}, null);
  LmtpAddress recipient = new LmtpAddress("<xyz@zimbra.com>", null, null);
  env.setSender(sender);
  env.addLocalRecipient(recipient);

  try {
   Account account = Provisioning.getInstance().getAccountByName("testEnv@zimbra.com");
   RuleManager.clearCachedRules(account);
   Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(
     account);

   account.setMailSieveScript(filterScript);
   List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(
     new OperationContext(mbox), mbox,
     new ParsedMessage(sampleMsg.getBytes(), false), 0,
     account.getName(), env,
     new DeliveryContext(),
     Mailbox.ID_FOLDER_INBOX, true);
   assertEquals(1, ids.size());
   Message msg = mbox.getMessageById(null, ids.get(0).getId());
   assertEquals("From", ArrayUtil.getFirstElement(msg.getTags()));
  } catch (Exception e) {
   fail("No exception should be thrown");
  }
 }

 @Test
 void testMailFromComma() {
  String filterScript = "require \"envelope\";\n"
    + "if envelope :all :is \"from\" \"ti,m@example.com\" {\n"
    + "  tag \"From\";\n"
    + "}";

  LmtpEnvelope env = new LmtpEnvelope();
  LmtpAddress sender = new LmtpAddress("<\"ti,m\"@example.com>", new String[]{"BODY", "SIZE"}, null);
  LmtpAddress recipient = new LmtpAddress("<xyz@zimbra.com>", null, null);
  env.setSender(sender);
  env.addLocalRecipient(recipient);

  try {
   Account account = Provisioning.getInstance().getAccountByName("testEnv@zimbra.com");
   RuleManager.clearCachedRules(account);
   Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(
     account);

   account.setMailSieveScript(filterScript);
   List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(
     new OperationContext(mbox), mbox,
     new ParsedMessage(sampleMsg.getBytes(), false), 0,
     account.getName(), env,
     new DeliveryContext(),
     Mailbox.ID_FOLDER_INBOX, true);
   assertEquals(1, ids.size());
   Message msg = mbox.getMessageById(null, ids.get(0).getId());
   assertEquals("From", ArrayUtil.getFirstElement(msg.getTags()));
  } catch (Exception e) {
   fail("No exception should be thrown");
  }
 }

    @Disabled
    public void testVariable1() {
        String filterScript = "require [\"variables\", \"envelope\"];\n"
                + "if envelope :matches [\"from\"] \"*\" {\n"
                + "  tag \"env_${1}\";\n"
                + "}\n"
                + "if envelope :matches [\"to\"] \"*\" {\n"
                + "  tag \"env_${1}\";\n"
                + "}\n"
                + "if address :matches :comparator \"i;ascii-casemap\" [\"from\"] \"*\" {\n"
                + "  tag \"adr_${1}\";\n"
                + "}\n"
                + "if address :matches :comparator \"i;ascii-casemap\" [\"to\"] \"*\" {\n"
                + "  tag \"adr_${1}\";\n"
                + "}\n"
                + "if header :matches [\"from\"] \"*\" {\n"
                + "  tag \"hdr_${1}\";\n"
                + "}\n"
                + "if header :matches [\"to\"] \"*\" {\n"
                + "  tag \"hdr_${1}\";\n"
                + "}\n";
        testVariable(filterScript);
    }

    /*
     * Once Bug 107044 is solved, this pattern should be tested instead testVariable1()
     */
    @Disabled
    public void testVariable2() {
        String filterScript = "require [\"variables\", \"envelope\"];\n"
                + "if envelope :matches [\"from\"] \"*\" {\n"
                + "  tag \"env_${1}\";\n"
                + "}\n"
                + "if envelope :matches [\"to\"] \"*\" {\n"
                + "  tag \"env_${1}\";\n"
                + "}\n"
                + "if address :matches [\"from\"] \"*\" {\n"
                + "  tag \"adr_${1}\";\n"
                + "}\n"
                + "if address :matches [\"to\"] \"*\" {\n"
                + "  tag \"adr_${1}\";\n"
                + "}\n"
                + "if header :matches [\"from\"] \"*\" {\n"
                + "  tag \"hdr_${1}\";\n"
                + "}\n"
                + "if header :matches [\"to\"] \"*\" {\n"
                + "  tag \"hdr_${1}\";\n"
                + "}\n";
        testVariable(filterScript);
    }

    public void testVariable(String filterScript) {
        /*
         * Checks if numeric variable works
         */
        String triggeringMsg =
                "from: message_header_from@example.com\n"
              + "to: message_header_to@zimbra.com\n"
              + "Subject: Example\n";

        String[] expectedTagName = {"env_envelope_from@example.com",
                                    "env_testEnv@zimbra.com",
                                    "adr_message_header_from@example.com",
                                    "adr_message_header_to@zimbra.com",
                                    "hdr_message_header_from@example.com",
                                    "hdr_message_header_to@zimbra.com"};

        LmtpEnvelope env = new LmtpEnvelope();
        LmtpAddress sender = new LmtpAddress("<envelope_from@example.com>", new String[] { "BODY", "SIZE" }, null);
        LmtpAddress recipient = new LmtpAddress("<testEnv@zimbra.com>", null, null);
        env.setSender(sender);
        env.addLocalRecipient(recipient);

        try {
            Account account = Provisioning.getInstance().getAccountByName("testEnv@zimbra.com");
            Map<String, Object> attrs = Maps.newHashMap();
            attrs = Maps.newHashMap();
            Provisioning.getInstance().getServer(account).modify(attrs);
            RuleManager.clearCachedRules(account);
            Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(
                    account);

            account.setMailSieveScript(filterScript);
            List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(
                    new OperationContext(mbox), mbox,
                    new ParsedMessage(triggeringMsg.getBytes(), false), 0,
                    account.getName(), env,
                    new DeliveryContext(),
                    Mailbox.ID_FOLDER_INBOX, true);
            assertEquals(1, ids.size());
            Message msg = mbox.getMessageById(null, ids.get(0).getId());
            String[] tags = msg.getTags();
            assertTrue(tags != null);
            assertEquals(expectedTagName.length, tags.length);
            for (int i = 0; i < expectedTagName.length; i++) {
                assertEquals(expectedTagName[i], tags[i]);
            }
        } catch (Exception e) {
            fail("No exception should be thrown");
        }
    }

 @Test
 void testMailFrom_nullReverse_path() {
  /*
   * RFC 5228 5.4.
   * ---
   * The null reverse-path is matched against as the empty
   * string, regardless of the ADDRESS-PART argument specified.
   * ---
    */
  String filterScript = "require \"envelope\";\n"
    + "if envelope :localpart :is \"from\" \"\" {\n"
    + "  tag \"NullMailFrom\";\n"
    + "}";

  LmtpEnvelope env = new LmtpEnvelope();
  LmtpAddress sender = new LmtpAddress("<>", new String[]{"BODY", "SIZE"}, null);
  LmtpAddress recipient = new LmtpAddress("<xyz@zimbra.com>", null, null);
  env.setSender(sender);
  env.addLocalRecipient(recipient);

  try {
   Account account = Provisioning.getInstance().getAccountByName("testEnv@zimbra.com");
   RuleManager.clearCachedRules(account);
   Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(
     account);

   account.setMailSieveScript(filterScript);
   List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(
     new OperationContext(mbox), mbox,
     new ParsedMessage(sampleMsg.getBytes(), false), 0,
     account.getName(), env,
     new DeliveryContext(),
     Mailbox.ID_FOLDER_INBOX, true);
   assertEquals(1, ids.size());
   Message msg = mbox.getMessageById(null, ids.get(0).getId());
   assertEquals("NullMailFrom", ArrayUtil.getFirstElement(msg.getTags()));
  } catch (Exception e) {
   fail("No exception should be thrown");
  }
 }

 @Test
 void testOutgoingFilter() {
  /*
   * As the envelope data is available only when the message is processed during the
   * LMTP session, the 'envelope' test always returns false.
    */
  String filterScript = "require \"envelope\";\n"
    + "if envelope :all :is \"from\" \"\" {\n"
    + "  tag \"outgoing\";\n"
    + "}";

  try {
   Account account = Provisioning.getInstance().getAccountByName("testEnv@zimbra.com");
   RuleManager.clearCachedRules(account);
   Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(
     account);

   account.setMailSieveScript(filterScript);
   List<ItemId> ids = RuleManager.applyRulesToOutgoingMessage(
     new OperationContext(mbox), mbox,
     new ParsedMessage(sampleMsg.getBytes(), false),
     5, /* sent folder */
     true, 0, null, Mailbox.ID_AUTO_INCREMENT);
   assertEquals(1, ids.size());
   Message msg = mbox.getMessageById(null, ids.get(0).getId());
   assertNull(ArrayUtil.getFirstElement(msg.getTags()));
  } catch (Exception e) {
   fail("No exception should be thrown");
  }
 }

 @Test
 void testCompareEmptyStringWithAsciiNumeric() {
  String filterScript = "require [\"envelope\", \"comparator-i;ascii-numeric\"];\n"
    + "if envelope :comparator \"i;ascii-numeric\" :all :is \"from\" \"\" {\n"
    + "  tag \"testCompareEmptyStringWithAsciiNumeric envelope\";"
    + "}"
    + "if header :comparator \"i;ascii-numeric\" :is \"from\" \"\" {\n"
    + "  tag \"testCompareEmptyStringWithAsciiNumeric header\";"
    + "}";

  LmtpEnvelope env = new LmtpEnvelope();
  LmtpAddress sender = new LmtpAddress("<tim@example.com>", new String[]{"BODY", "SIZE"}, null);
  LmtpAddress recipient = new LmtpAddress("<testEnv@zimbra.com>", null, null);
  env.setSender(sender);
  env.addLocalRecipient(recipient);

  try {
   Account account = Provisioning.getInstance().getAccountByName("testEnv@zimbra.com");
   RuleManager.clearCachedRules(account);
   Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);

   account.setMailSieveScript(filterScript);
   List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(
     new OperationContext(mbox), mbox,
     new ParsedMessage(sampleMsg.getBytes(), false), 0,
     account.getName(), env,
     new DeliveryContext(),
     Mailbox.ID_FOLDER_INBOX, true);
   assertEquals(1, ids.size());
   Message msg = mbox.getMessageById(null, ids.get(0).getId());

   String[] tags = msg.getTags();
   assertTrue(tags != null);
   assertEquals(2, tags.length);
   assertEquals("testCompareEmptyStringWithAsciiNumeric envelope", tags[0]);
   assertEquals("testCompareEmptyStringWithAsciiNumeric header", tags[1]);
  } catch (Exception e) {
   fail("No exception should be thrown" + e);
  }
 }

 @Test
 void testTo_Alias() {
  String filterScript = "require [\"variables\", \"envelope\", \"relational\", \"comparator-i;ascii-numeric\"];\n"
    + "set \"rcptto\" \"unknown\";\n"
    + "if envelope :all :matches \"to\" \"*\" {\n"
    + "  set \"rcptto\" \"${1}\";\n"
    + "  tag \"${rcptto}\";\n"
    + "}\n"
    + "if envelope :all :matches \"to\" \"alias1*\" {\n"
    + "  tag \"${1}\";\n"
    + "}\n"
    + "if envelope :all :matches \"to\" \"alias2*\" {\n"
    + "  tag \"bad\";\n"
    + "}\n"
    + "if envelope :count \"eq\" :comparator \"i;ascii-numeric\" \"to\" \"1\" {"
    + "  tag \"1\";\n"
    + "}";

  LmtpEnvelope env = new LmtpEnvelope();
  LmtpAddress sender = new LmtpAddress("<tim@example.com>", new String[]{"BODY", "SIZE"}, null);
  LmtpAddress recipient = new LmtpAddress("<alias1@zimbra.com>", null, null);
  env.setSender(sender);
  env.addLocalRecipient(recipient);

  try {
   Provisioning prov = Provisioning.getInstance();
   Account account = prov.createAccount("original1@zimbra.com", "secret", new HashMap<String, Object>());
   RuleManager.clearCachedRules(account);
   Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(
     account);

   account.setAdminSieveScriptBefore(filterScript);
   account.setMail("original1@zimbra.com");
   String[] alias = {"alias1@zimbra.com", "alias2@zimbra.com"};
   account.setMailAlias(alias);
   List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(
     new OperationContext(mbox), mbox,
     new ParsedMessage(sampleMsg.getBytes(), false), 0,
     account.getName(), env,
     new DeliveryContext(),
     Mailbox.ID_FOLDER_INBOX, true);
   assertEquals(1, ids.size());
   Message msg = mbox.getMessageById(null, ids.get(0).getId());

   String[] tags = msg.getTags();
   assertTrue(tags != null);
   assertEquals(3, tags.length);
   assertEquals("alias1@zimbra.com", tags[0]);
   assertEquals("@zimbra.com", tags[1]);
   assertEquals("1", tags[2]);
  } catch (Exception e) {
   fail("No exception should be thrown:" + e);
  }
 }

 @Test
 void testCountForEmptyFromHeader() {
  String filterScript = "require [\"envelope\", \"relational\", \"comparator-i;ascii-numeric\"];\n"
    + "if envelope :count \"eq\" :comparator \"i;ascii-numeric\" :all \"FROM\" \"0\" {\n"
    + "tag \"0\";\n"
    + "}\n"
    + "if envelope :all :matches \"from\" \"\" {\n"
    + "  tag \"empty\";\n"
    + "}\n"
    + "if envelope :count \"eq\" :comparator \"i;ascii-numeric\" :all \"to\" \"1\" {\n"
    + "tag \"1\";\n"
    + "}";

  LmtpEnvelope env = new LmtpEnvelope();
  LmtpAddress sender = new LmtpAddress("<>", new String[]{"BODY", "SIZE"}, null);
  LmtpAddress recipient = new LmtpAddress("<xyz@zimbra.com>", null, null);
  env.setSender(sender);
  env.addLocalRecipient(recipient);

  try {
   Provisioning prov = Provisioning.getInstance();
   Account account = prov.createAccount("xyz@zimbra.com", "secret", new HashMap<String, Object>());
   RuleManager.clearCachedRules(account);
   Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(
     account);

   account.setMailSieveScript(filterScript);
   List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(
     new OperationContext(mbox), mbox,
     new ParsedMessage(sampleMsg.getBytes(), false), 0,
     account.getName(), env,
     new DeliveryContext(),
     Mailbox.ID_FOLDER_INBOX, true);
   assertEquals(1, ids.size());
   Message msg = mbox.getMessageById(null, ids.get(0).getId());
   assertEquals("0", ArrayUtil.getFirstElement(msg.getTags()));
   assertEquals("empty", msg.getTags()[1]);
   assertEquals("1", msg.getTags()[2]);
  } catch (Exception e) {
   fail("No exception should be thrown");
  }
 }

 @Test
 void testNumericNegativeValueCount() {
  String filterScript = "require [\"envelope\", \"tag\", \"relational\", \"comparator-i;ascii-numeric\"];\n"
    + "if envelope :all :count \"lt\" :comparator \"i;ascii-numeric\" \"to\" \"-1\" {\n"
    + "  tag \"To\";\n"
    + "}";

  LmtpEnvelope env = new LmtpEnvelope();
  LmtpAddress sender = new LmtpAddress("<tim@example.com>", new String[]{"BODY", "SIZE"}, null);
  LmtpAddress recipient = new LmtpAddress("<testEnv@zimbra.com>", null, null);
  env.setSender(sender);
  env.addLocalRecipient(recipient);

  try {
   Account account = Provisioning.getInstance().getAccountByName("testEnv@zimbra.com");
   RuleManager.clearCachedRules(account);
   Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(
     account);

   account.setMailSieveScript(filterScript);
   account.setMail("testEnv@zimbra.com");
   List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(
     new OperationContext(mbox), mbox,
     new ParsedMessage(sampleMsg.getBytes(), false), 0,
     account.getName(), env,
     new DeliveryContext(),
     Mailbox.ID_FOLDER_INBOX, true);
   assertEquals(1, ids.size());
   Message msg = mbox.getMessageById(null, ids.get(0).getId());
   assertNull(ArrayUtil.getFirstElement(msg.getTags()));
  } catch (Exception e) {
   fail("No exception should be thrown");
  }
 }

 @Test
 void testHeaderNameWithLeadingSpace() {
  String filterScript = "require \"envelope\";\n"
    + "if envelope :matches \" TO\" \"*@zimbra.com\" {\n"
    + "    tag \"t1\";\n"
    + "}\n"
  ;
  LmtpEnvelope env = new LmtpEnvelope();
  LmtpAddress sender = new LmtpAddress("<>", new String[]{"BODY", "SIZE"}, null);
  LmtpAddress recipient = new LmtpAddress("<xyz@zimbra.com>", null, null);
  env.setSender(sender);
  env.addLocalRecipient(recipient);

  try {
   Provisioning prov = Provisioning.getInstance();
   Account account = prov.createAccount("xyz@zimbra.com", "secret", new HashMap<String, Object>());
   RuleManager.clearCachedRules(account);
   Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(
     account);

   account.setMailSieveScript(filterScript);
   List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(
     new OperationContext(mbox), mbox,
     new ParsedMessage(sampleMsg.getBytes(), false), 0,
     account.getName(), env,
     new DeliveryContext(),
     Mailbox.ID_FOLDER_INBOX, true);
   assertEquals(1, ids.size());
   Message msg = mbox.getMessageById(null, ids.get(0).getId());
   assertEquals(0, msg.getTags().length);
  } catch (Exception e) {
   fail("No exception should be thrown");
  }
 }

 @Test
 void testHeaderNameWithTrailingSpace() {
  String filterScript = "require \"envelope\";\n"
    + "if envelope :matches \"TO \" \"*@zimbra.com\" {\n"
    + "    tag \"t1\";\n"
    + "}\n"
  ;
  LmtpEnvelope env = new LmtpEnvelope();
  LmtpAddress sender = new LmtpAddress("<>", new String[]{"BODY", "SIZE"}, null);
  LmtpAddress recipient = new LmtpAddress("<xyz@zimbra.com>", null, null);
  env.setSender(sender);
  env.addLocalRecipient(recipient);

  try {
   Provisioning prov = Provisioning.getInstance();
   Account account = prov.createAccount("xyz@zimbra.com", "secret", new HashMap<String, Object>());
   RuleManager.clearCachedRules(account);
   Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(
     account);

   account.setMailSieveScript(filterScript);
   List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(
     new OperationContext(mbox), mbox,
     new ParsedMessage(sampleMsg.getBytes(), false), 0,
     account.getName(), env,
     new DeliveryContext(),
     Mailbox.ID_FOLDER_INBOX, true);
   assertEquals(1, ids.size());
   Message msg = mbox.getMessageById(null, ids.get(0).getId());
   assertEquals(0, msg.getTags().length);
  } catch (Exception e) {
   fail("No exception should be thrown");
  }
 }

 @Test
 void testHeaderNameWithLeadingAndTrailingSpace() {
  String filterScript = "require \"envelope\";\n"
    + "if envelope :matches \" TO \" \"*@zimbra.com\" {\n"
    + "    tag \"t1\";\n"
    + "}\n"
  ;
  LmtpEnvelope env = new LmtpEnvelope();
  LmtpAddress sender = new LmtpAddress("<>", new String[]{"BODY", "SIZE"}, null);
  LmtpAddress recipient = new LmtpAddress("<xyz@zimbra.com>", null, null);
  env.setSender(sender);
  env.addLocalRecipient(recipient);

  try {
   Provisioning prov = Provisioning.getInstance();
   Account account = prov.createAccount("xyz@zimbra.com", "secret", new HashMap<String, Object>());
   RuleManager.clearCachedRules(account);
   Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(
     account);

   account.setMailSieveScript(filterScript);
   List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(
     new OperationContext(mbox), mbox,
     new ParsedMessage(sampleMsg.getBytes(), false), 0,
     account.getName(), env,
     new DeliveryContext(),
     Mailbox.ID_FOLDER_INBOX, true);
   assertEquals(1, ids.size());
   Message msg = mbox.getMessageById(null, ids.get(0).getId());
   assertEquals(0, msg.getTags().length);
  } catch (Exception e) {
   fail("No exception should be thrown");
  }
 }

 @Test
 void testInvalidHeaderName() {
  String filterScript = "require  \"envelope\";\n"
    + "if anyof envelope :matches \"to123\" \"t1@zimbra.com\" {\n"
    + "    fileinto \"Junk\";\n"
    + "}\n"
  ;
  LmtpEnvelope env = new LmtpEnvelope();
  LmtpAddress sender = new LmtpAddress("<t1@zimbra.com>", new String[]{"BODY", "SIZE"}, null);
  LmtpAddress recipient = new LmtpAddress("<xyz@zimbra.com>", null, null);
  env.setSender(sender);
  env.addLocalRecipient(recipient);

  try {
   Provisioning prov = Provisioning.getInstance();
   Account account = prov.createAccount("xyz@zimbra.com", "secret", new HashMap<String, Object>());
   account.setMail("xyz@zimbra.com");
   RuleManager.clearCachedRules(account);
   Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(
     account);

   account.setMailSieveScript(filterScript);
   List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(
     new OperationContext(mbox), mbox,
     new ParsedMessage(sampleMsg.getBytes(), false), 0,
     account.getName(), env,
     new DeliveryContext(),
     Mailbox.ID_FOLDER_INBOX, true);
   assertEquals(1, ids.size());
   Message msg = mbox.getMessageById(null, ids.get(0).getId());
   assertEquals(Mailbox.ID_FOLDER_INBOX, msg.getFolderId());
  } catch (Exception e) {
   fail("No exception should be thrown");
  }
 }

 @Test
 void testInvalidHeaderName2() {
  String filterScript = "require  \"envelope\";\n"
    + "if anyof envelope :matches \"from123\" \"t1@zimbra.com\" {\n"
    + "    fileinto \"Junk\";\n"
    + "}\n"
  ;
  LmtpEnvelope env = new LmtpEnvelope();
  LmtpAddress sender = new LmtpAddress("<t1@zimbra.com>", new String[]{"BODY", "SIZE"}, null);
  LmtpAddress recipient = new LmtpAddress("<xyz@zimbra.com>", null, null);
  env.setSender(sender);
  env.addLocalRecipient(recipient);

  try {
   Provisioning prov = Provisioning.getInstance();
   Account account = prov.createAccount("xyz@zimbra.com", "secret", new HashMap<String, Object>());
   account.setMail("xyz@zimbra.com");
   RuleManager.clearCachedRules(account);
   Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(
     account);

   account.setMailSieveScript(filterScript);
   List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(
     new OperationContext(mbox), mbox,
     new ParsedMessage(sampleMsg.getBytes(), false), 0,
     account.getName(), env,
     new DeliveryContext(),
     Mailbox.ID_FOLDER_INBOX, true);
   assertEquals(1, ids.size());
   Message msg = mbox.getMessageById(null, ids.get(0).getId());
   assertEquals(Mailbox.ID_FOLDER_INBOX, msg.getFolderId());
  } catch (Exception e) {
   fail("No exception should be thrown");
  }
 }

 @Test
 void testAllDomainLocalIs() {
  String filterScript = "require  [\"envelope\", \"tag\"];\n"
    + "if envelope :domain :is \"to\" \"zimbra.com\" {\n"
    + "    tag \"is-domain\";\n"
    + "}\n"
    + "if envelope :localpart :is \"to\" \"xyz\" {\n"
    + "    tag \"is-local\";\n"
    + "}\n"
    + "if envelope :all :is \"to\" \"xyz@zimbra.com\" {"
    + "    tag \"is-all\";\n"
    + "}";
  LmtpEnvelope env = new LmtpEnvelope();
  LmtpAddress sender = new LmtpAddress("<t1@zimbra.com>", new String[]{"BODY", "SIZE"}, null);
  LmtpAddress recipient = new LmtpAddress("<xyz@zimbra.com>", null, null);
  env.setSender(sender);
  env.addLocalRecipient(recipient);

  try {
   Provisioning prov = Provisioning.getInstance();
   Account account = prov.createAccount("xyz@zimbra.com", "secret", new HashMap<String, Object>());
   account.setMail("xyz@zimbra.com");
   RuleManager.clearCachedRules(account);
   Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(
     account);

   account.setMailSieveScript(filterScript);
   List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(
     new OperationContext(mbox), mbox,
     new ParsedMessage(sampleMsg.getBytes(), false), 0,
     account.getName(), env,
     new DeliveryContext(),
     Mailbox.ID_FOLDER_INBOX, true);
   assertEquals(1, ids.size());
   Message msg = mbox.getMessageById(null, ids.get(0).getId());

   String[] tags = msg.getTags();
   assertTrue(tags != null);
   assertEquals(3, tags.length);
   assertEquals("is-domain", tags[0]);
   assertEquals("is-local", tags[1]);
   assertEquals("is-all", tags[2]);
  } catch (Exception e) {
   fail("No exception should be thrown: " + e);
  }
 }

 /*
  * The ascii-numeric comparator should be looked up in the list of the "require".
  */
 @Test
 void testMissingComparatorNumericDeclaration() throws Exception {
  // Default match type :is is used.
  // No "comparator-i;ascii-numeric" capability text in the require command
  String filterScript = "require [\"envelope\"];"
    + "if envelope :comparator \"i;ascii-numeric\" \"To\" \"xyz@zimbra.com\" {\n"
    + "  tag \"is\";\n"
    + "} else {\n"
    + "  tag \"not is\";\n"
    + "}";
  LmtpEnvelope env = new LmtpEnvelope();
  LmtpAddress sender = new LmtpAddress("<t1@zimbra.com>", new String[]{"BODY", "SIZE"}, null);
  LmtpAddress recipient = new LmtpAddress("<xyz@zimbra.com>", null, null);
  env.setSender(sender);
  env.addLocalRecipient(recipient);

  try {
   Account account = Provisioning.getInstance().getAccountByName("testEnv@zimbra.com");
   RuleManager.clearCachedRules(account);
   Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);

   account.unsetAdminSieveScriptBefore();
   account.unsetMailSieveScript();
   account.unsetAdminSieveScriptAfter();
   account.setMailSieveScript(filterScript);
   List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(
     new OperationContext(mbox), mbox,
     new ParsedMessage(sampleMsg.getBytes(), false), 0,
     account.getName(), env,
     new DeliveryContext(),
     Mailbox.ID_FOLDER_INBOX, true);
   assertEquals(1, ids.size());
   Message msg = mbox.getMessageById(null, ids.get(0).getId());
   assertNull(ArrayUtil.getFirstElement(msg.getTags()));
  } catch (Exception e) {
   fail("No exception should be thrown" + e);
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
