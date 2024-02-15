// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.MockProvisioning;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.DeliveryContext;
import com.zimbra.cs.mailbox.DeliveryOptions;
import com.zimbra.cs.mailbox.Flag;
import com.zimbra.cs.mailbox.Folder;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.MailItem.Type;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.mailbox.Message;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.mime.ParsedMessage;
import com.zimbra.cs.service.util.ItemId;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import qa.unittest.TestUtil;

class FileIntoCopyTest {

  private static Provisioning provisioning;

  @BeforeEach
  public void setUp() throws Exception {
    MailboxTestUtil.initServer();
    provisioning = Provisioning.getInstance();
  }

  @AfterEach
  public void tearDown() {
    try {
      MailboxTestUtil.clearData();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Test
  void testCopyFileInto() {
    String filterScript = "require [\"copy\", \"fileinto\"];\n"
        + "if header :contains \"Subject\" \"test\" { fileinto :copy \"Junk\"; }";
    try {
      final String accountName = UUID.randomUUID() + "@" + UUID.randomUUID() + ".com";
      final Account account = provisioning.createAccount(accountName, "secret", new HashMap<>());
      RuleManager.clearCachedRules(account);
      Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);
      account.setMailSieveScript(filterScript);
      String raw = "From: sender@zimbra.com\n" + "To: test1@zimbra.com\n" + "Subject: Test\n"
          + "\n" + "Hello World.";
      List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(new OperationContext(mbox),
          mbox, new ParsedMessage(raw.getBytes(), false), 0, account.getName(),
          new DeliveryContext(), Mailbox.ID_FOLDER_INBOX, true);

      assertEquals(2, ids.size());

      Message msg = mbox.getMessageById(null, ids.get(0).getId());
      assertEquals("Test", msg.getSubject());
    } catch (Exception e) {
      e.printStackTrace();
      fail("No exception should be thrown");
    }
  }

  @Test
  void testPlainFileInto() {
    String filterPlainFileintoScript = "require [\"fileinto\"];\n"
        + "if header :contains \"Subject\" \"test\" { fileinto \"Junk\"; }";
    try {
      final String accountName = UUID.randomUUID() + "@" + UUID.randomUUID() + ".com";
      final Account account = provisioning.createAccount(accountName, "secret", new HashMap<>());
      RuleManager.clearCachedRules(account);
      Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);
      account.setMailSieveScript(filterPlainFileintoScript);
      String raw = "From: sender@zimbra.com\n" + "To: test1@zimbra.com\n" + "Subject: Test\n"
          + "\n" + "Hello World.";
      List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(new OperationContext(mbox),
          mbox, new ParsedMessage(raw.getBytes(), false), 0, account.getName(),
          new DeliveryContext(), Mailbox.ID_FOLDER_INBOX, true);
      assertEquals(1, ids.size());
      Message msg = mbox.getMessageById(null, ids.get(0).getId());
      assertEquals("Test", msg.getSubject());
    } catch (Exception e) {
      e.printStackTrace();
      fail("No exception should be thrown");
    }
  }

