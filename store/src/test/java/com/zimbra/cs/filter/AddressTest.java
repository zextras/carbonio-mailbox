// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.filter;

import static org.junit.jupiter.api.Assertions.*;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

import javax.mail.internet.MimeMessage;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.zimbra.common.util.ArrayUtil;
import com.zimbra.common.zmime.ZMimeMessage;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.MockProvisioning;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.filter.RuleManager;
import com.zimbra.cs.lmtpserver.LmtpAddress;
import com.zimbra.cs.lmtpserver.LmtpEnvelope;
import com.zimbra.cs.mailbox.DeliveryContext;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.mailbox.Message;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.mailbox.Flag.FlagInfo;
import com.zimbra.cs.mime.ParsedMessage;
import com.zimbra.cs.service.util.ItemId;
import com.zimbra.cs.util.JMSession;

/**
 * @author zimbra
 *
 */
public class AddressTest {

    @BeforeAll
    public static void init() throws Exception {
        MailboxTestUtil.initServer();
        Provisioning prov = Provisioning.getInstance();
        prov.createAccount("test@in.telligent.com", "secret",
                new HashMap<String, Object>());
    }

    @BeforeEach
    public void setUp() throws Exception {
        MailboxTestUtil.clearData();
    }

 @Test
 void filterValidToField() {
  try {
   Account account = Provisioning.getInstance().getAccount(
     MockProvisioning.DEFAULT_ACCOUNT_ID);
   RuleManager.clearCachedRules(account);
   Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(
     account);

   String filterScript = "if anyof (address :domain :is :comparator \"i;ascii-casemap\" "
     + "[\"to\"] \"in.telligent.com\",address :domain :is :comparator \"i;ascii-casemap\" [\"to\"] "
     + "\"in.telligent.com\") {" + "tag \"Priority\";}";

   account.setMailSieveScript(filterScript);
   List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(
     new OperationContext(mbox), mbox, new ParsedMessage(
       "To: test1@in.telligent.com".getBytes(), false), 0,
     account.getName(), new DeliveryContext(),
     Mailbox.ID_FOLDER_INBOX, true);
   assertEquals(1, ids.size());
   Message msg = mbox.getMessageById(null, ids.get(0).getId());
   assertEquals("Priority",
     ArrayUtil.getFirstElement(msg.getTags()));

  } catch (Exception e) {
   fail("No exception should be thrown");
  }

 }

 @Test
 void filterInValidToField() {
  try {
   Account account = Provisioning.getInstance().getAccount(
     MockProvisioning.DEFAULT_ACCOUNT_ID);
   RuleManager.clearCachedRules(account);
   Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(
     account);

   String filterScript = "if anyof (address :domain :is :comparator \"i;ascii-casemap\" "
     + "[\"to\"] \"in.telligent.com\",address :domain :is :comparator \"i;ascii-casemap\" [\"to\"] "
     + "\"in.telligent.com\") {" + "tag \"Priority\";}";

   account.setMailSieveScript(filterScript);
   List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(
     new OperationContext(mbox), mbox, new ParsedMessage(
       "To: undisclosed-recipients:;".getBytes(), false),
     0, account.getName(), new DeliveryContext(),
     Mailbox.ID_FOLDER_INBOX, true);
   assertEquals(1, ids.size());
   Message msg = mbox.getMessageById(null, ids.get(0).getId());
   assertNull(ArrayUtil.getFirstElement(msg.getTags()));
  } catch (Exception e) {
   fail("No exception should be thrown");
  }

 }

 @Test
 void noComparator() {
  try {
   Account account = Provisioning.getInstance().getAccount(
     MockProvisioning.DEFAULT_ACCOUNT_ID);
   RuleManager.clearCachedRules(account);
   Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(
     account);

   String filterScript = "if address :matches [\"to\"] \"*\" {"
     + "  tag \"noComparator\";"
     + "}";

   account.setMailSieveScript(filterScript);
   List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(
     new OperationContext(mbox), mbox,
     new ParsedMessage("to: foo@example.com".getBytes(), false),
     0, account.getName(), new DeliveryContext(),
     Mailbox.ID_FOLDER_INBOX, true);
   assertEquals(1, ids.size());
   Message msg = mbox.getMessageById(null, ids.get(0).getId());
   assertEquals("noComparator", ArrayUtil.getFirstElement(msg.getTags()));
  } catch (Exception e) {
   fail("No exception should be thrown" + e);
  }
 }

 @Test
 void testAddressContainingBackslash() {
  try {
   Account account = Provisioning.getInstance().getAccount(
     MockProvisioning.DEFAULT_ACCOUNT_ID);
   RuleManager.clearCachedRules(account);
   Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(
     account);

   String filterScript = "if address :comparator \"i;ascii-casemap\" :matches \"from\" \"\\\"user\\\\1\\\"@cosmonaut.zimbra.com\" {"
     + "  tag \"TestBackslash\";"
     + "}";

   account.setMailSieveScript(filterScript);
   InputStream is = getClass().getResourceAsStream("TestFilter-testBackslashDotInAddress.msg");
   MimeMessage mm = new ZMimeMessage(JMSession.getSession(), is);

   List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(
     new OperationContext(mbox), mbox,
     new ParsedMessage(mm, false),
     0, account.getName(), new DeliveryContext(),
     Mailbox.ID_FOLDER_INBOX, true);
   assertEquals(1, ids.size());
   Message msg = mbox.getMessageById(null, ids.get(0).getId());
   assertEquals("TestBackslash", ArrayUtil.getFirstElement(msg.getTags()));
  } catch (Exception e) {
   fail("No exception should be thrown" + e);
  }
 }

 @Test
 void testAddressContainingDot() {
  try {
   Account account = Provisioning.getInstance().getAccount(
     MockProvisioning.DEFAULT_ACCOUNT_ID);
   RuleManager.clearCachedRules(account);
   Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(
     account);

   String filterScript = "if address :comparator \"i;ascii-casemap\" :matches \"to\" \"user.1@cosmonaut.zimbra.com\" {"
     + "  tag \"TestDot\";"
     + "}";

   account.setMailSieveScript(filterScript);
   InputStream is = getClass().getResourceAsStream("TestFilter-testBackslashDotInAddress.msg");
   MimeMessage mm = new ZMimeMessage(JMSession.getSession(), is);

   List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(
     new OperationContext(mbox), mbox,
     new ParsedMessage(mm, false),
     0, account.getName(), new DeliveryContext(),
     Mailbox.ID_FOLDER_INBOX, true);
   assertEquals(1, ids.size());
   Message msg = mbox.getMessageById(null, ids.get(0).getId());
   assertEquals("TestDot", ArrayUtil.getFirstElement(msg.getTags()));
  } catch (Exception e) {
   fail("No exception should be thrown" + e);
  }
 }

 @Test
 void testAddressContainingDoubleQuote() {
  try {
   Account account = Provisioning.getInstance().getAccount(
     MockProvisioning.DEFAULT_ACCOUNT_ID);
   RuleManager.clearCachedRules(account);
   Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(
     account);

   String filterScript = "if address :comparator \"i;ascii-casemap\" :matches \"to\" \"\\\"user\\\"1\\\"@cosmonaut.zimbra.com\" {"
     + "  tag \"TestDoubleQuote\";"
     + "}";

   account.setMailSieveScript(filterScript);
   InputStream is = getClass().getResourceAsStream("TestFilter-testQuotesInAddress.msg");
   MimeMessage mm = new ZMimeMessage(JMSession.getSession(), is);

   List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(
     new OperationContext(mbox), mbox,
     new ParsedMessage(mm, false),
     0, account.getName(), new DeliveryContext(),
     Mailbox.ID_FOLDER_INBOX, true);
   assertEquals(1, ids.size());
   Message msg = mbox.getMessageById(null, ids.get(0).getId());
   assertEquals("TestDoubleQuote", ArrayUtil.getFirstElement(msg.getTags()));
  } catch (Exception e) {
   fail("No exception should be thrown" + e);
  }
 }