  @Test
  void testPlainFileIntoNonExistingFolder() {
    String filterPlainFileintoScript = "require [\"fileinto\"];\n"
        + "if header :contains \"Subject\" \"test\" { fileinto \"HelloWorld\"; }";
    try {
      final String accountName = UUID.randomUUID() + "@" + UUID.randomUUID() + ".com";
      final Account account = provisioning.createAccount(accountName, "secret", new HashMap<>());
      RuleManager.clearCachedRules(account);
      Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);
      account.setMailSieveScript(filterPlainFileintoScript);
      String raw = "From: sender@zimbra.com\n" + "To: test1@zimbra.com\n" + "Subject: Test\n"
          + "\n" + "Hello World.";
      List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(new OperationContext(mbox),
          mbox, new ParsedMessage(raw.getBytes(), false), 0, account.getName(),
          new DeliveryContext(), Mailbox.ID_FOLDER_INBOX, true);
      assertEquals(1, ids.size());
      Message msg = mbox.getMessageById(null, ids.get(0).getId());
      assertEquals("Test", msg.getSubject());
      com.zimbra.cs.mailbox.Folder folder = mbox.getFolderById(null, msg.getFolderId());
      assertEquals("HelloWorld", folder.getName());
    } catch (Exception e) {
      e.printStackTrace();
      fail("No exception should be thrown");
    }
  }

  /*
   * fileinto :copy foo; if header :contains "Subject" "test" { fileinto bar;
   * }
   *
   * if message has "Subject: Test" ==> it should be stored in "foo" and "bar"
   */
  @Test
  void testCopyFileIntoPattern1Test() {
    try {
      String filterScriptPattern1 = "require [\"copy\", \"fileinto\"];\n"
          + "fileinto :copy \"foo\";\n" + "if header :contains \"Subject\" \"Test\" {\n"
          + "fileinto \"bar\"; }";
      final String accountName = UUID.randomUUID() + "@" + UUID.randomUUID() + ".com";
      final Account account = provisioning.createAccount(accountName, "secret", new HashMap<>());
      RuleManager.clearCachedRules(account);
      Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);
      account.setMailSieveScript(filterScriptPattern1);
      String rawTest = "From: sender@zimbra.com\n" + "To: test1@zimbra.com\n"
          + "Subject: Test\n" + "\n" + "Hello World";
      RuleManager.applyRulesToIncomingMessage(new OperationContext(mbox),
          mbox, new ParsedMessage(rawTest.getBytes(), false), 0, account.getName(),
          new DeliveryContext(), Mailbox.ID_FOLDER_INBOX, true);
      // message should not be stored in inbox
      assertNull(
          mbox.getItemIds(null, Mailbox.ID_FOLDER_INBOX).getIds(MailItem.Type.MESSAGE));
      // message should be stored in foo
      Integer item = Objects.requireNonNull(mbox
          .getItemIds(null,
              mbox.getFolderByName(null, Mailbox.ID_FOLDER_USER_ROOT, "foo").getId())
          .getIds(Type.MESSAGE)).get(0);
      Message msg = mbox.getMessageById(null, item);
      assertEquals("Hello World", msg.getFragment());
      // message should be stored in bar
      item = Objects.requireNonNull(mbox
          .getItemIds(null,
              mbox.getFolderByName(null, Mailbox.ID_FOLDER_USER_ROOT, "bar").getId())
          .getIds(Type.MESSAGE)).get(0);
      msg = mbox.getMessageById(null, item);
      assertEquals("Hello World", msg.getFragment());
    } catch (Exception e) {
      e.printStackTrace();
      fail("No exception should be thrown");
    }
  }

  /*
   * fileinto :copy foo; if header :contains "Subject" "test" { fileinto bar;
   * }
   *
   * if message has "Subject: real" ==> it should be stored in "foo" and INBOX
   */

  @Test
  void testCopyFileIntoPattern1Real() {
    try {
      String filterScriptPattern1 = "require [\"copy\", \"fileinto\"];\n"
          + "fileinto :copy \"foo\";\n" + "if header :contains \"Subject\" \"Test\" {\n"
          + "fileinto \"bar\"; }";
      final String accountName = UUID.randomUUID() + "@" + UUID.randomUUID() + ".com";
      final Account account = provisioning.createAccount(accountName, "secret", new HashMap<>());
      RuleManager.clearCachedRules(account);
      Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);
      account.setMailSieveScript(filterScriptPattern1);
      String rawReal = "From: sender@zimbra.com\n" + "To: test1@zimbra.com\n"
          + "Subject: Real\n" + "\n" + "Hello World";
      RuleManager.applyRulesToIncomingMessage(new OperationContext(mbox), mbox,
          new ParsedMessage(rawReal.getBytes(), false), 0, account.getName(),
          new DeliveryContext(), Mailbox.ID_FOLDER_INBOX, true);
      // message should be stored in foo
      Integer item = Objects.requireNonNull(mbox
          .getItemIds(null,
              mbox.getFolderByName(null, Mailbox.ID_FOLDER_USER_ROOT, "foo").getId())
          .getIds(Type.MESSAGE)).get(0);
      Message msg = mbox.getMessageById(null, item);
      assertEquals("Hello World", msg.getFragment());
      // message should be stored in inbox
      item = Objects
          .requireNonNull(mbox.getItemIds(null, Mailbox.ID_FOLDER_INBOX).getIds(Type.MESSAGE))
          .get(0);
      msg = mbox.getMessageById(null, item);
      assertEquals("Hello World", msg.getFragment());
    } catch (Exception e) {
      e.printStackTrace();
      fail("No exception should be thrown");
    }
  }

  /*
   * fileinto :copy foo; if header :contains "Subject" "Test" { fileinto :copy
   * bar; }
   *
   * if message has "Subject: test" ==> it should be stored in "foo", "bar"
   * and INBOX
   */

  @Test
  void testCopyFileIntoPattern2Test() {
    try {
      String filterScriptPattern1 = "require [\"copy\", \"fileinto\"];\n"
          + "fileinto :copy \"foo\";" + "if header :contains \"Subject\" \"Test\" {\n"
          + "fileinto :copy \"bar\"; }";
      final String accountName = UUID.randomUUID() + "@" + UUID.randomUUID() + ".com";
      final Account account = provisioning.createAccount(accountName, "secret", new HashMap<>());
      RuleManager.clearCachedRules(account);
      Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);
      account.setMailSieveScript(filterScriptPattern1);
      String rawReal = "From: sender@zimbra.com\n" + "To: test1@zimbra.com\n"
          + "Subject: Test\n" + "\n" + "Hello World";
      RuleManager.applyRulesToIncomingMessage(new OperationContext(mbox), mbox,
          new ParsedMessage(rawReal.getBytes(), false), 0, account.getName(),
          new DeliveryContext(), Mailbox.ID_FOLDER_INBOX, true);
      // message should be stored in bar
      Integer item = Objects.requireNonNull(mbox
          .getItemIds(null,
              mbox.getFolderByName(null, Mailbox.ID_FOLDER_USER_ROOT, "bar").getId())
          .getIds(Type.MESSAGE)).get(0);
      Message msg = mbox.getMessageById(null, item);
      assertEquals("Hello World", msg.getFragment());
      // message should be stored in foo
      item = Objects.requireNonNull(mbox
          .getItemIds(null,
              mbox.getFolderByName(null, Mailbox.ID_FOLDER_USER_ROOT, "foo").getId())
          .getIds(Type.MESSAGE)).get(0);
      msg = mbox.getMessageById(null, item);
      assertEquals("Hello World", msg.getFragment());
      // message should be stored in inbox
      item = Objects
          .requireNonNull(mbox.getItemIds(null, Mailbox.ID_FOLDER_INBOX).getIds(Type.MESSAGE))
          .get(0);
      msg = mbox.getMessageById(null, item);
      assertEquals("Hello World", msg.getFragment());
    } catch (Exception e) {
      e.printStackTrace();
      fail("No exception should be thrown");
    }
  }

  /*
   * fileinto :copy foo; if header :contains "Subject" "Test" { fileinto :copy
   * bar; }
   *
   * if message has "Subject: Real" ==> it should be stored in "foo" and INBOX
   */

  @Test
  void testCopyFileIntoPattern2Real() {
    try {
      String filterScriptPattern1 = "require [\"copy\", \"fileinto\"];\n"
          + "fileinto :copy \"foo\";" + "if header :contains \"Subject\" \"Test\" {\n"
          + "fileinto :copy \"bar\"; }";
      final String accountName = UUID.randomUUID() + "@" + UUID.randomUUID() + ".com";
      final Account account = provisioning.createAccount(accountName, "secret", new HashMap<>());
      RuleManager.clearCachedRules(account);
      Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);
      account.setMailSieveScript(filterScriptPattern1);
      String rawReal = "From: sender@zimbra.com\n" + "To: test1@zimbra.com\n"
          + "Subject: Real\n" + "\n" + "Hello World";
      RuleManager.applyRulesToIncomingMessage(new OperationContext(mbox), mbox,
          new ParsedMessage(rawReal.getBytes(), false), 0, account.getName(),
          new DeliveryContext(), Mailbox.ID_FOLDER_INBOX, true);
      // message should be stored in foo
      Integer item = Objects.requireNonNull(mbox
          .getItemIds(null,
              mbox.getFolderByName(null, Mailbox.ID_FOLDER_USER_ROOT, "foo").getId())
          .getIds(Type.MESSAGE)).get(0);
      Message msg = mbox.getMessageById(null, item);
      assertEquals("Hello World", msg.getFragment());
      // message should be stored in inbox
      item = Objects
          .requireNonNull(mbox.getItemIds(null, Mailbox.ID_FOLDER_INBOX).getIds(Type.MESSAGE))
          .get(0);
      msg = mbox.getMessageById(null, item);
      assertEquals("Hello World", msg.getFragment());
    } catch (Exception e) {
      e.printStackTrace();
      fail("No exception should be thrown");
    }
  }

  /*
   * fileinto :copy foo; if header :contains "Subject" "Test" { discard; }
   *
   * if message has "Subject: Test" ==> it should be stored in "foo"
   */

  @Test
  void testCopyFileIntoPattern3Test() {
    try {
      String filterScriptPattern1 = "require [\"copy\", \"fileinto\"];\n"
          + "fileinto :copy \"foo\";" + "if header :contains \"Subject\" \"Test\" {\n"
          + "discard; }";
      final String accountName = UUID.randomUUID() + "@" + UUID.randomUUID() + ".com";
      final Account account = provisioning.createAccount(accountName, "secret", new HashMap<>());
      RuleManager.clearCachedRules(account);
      Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);
      account.setMailSieveScript(filterScriptPattern1);
      String rawReal = "From: sender@zimbra.com\n" + "To: test1@zimbra.com\n"
          + "Subject: Test\n" + "\n" + "Hello World";
      RuleManager.applyRulesToIncomingMessage(new OperationContext(mbox), mbox,
          new ParsedMessage(rawReal.getBytes(), false), 0, account.getName(),
          new DeliveryContext(), Mailbox.ID_FOLDER_INBOX, true);
      // message should be stored in foo
      Integer item = Objects.requireNonNull(mbox
          .getItemIds(null,
              mbox.getFolderByName(null, Mailbox.ID_FOLDER_USER_ROOT, "foo").getId())
          .getIds(Type.MESSAGE)).get(0);
      Message msg = mbox.getMessageById(null, item);
      assertEquals("Hello World", msg.getFragment());
      // message should not be stored in inbox
      assertNull(
          mbox.getItemIds(null, Mailbox.ID_FOLDER_INBOX).getIds(MailItem.Type.MESSAGE));
    } catch (Exception e) {
      e.printStackTrace();
      fail("No exception should be thrown");
    }
  }

  /*
   * fileinto :copy foo; if header :contains "Subject" "Test" { discard; }
   *
   * if message has "Subject: real" ==> it should be stored in "foo" and INBOX
   */

  @Test
  void testCopyFileIntoPattern3Real() {
    try {
      String filterScriptPattern1 = "require [\"copy\", \"fileinto\"];\n"
          + "fileinto :copy \"foo\";" + "if header :contains \"Subject\" \"Test\" {\n"
          + "discard; }";
      final String accountName = UUID.randomUUID() + "@" + UUID.randomUUID() + ".com";
      final Account account = provisioning.createAccount(accountName, "secret", new HashMap<>());
      RuleManager.clearCachedRules(account);
      Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);
      account.setMailSieveScript(filterScriptPattern1);
      String rawReal = "From: sender@zimbra.com\n" + "To: test1@zimbra.com\n"
          + "Subject: Real\n" + "\n" + "Hello World";
      RuleManager.applyRulesToIncomingMessage(new OperationContext(mbox), mbox,
          new ParsedMessage(rawReal.getBytes(), false), 0, account.getName(),
          new DeliveryContext(), Mailbox.ID_FOLDER_INBOX, true);
      // message should be stored in foo
      Integer item = Objects.requireNonNull(mbox
          .getItemIds(null,
              mbox.getFolderByName(null, Mailbox.ID_FOLDER_USER_ROOT, "foo").getId())
          .getIds(Type.MESSAGE)).get(0);
      Message msg = mbox.getMessageById(null, item);
      assertEquals("Hello World", msg.getFragment());
      // message should be stored in inbox
      item = Objects
          .requireNonNull(mbox.getItemIds(null, Mailbox.ID_FOLDER_INBOX).getIds(Type.MESSAGE))
          .get(0);
      msg = mbox.getMessageById(null, item);
      assertEquals("Hello World", msg.getFragment());
    } catch (Exception e) {
      e.printStackTrace();
      fail("No exception should be thrown");
    }
  }

  /*
   * Only one copy of message should be delivered in INBOX
   */
  @Test
  void testKeepAndFileInto() {
    doKeepAndFileInto("require \"fileinto\"; keep; fileinto \"Inbox\";");
    doKeepAndFileInto("require \"fileinto\"; keep; fileinto \"/Inbox\";");
    doKeepAndFileInto("require \"fileinto\"; keep; fileinto \"Inbox/\";");
    doKeepAndFileInto("require \"fileinto\"; keep; fileinto \"/Inbox/\";");
    doKeepAndFileInto("require \"fileinto\"; keep; fileinto \"inbox\";");
    doKeepAndFileInto("require \"fileinto\"; fileinto \"Inbox\"; keep;");
    doKeepAndFileInto("require [\"fileinto\", \"copy\"]; fileinto :copy \"Inbox\"; keep;");

    doKeepAndFileIntoOutgoing("require \"fileinto\"; keep; fileinto \"Sent\";");
    doKeepAndFileIntoOutgoing("require \"fileinto\"; keep; fileinto \"/Sent\";");
    doKeepAndFileIntoOutgoing("require \"fileinto\"; keep; fileinto \"Sent/\";");
    doKeepAndFileIntoOutgoing("require \"fileinto\"; keep; fileinto \"/Sent/\";");
    doKeepAndFileIntoOutgoing("require \"fileinto\"; keep; fileinto \"sent\";");
    doKeepAndFileIntoOutgoing("require \"fileinto\"; fileinto \"Sent\"; keep;");
    doKeepAndFileIntoOutgoing("require [\"fileinto\", \"copy\"]; fileinto :copy \"Sent\"; keep;");
  }

  private void doKeepAndFileInto(String filterScript) {
    doKeepAndFileIntoIncoming(filterScript);
    doKeepAndFileIntoExisting(filterScript);
  }

  private void doKeepAndFileIntoIncoming(String filterScript) {
    String body = "doKeepAndFileIntoIncoming" + filterScript.hashCode();
    String sampleMsg = "From: sender@zimbra.com\n" + "To: test1@zimbra.com\n" + "Subject: Test\n"
        + "\n" + body;
    try {
      // Incoming
      final String accountName = UUID.randomUUID() + "@" + UUID.randomUUID() + ".com";
      final Account account = provisioning.createAccount(accountName, "secret", new HashMap<>());
      RuleManager.clearCachedRules(account);
      Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);
      account.setMailSieveScript(filterScript);

      List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(new OperationContext(mbox),
          mbox, new ParsedMessage(sampleMsg.getBytes(), false), 0, account.getName(),
          new DeliveryContext(), Mailbox.ID_FOLDER_INBOX, true);
      assertEquals(1, ids.size());

      List<Integer> searchedIds = TestUtil.search(mbox, "in:inbox " + body, MailItem.Type.MESSAGE);
      assertEquals(1, searchedIds.size());
    } catch (Exception e) {
      e.printStackTrace();
      fail("No exception should be thrown");
    }
  }

  private void doKeepAndFileIntoExisting(String filterScript) {
    String body = "doKeepAndFileIntoExisting" + filterScript.hashCode();
    String sampleMsg = "From: sender@zimbra.com\n" + "To: test1@zimbra.com\n" + "Subject: Test\n"
        + "\n" + body;
    try {
      // Existing
      Account account = Provisioning.getInstance().getAccount(MockProvisioning.DEFAULT_ACCOUNT_ID);
      RuleManager.clearCachedRules(account);
      Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);
      mbox.getItemIds(null, Mailbox.ID_FOLDER_INBOX)
          .getIds(MailItem.Type.MESSAGE);
      OperationContext octx = new OperationContext(mbox);
      Message msg = mbox.addMessage(octx,
          new ParsedMessage(sampleMsg.getBytes(), false),
          new DeliveryOptions().setFolderId(Mailbox.ID_FOLDER_INBOX)
              .setFlags(Flag.BITMASK_PRIORITY),
          new DeliveryContext());
      boolean filtered = RuleManager.applyRulesToExistingMessage(new OperationContext(mbox), mbox,
          msg.getId(),
          RuleManager.parse(filterScript));
      assertFalse(filtered);
      List<Integer> searchedIds = TestUtil.search(mbox, "in:inbox " + body, MailItem.Type.MESSAGE);
      assertEquals(1, searchedIds.size());
    } catch (Exception e) {
      e.printStackTrace();
      fail("No exception should be thrown");
    }
  }

  private void doKeepAndFileIntoOutgoing(String filterScript) {
    String body = "doKeepAndFileIntoIncoming" + filterScript.hashCode();
    String sampleMsg = "From: sender@zimbra.com\n" + "To: test1@zimbra.com\n" + "Subject: Test\n"
        + "\n" + body;
    try {
      // Outgoing
      Account account = Provisioning.getInstance().getAccount(MockProvisioning.DEFAULT_ACCOUNT_ID);
      RuleManager.clearCachedRules(account);
      Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);
      account.setMailOutgoingSieveScript(filterScript);

      RuleManager.applyRulesToOutgoingMessage(
          new OperationContext(mbox), mbox,
          new ParsedMessage(sampleMsg.getBytes(), false),
          5, /* sent folder */
          true, 0, null, Mailbox.ID_AUTO_INCREMENT);
      List<Integer> searchedIds = TestUtil.search(mbox, "in:sent " + body, MailItem.Type.MESSAGE);
      assertEquals(1, searchedIds.size());
    } catch (Exception e) {
      e.printStackTrace();
      fail("No exception should be thrown");
    }
  }

  @Test
  void testNoCapability1() {
    // No "fileinto" declaration
    String filterPlainFileintoScript =
        "if header :contains \"Subject\" \"test\" { fileinto \"Junk\"; }";
    try {
      final String accountName = UUID.randomUUID() + "@" + UUID.randomUUID() + ".com";
      final Account account = provisioning.createAccount(accountName, "secret", new HashMap<>());
      RuleManager.clearCachedRules(account);
      Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);
      account.setMailSieveScript(filterPlainFileintoScript);
      String raw = "From: sender@zimbra.com\n" + "To: test1@zimbra.com\n" + "Subject: Test\n"
          + "\n" + "Hello World.";
      List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(new OperationContext(mbox),
          mbox, new ParsedMessage(raw.getBytes(), false), 0, account.getName(),
          new DeliveryContext(), Mailbox.ID_FOLDER_INBOX, true);
      assertEquals(1, ids.size());
      Message msg = mbox.getMessageById(null, ids.get(0).getId());
      assertEquals(Mailbox.ID_FOLDER_INBOX, msg.getFolderId());
    } catch (Exception e) {
      e.printStackTrace();
      fail("No exception should be thrown");
    }
  }

  @Test
  void testNoCapability2() {
    // declare "fileinto" without "copy"
    String filterPlainFileintoScript = "require \"fileinto\";\n"
        + "if header :contains \"Subject\" \"test\" {\n"
        + "  fileinto :copy \"copyAndJunk\";\n"
        + "}";
    try {
      final String accountName = UUID.randomUUID() + "@" + UUID.randomUUID() + ".com";
      final Account account = provisioning.createAccount(accountName, "secret", new HashMap<>());
      RuleManager.clearCachedRules(account);
      Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);
      account.setMailSieveScript(filterPlainFileintoScript);
      String raw = "From: sender@zimbra.com\n" + "To: test1@zimbra.com\n" + "Subject: Test\n"
          + "\n" + "Hello World.";

      // Capability string is mandatory ==> :copy extension will be failed
      account.setSieveRequireControlEnabled(true);
      List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(new OperationContext(mbox),
          mbox, new ParsedMessage(raw.getBytes(), false), 0, account.getName(),
          new DeliveryContext(), Mailbox.ID_FOLDER_INBOX, true);
      assertEquals(1, ids.size());
      Message msg = mbox.getMessageById(null, ids.get(0).getId());
      assertEquals(Mailbox.ID_FOLDER_INBOX, msg.getFolderId());

      // Capability string is optional ==> :copy extension should be available
      account.setSieveRequireControlEnabled(false);
      ids = RuleManager.applyRulesToIncomingMessage(new OperationContext(mbox),
          mbox, new ParsedMessage(raw.getBytes(), false), 0, account.getName(),
          new DeliveryContext(), Mailbox.ID_FOLDER_INBOX, true);
      assertEquals(2, ids.size());
      msg = mbox.getMessageById(null, ids.get(0).getId());
      assertEquals(Mailbox.ID_FOLDER_INBOX, msg.getFolderId());
      msg = mbox.getMessageById(null, ids.get(1).getId());
      Folder folder = mbox.getFolderById(null, msg.getFolderId());
      assertEquals("copyAndJunk", folder.getName());
    } catch (Exception e) {
      e.printStackTrace();
      fail("No exception should be thrown");
    }
  }

  @Test
  void testPlainFileIntoWithSpaces() {
    String filterScript = "require [\"fileinto\"];\n"
        + "fileinto \" abc\";"   // final destination folder = " abc"
        + "fileinto \"abc \";"   // final destination folder = "abc"
        + "fileinto \" abc \";"; // final destination folder = " abc"
    try {
      final String accountName = UUID.randomUUID() + "@" + UUID.randomUUID() + ".com";
      final Account account = provisioning.createAccount(accountName, "secret", new HashMap<>());
      RuleManager.clearCachedRules(account);
      Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);
      account.setMailSieveScript(filterScript);
      String raw = "From: sender@zimbra.com\n" + "To: test1@zimbra.com\n" + "Subject: Test\n"
          + "\n" + "Hello World.";
      List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(new OperationContext(mbox),
          mbox, new ParsedMessage(raw.getBytes(), false), 0, account.getName(),
          new DeliveryContext(), Mailbox.ID_FOLDER_INBOX, true);
      assertEquals(2, ids.size());
      Message msg = mbox.getMessageById(null, ids.get(0).getId());
      Folder folder = mbox.getFolderById(null, msg.getFolderId());
      assertEquals(" abc", folder.getName());
      msg = mbox.getMessageById(null, ids.get(1).getId());
      folder = mbox.getFolderById(null, msg.getFolderId());
      assertEquals("abc", folder.getName());
    } catch (Exception e) {
      e.printStackTrace();
      fail("No exception should be thrown");
    }
  }
}