 @Test
 void testAddressContainingSingleQuote() {
  try {
   Account account = Provisioning.getInstance().getAccount(
     MockProvisioning.DEFAULT_ACCOUNT_ID);
   RuleManager.clearCachedRules(account);
   Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(
     account);

   String filterScript = "if address :comparator \"i;ascii-casemap\" :matches \"from\" \"user'1@cosmonaut.zimbra.com\" {"
     + "  tag \"TestSingleQuote\";"
     + "}";

   account.setMailSieveScript(filterScript);
   InputStream is = getClass().getResourceAsStream("TestFilter-testQuotesInAddress.msg");
   MimeMessage mm = new ZMimeMessage(JMSession.getSession(), is);

   List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(
     new OperationContext(mbox), mbox,
     new ParsedMessage(mm, false),
     0, account.getName(), new DeliveryContext(),
     Mailbox.ID_FOLDER_INBOX, true);
   assertEquals(1, ids.size());
   Message msg = mbox.getMessageById(null, ids.get(0).getId());
   assertEquals("TestSingleQuote", ArrayUtil.getFirstElement(msg.getTags()));
  } catch (Exception e) {
   fail("No exception should be thrown" + e);
  }
 }

 @Test
 void testAddressContainingQuestionMark() {
  try {
   Account account = Provisioning.getInstance().getAccount(
     MockProvisioning.DEFAULT_ACCOUNT_ID);
   RuleManager.clearCachedRules(account);
   Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(
     account);

   String filterScript = "if address :comparator \"i;ascii-casemap\" :matches \"from\" \"user?1@cosmonaut.zimbra.com\" {"
     + "  tag \"TestQuestionMark\";"
     + "}";

   account.setMailSieveScript(filterScript);
   InputStream is = getClass().getResourceAsStream("TestFilter-testQuestionMarkCommaInAddress.msg");
   MimeMessage mm = new ZMimeMessage(JMSession.getSession(), is);

   List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(
     new OperationContext(mbox), mbox,
     new ParsedMessage(mm, false),
     0, account.getName(), new DeliveryContext(),
     Mailbox.ID_FOLDER_INBOX, true);
   assertEquals(1, ids.size());
   Message msg = mbox.getMessageById(null, ids.get(0).getId());
   assertEquals("TestQuestionMark", ArrayUtil.getFirstElement(msg.getTags()));
  } catch (Exception e) {
   fail("No exception should be thrown" + e);
  }
 }

 @Test
 void testAddressContainingComma() {
  try {
   Account account = Provisioning.getInstance().getAccount(
     MockProvisioning.DEFAULT_ACCOUNT_ID);
   RuleManager.clearCachedRules(account);
   Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(
     account);

   String filterScript = "if address :comparator \"i;ascii-casemap\" :matches \"to\" \"\\\"user,1\\\"@cosmonaut.zimbra.com\" {"
     + "  tag \"TestComma\";"
     + "}";

   account.setMailSieveScript(filterScript);
   InputStream is = getClass().getResourceAsStream("TestFilter-testQuestionMarkCommaInAddress.msg");
   MimeMessage mm = new ZMimeMessage(JMSession.getSession(), is);

   List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(
     new OperationContext(mbox), mbox,
     new ParsedMessage(mm, false),
     0, account.getName(), new DeliveryContext(),
     Mailbox.ID_FOLDER_INBOX, true);
   assertEquals(1, ids.size());
   Message msg = mbox.getMessageById(null, ids.get(0).getId());
   assertEquals("TestComma", ArrayUtil.getFirstElement(msg.getTags()));
  } catch (Exception e) {
   fail("No exception should be thrown" + e);
  }
 }

 @Test
 void compareEmptyStringWithAsciiNumeric() {
  try {
   Account acct = Provisioning.getInstance().getAccount(MockProvisioning.DEFAULT_ACCOUNT_ID);
   RuleManager.clearCachedRules(acct);
   Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(acct);

   String filterScript = "require [\"comparator-i;ascii-numeric\"];"
     + "if address :is :comparator \"i;ascii-numeric\" \"To\" \"\" {"
     + "  tag \"compareEmptyStringWithAsciiNumeric\";"
     + "}";

   acct.setMailSieveScript(filterScript);
   List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(
     new OperationContext(mbox), mbox, new ParsedMessage("To: test1@zimbra.com".getBytes(), false), 0,
     acct.getName(), new DeliveryContext(), Mailbox.ID_FOLDER_INBOX, true);
   assertEquals(1, ids.size());
   Message msg = mbox.getMessageById(null, ids.get(0).getId());
   assertEquals("compareEmptyStringWithAsciiNumeric", ArrayUtil.getFirstElement(msg.getTags()));
  } catch (Exception e) {
   fail("No exception should be thrown");
  }
 }

 @Test
 void testNumericNegativeValueIs() {
  try {
   Account acct = Provisioning.getInstance().getAccount(MockProvisioning.DEFAULT_ACCOUNT_ID);
   RuleManager.clearCachedRules(acct);
   Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(acct);

   String filterScript = "require [\"tag\", \"relational\", \"comparator-i;ascii-numeric\"];\n"
     + "if address :count \"lt\" :comparator \"i;ascii-numeric\" \"To\" \"-1\" {"
     + "  tag \"compareAsciiNumericNegativeValue\";"
     + "}";

   acct.setMailSieveScript(filterScript);
   List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(
     new OperationContext(mbox), mbox, new ParsedMessage("To: test1@zimbra.com".getBytes(), false), 0,
     acct.getName(), new DeliveryContext(), Mailbox.ID_FOLDER_INBOX, true);
   assertEquals(1, ids.size());
   Message msg = mbox.getMessageById(null, ids.get(0).getId());
   assertNull(ArrayUtil.getFirstElement(msg.getTags()));
  } catch (Exception e) {
   fail("No exception should be thrown");
  }
 }

 @Test
 void compareHeaderNameWithLeadingSpaces() {
  try {
   Account acct = Provisioning.getInstance().getAccount(MockProvisioning.DEFAULT_ACCOUNT_ID);
   RuleManager.clearCachedRules(acct);
   Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(acct);

   String filterScript = "require [\"tag\", \"comparator-i;ascii-numeric\"];\n"
     + "if address :is :comparator \"i;ascii-numeric\" \" To\" \"test1@zimbra.com\" {"
     + "  tag \"t1\";"
     + "}"
   ;

   acct.setMailSieveScript(filterScript);
   List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(
     new OperationContext(mbox), mbox, new ParsedMessage("To: test1@zimbra.com".getBytes(), false), 0,
     acct.getName(), new DeliveryContext(), Mailbox.ID_FOLDER_INBOX, true);
   assertEquals(1, ids.size());
   Message msg = mbox.getMessageById(null, ids.get(0).getId());
   assertEquals(0, msg.getTags().length);
  } catch (Exception e) {
   fail("No exception should be thrown");
  }
 }

 @Test
 void compareHeaderNameWithTrailingSpaces() {
  try {
   Account acct = Provisioning.getInstance().getAccount(MockProvisioning.DEFAULT_ACCOUNT_ID);
   RuleManager.clearCachedRules(acct);
   Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(acct);

   String filterScript = "require [\"tag\", \"comparator-i;ascii-numeric\"];\n"
     + "if address :is :comparator \"i;ascii-numeric\" \"To \" \"test1@zimbra.com\" {"
     + "  tag \"t2\";"
     + "}"
   ;

   acct.setMailSieveScript(filterScript);
   List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(
     new OperationContext(mbox), mbox, new ParsedMessage("To: test1@zimbra.com".getBytes(), false), 0,
     acct.getName(), new DeliveryContext(), Mailbox.ID_FOLDER_INBOX, true);
   assertEquals(1, ids.size());
   Message msg = mbox.getMessageById(null, ids.get(0).getId());
   assertEquals(0, msg.getTags().length);
  } catch (Exception e) {
   fail("No exception should be thrown");
  }
 }

 @Test
 void compareHeaderNameWithLeadingAndTrailingSpaces() {
  try {
   Account acct = Provisioning.getInstance().getAccount(MockProvisioning.DEFAULT_ACCOUNT_ID);
   RuleManager.clearCachedRules(acct);
   Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(acct);

   String filterScript = "require [\"tag\", \"comparator-i;ascii-numeric\"];\n"
     + "if address :is :comparator \"i;ascii-numeric\" \" To \" \"test1@zimbra.com\" {"
     + "  tag \"t3\";"
     + "}"
   ;

   acct.setMailSieveScript(filterScript);
   List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(
     new OperationContext(mbox), mbox, new ParsedMessage("To: test1@zimbra.com".getBytes(), false), 0,
     acct.getName(), new DeliveryContext(), Mailbox.ID_FOLDER_INBOX, true);
   assertEquals(1, ids.size());
   Message msg = mbox.getMessageById(null, ids.get(0).getId());
   assertEquals(0, msg.getTags().length);
  } catch (Exception e) {
   fail("No exception should be thrown");
  }
 }

 @Test
 void testDomainIs() {
  String filterScript = "require  [\"envelope\", \"tag\"];\n"
    + "if address :domain :is \"to\" \"zimbra.com\" {\n"
    + "    tag \"is-domain\";\n"
    + "}\n"
    + "if address :localpart :is \"to\" \"xyz\" {\n"
    + "    tag \"is-local\";\n"
    + "}\n"
    + "if address :all :is \"to\" \"xyz@zimbra.com\" {"
    + "    tag \"is-all\";\n"
    + "}";

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
     new ParsedMessage("To: xyz@zimbra.com".getBytes(), false), 0,
     account.getName(),
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

 @Test
 void testMalencodedHeader() throws Exception {
  String filterScript = "if address :matches [\"To\"] \"*\" { flag \"priority\"; }";
  try {
   Account account = Provisioning.getInstance().getAccount(
     MockProvisioning.DEFAULT_ACCOUNT_ID);
   RuleManager.clearCachedRules(account);
   Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(
     account);
   account.setMailSieveScript(filterScript);

   List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(
     new OperationContext(mbox), mbox,
     new ParsedMessage("to: =?ABC?A?GyRCJFskMhsoQg==?=@zimbra.com".getBytes(), false),
     0, account.getName(), new DeliveryContext(),
     Mailbox.ID_FOLDER_INBOX, true);

   assertEquals(1, ids.size());
   Message msg = mbox.getMessageById(null, ids.get(0).getId());
   assertTrue(msg.isTagged(FlagInfo.PRIORITY));
  } catch (Exception e) {
   fail("No exception should be thrown" + e);
  }
 }

 /*
  * The ascii-numeric comparator should be looked up in the list of the "require".
  */
 @Test
 void testMissingComparatorNumericDeclaration() throws Exception {
  // Default match type :is is used.
  // No "comparator-i;ascii-numeric" capability text in the require command
  String filterScript = "require [\"tag\"];"
    + "if address :comparator \"i;ascii-numeric\" \"To\" \"test1@zimbra.com\" {\n"
    + "  tag \"is\";\n"
    + "} else {\n"
    + "  tag \"not is\";\n"
    + "}";
  try {
   Account account = Provisioning.getInstance().getAccount(
     MockProvisioning.DEFAULT_ACCOUNT_ID);
   RuleManager.clearCachedRules(account);
   Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);

   account.unsetAdminSieveScriptBefore();
   account.unsetMailSieveScript();
   account.unsetAdminSieveScriptAfter();
   account.setMailSieveScript(filterScript);
   List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(
     new OperationContext(mbox), mbox,
     new ParsedMessage("To: test1@zimbra.com\nSubject: example\n".getBytes(), false), 0,
     account.getName(),
     new DeliveryContext(),
     Mailbox.ID_FOLDER_INBOX, true);
   assertEquals(1, ids.size());
   Message msg = mbox.getMessageById(null, ids.get(0).getId());
   assertNull(ArrayUtil.getFirstElement(msg.getTags()));
  } catch (Exception e) {
   fail("No exception should be thrown " + e);
  }
 }
}
