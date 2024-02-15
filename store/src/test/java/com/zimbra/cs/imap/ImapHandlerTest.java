// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.imap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.zimbra.common.localconfig.LC;
import com.zimbra.common.mailbox.FolderStore;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.mailbox.Message;
import com.zimbra.cs.mailbox.SearchFolder;
import com.zimbra.cs.server.ServerThrottle;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import qa.unittest.TestUtil;


class ImapHandlerTest {

  @BeforeEach
  public void setUp() throws Exception {
    LC.imap_use_ehcache.setDefault(false);
    MailboxTestUtil.initServer();
    String[] hosts = {"localhost", "127.0.0.1"};
    ServerThrottle.configureThrottle(new ImapConfig(false).getProtocol(), 100, 100,
        Arrays.asList(hosts), Arrays.asList(hosts));
  }

  private Account createTestAccount(Provisioning prov, String accountName) throws ServiceException {
    HashMap<String, Object> attrs = new HashMap<>();
    attrs.put(Provisioning.A_zimbraId, accountName);
    attrs.put(Provisioning.A_zimbraFeatureAntispamEnabled, "true");
    return prov.createAccount(accountName, "secret", attrs);
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
  void testDoCOPYByUID() {

    try {
      Account acct = createTestAccount(Provisioning.getInstance(),
          UUID.randomUUID() + "@" + UUID.randomUUID() + ".com");
      acct.setFeatureAntispamEnabled(true);
      Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(acct);
      Message m1 = TestUtil.addMessage(mbox, "Message 1");
      Message m2 = TestUtil.addMessage(mbox, "Message 2");
      Message m3 = TestUtil.addMessage(mbox, "Message 3");
      assertEquals(Mailbox.ID_FOLDER_INBOX, mbox.getMessageById(null, m1.getId()).getFolderId());
      assertEquals(Mailbox.ID_FOLDER_INBOX, mbox.getMessageById(null, m2.getId()).getFolderId());
      assertEquals(Mailbox.ID_FOLDER_INBOX, mbox.getMessageById(null, m3.getId()).getFolderId());
      ImapHandler handler = new MockImapHandler();
      ImapCredentials creds = new ImapCredentials(acct, ImapCredentials.EnabledHack.NONE);
      ImapPath pathSpam = new MockImapPath(null, mbox.getFolderById(null, Mailbox.ID_FOLDER_SPAM),
          creds);
      ImapPath pathInbox = new MockImapPath(null,
          mbox.getFolderById(null, Mailbox.ID_FOLDER_INBOX), creds);
      handler.setCredentials(creds);
      byte params = 0;
      handler.setSelectedFolder(pathSpam, params);
      String sequenceSet = String.format("%d,%d,%d", m1.getId(), m2.getId(), m3.getId());
      assertTrue(handler.doCOPY(null, sequenceSet, pathInbox, true));
      List<Integer> newIds = TestUtil.search(mbox, "in:Inbox", MailItem.Type.MESSAGE);
      assertEquals(3, newIds.size());
      assertEquals(Mailbox.ID_FOLDER_INBOX, mbox.getMessageById(null, newIds.get(0)).getFolderId());
      assertEquals(Mailbox.ID_FOLDER_INBOX, mbox.getMessageById(null, newIds.get(1)).getFolderId());
      assertEquals(Mailbox.ID_FOLDER_INBOX, mbox.getMessageById(null, newIds.get(2)).getFolderId());
      /* Note, messages may not be returned in the original order */
      assertTrue(newIds.contains(m1.getId()),
          String.format("message IDs should not have changed 1st ID=%s newIds=%s", m1.getId(),
              newIds));
      assertTrue(newIds.contains(m2.getId()),
          String.format("message IDs should not have changed 2nd ID=%s newIds=%s", m2.getId(),
              newIds));
      assertTrue(newIds.contains(m3.getId()),
          String.format("message IDs should not have changed 3rd ID=%s newIds=%s", m3.getId(),
              newIds));

      handler.setSelectedFolder(pathInbox, params);
      ImapFolder i4folder = handler.getSelectedFolder();
      assertEquals(3, i4folder.getSize());
      assertTrue(handler.doCOPY(null, sequenceSet, pathSpam, true));
      newIds = TestUtil.search(mbox, "in:junk", MailItem.Type.MESSAGE);
      assertEquals(3, newIds.size());
      assertFalse(newIds.contains(m1.getId()), "Message IDs should have changed");
      assertFalse(newIds.contains(m3.getId()), "Message IDs should have changed");
      assertFalse(newIds.contains(m3.getId()), "Message IDs should have changed");

      assertEquals(Mailbox.ID_FOLDER_SPAM, mbox.getMessageById(null, newIds.get(0)).getFolderId(),
          "Message should have been copied to Junk");
      assertEquals(Mailbox.ID_FOLDER_SPAM, mbox.getMessageById(null, newIds.get(1)).getFolderId(),
          "Message should have been copied to Junk");
      assertEquals(Mailbox.ID_FOLDER_SPAM, mbox.getMessageById(null, newIds.get(2)).getFolderId(),
          "Message should have been copied to Junk");

      assertEquals(Mailbox.ID_FOLDER_INBOX, mbox.getMessageById(null, m1.getId()).getFolderId(),
          "original messages should have stayed in inbox");
      assertEquals(Mailbox.ID_FOLDER_INBOX, mbox.getMessageById(null, m2.getId()).getFolderId(),
          "original messages should have stayed in inbox");
      assertEquals(Mailbox.ID_FOLDER_INBOX, mbox.getMessageById(null, m3.getId()).getFolderId(),
          "original messages should have stayed in inbox");
    } catch (Exception e) {
      fail("No error should be thrown");
      e.printStackTrace();
    }
  }

  @Test
  void testDoCOPYByNumber() throws Exception {

    Account acct = createTestAccount(Provisioning.getInstance(),
        UUID.randomUUID() + "@" + UUID.randomUUID() + ".com");
    acct.setFeatureAntispamEnabled(true);
    Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(acct);
    Message m1 = TestUtil.addMessage(mbox, "Message 1");
    Message m2 = TestUtil.addMessage(mbox, "Message 2");
    Message m3 = TestUtil.addMessage(mbox, "Message 3");
    assertEquals(Mailbox.ID_FOLDER_INBOX, mbox.getMessageById(null, m1.getId()).getFolderId());
    assertEquals(Mailbox.ID_FOLDER_INBOX, mbox.getMessageById(null, m2.getId()).getFolderId());
    assertEquals(Mailbox.ID_FOLDER_INBOX, mbox.getMessageById(null, m3.getId()).getFolderId());
    ImapHandler handler = new MockImapHandler();
    ImapCredentials creds = new ImapCredentials(acct, ImapCredentials.EnabledHack.NONE);
    ImapPath pathSpam = new MockImapPath(null, mbox.getFolderById(null, Mailbox.ID_FOLDER_SPAM),
        creds);
    ImapPath pathInbox = new MockImapPath(null,
        mbox.getFolderById(null, Mailbox.ID_FOLDER_INBOX), creds);
    handler.setCredentials(creds);
    byte params = 0;
    handler.setSelectedFolder(pathSpam, params);
    String sequenceSet = String.format("%d,%d,%d", m1.getId(), m2.getId(), m3.getId());
    boolean thrown = false;
    try {
      handler.doCOPY(null, sequenceSet, pathInbox, false);
    } catch (ImapParseException ex) {
      thrown = true;
    }
    assertTrue(thrown, "Should have thrown 'Invalid Message Sequence Number'");
    assertEquals(Mailbox.ID_FOLDER_INBOX, mbox.getMessageById(null, m1.getId()).getFolderId());
    assertEquals(Mailbox.ID_FOLDER_INBOX, mbox.getMessageById(null, m2.getId()).getFolderId());
    assertEquals(Mailbox.ID_FOLDER_INBOX, mbox.getMessageById(null, m3.getId()).getFolderId());

    sequenceSet = "1:3";
    handler.setSelectedFolder(pathInbox, params);
    assertTrue(handler.doCOPY(null, sequenceSet, pathSpam, true));
    assertEquals(Mailbox.ID_FOLDER_INBOX, mbox.getMessageById(null, m1.getId()).getFolderId(),
        "Original message should have stayed in Inbox");
    assertEquals(Mailbox.ID_FOLDER_INBOX, mbox.getMessageById(null, m2.getId()).getFolderId(),
        "Original message should have stayed in Inbox");
    assertEquals(Mailbox.ID_FOLDER_INBOX, mbox.getMessageById(null, m3.getId()).getFolderId(),
        "Original message should have stayed in Inbox");
    List<Integer> newIds = TestUtil.search(mbox, "in:junk", MailItem.Type.MESSAGE);
    assertEquals(0, newIds.size(),
        "should not have copied anything to Junk with an invalid sequence set");

    ImapFolder i4folder = handler.getSelectedFolder();
    assertEquals(3, i4folder.getSize());
    assertTrue(handler.doCOPY(null, sequenceSet, pathSpam, false));
    newIds = TestUtil.search(mbox, "in:junk", MailItem.Type.MESSAGE);
    assertFalse(newIds.contains(m1.getId()), "Message IDs should have changed");
    assertFalse(newIds.contains(m3.getId()), "Message IDs should have changed");
    assertFalse(newIds.contains(m3.getId()), "Message IDs should have changed");
    assertEquals(Mailbox.ID_FOLDER_SPAM, mbox.getMessageById(null, newIds.get(0)).getFolderId(),
        "Message should have been copied to Junk");
    assertEquals(Mailbox.ID_FOLDER_SPAM, mbox.getMessageById(null, newIds.get(1)).getFolderId(),
        "Message should have been copied to Junk");
    assertEquals(Mailbox.ID_FOLDER_SPAM, mbox.getMessageById(null, newIds.get(2)).getFolderId(),
        "Message should have been copied to Junk");

    assertEquals(Mailbox.ID_FOLDER_INBOX, mbox.getMessageById(null, m1.getId()).getFolderId(),
        "original messages should have stayed in inbox");
    assertEquals(Mailbox.ID_FOLDER_INBOX, mbox.getMessageById(null, m2.getId()).getFolderId(),
        "original messages should have stayed in inbox");
    assertEquals(Mailbox.ID_FOLDER_INBOX, mbox.getMessageById(null, m3.getId()).getFolderId(),
        "original messages should have stayed in inbox");

  }

  @Test
  void testDoSearch() throws Exception {

    Account acct = createTestAccount(Provisioning.getInstance(),
        UUID.randomUUID() + "@" + UUID.randomUUID() + ".com");
    acct.setFeatureAntispamEnabled(true);
    Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(acct);
    Message m1 = TestUtil.addMessage(mbox, "Message 1 blue");
    Message m2 = TestUtil.addMessage(mbox, "Message 2 green red");
    Message m3 = TestUtil.addMessage(mbox, "Message 3 green white");
    assertEquals(Mailbox.ID_FOLDER_INBOX, mbox.getMessageById(null, m1.getId()).getFolderId());
    assertEquals(Mailbox.ID_FOLDER_INBOX, mbox.getMessageById(null, m2.getId()).getFolderId());
    assertEquals(Mailbox.ID_FOLDER_INBOX, mbox.getMessageById(null, m3.getId()).getFolderId());

    Thread.sleep(500);
    ImapHandler handler = new MockImapHandler();
    ImapCredentials creds = new ImapCredentials(acct, ImapCredentials.EnabledHack.NONE);
    ImapPath pathInbox = new MockImapPath(null,
        mbox.getFolderById(null, Mailbox.ID_FOLDER_INBOX), creds);
    handler.setCredentials(creds);
    byte params = 0;
    handler.setSelectedFolder(pathInbox, params);
    Integer options = null;
    ImapSearch.LogicalOperation i4srch = new ImapSearch.AndOperation();
    ImapSearch child = new ImapSearch.AndOperation(new ImapSearch.FlagSearch("\\Recent"),
        new ImapSearch.NotOperation(new ImapSearch.FlagSearch("\\Seen")));
    i4srch.addChild(child);
    i4srch.addChild(new ImapSearch.ContentSearch("green"));
    assertTrue(handler.doSEARCH("searchtag", i4srch, false, options));
    ByteArrayOutputStream baos = (ByteArrayOutputStream) handler.output;
    assertEquals("* SEARCH 2 3\r\nsearchtag OK SEARCH completed\r\n", baos.toString(),
        "Output of SEARCH");
  }

  @Test
  void testSearchInSearchFolder() throws Exception {

    Account acct = createTestAccount(Provisioning.getInstance(),
        UUID.randomUUID() + "@" + UUID.randomUUID() + ".com");
    acct.setFeatureAntispamEnabled(true);
    Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(acct);
    Message m1 = TestUtil.addMessage(mbox, "Message 1 blue");
    Message m2 = TestUtil.addMessage(mbox, "Message 2 green red");
    Message m3 = TestUtil.addMessage(mbox, "Message 3 green white");
    SearchFolder searchFolder = mbox.createSearchFolder(null, Mailbox.ID_FOLDER_USER_ROOT,
        "lookForGreen" /* name */, "green" /* query */, "message", "none", 0, (byte) 0);
    assertEquals(Mailbox.ID_FOLDER_INBOX, mbox.getMessageById(null, m1.getId()).getFolderId());
    assertEquals(Mailbox.ID_FOLDER_INBOX, mbox.getMessageById(null, m2.getId()).getFolderId());
    assertEquals(Mailbox.ID_FOLDER_INBOX, mbox.getMessageById(null, m3.getId()).getFolderId());

    ImapHandler handler = new MockImapHandler();
    ImapCredentials creds = new ImapCredentials(acct, ImapCredentials.EnabledHack.NONE);
    ImapPath pathSearchFldr = new MockImapPath(null, searchFolder, creds);
    handler.setCredentials(creds);
    byte params = 0;
    handler.setSelectedFolder(pathSearchFldr, params);
    Integer options = null;
    ImapSearch.LogicalOperation i4srch = new ImapSearch.AndOperation();
    ImapSearch child = new ImapSearch.AndOperation(new ImapSearch.FlagSearch("\\Recent"),
        new ImapSearch.NotOperation(new ImapSearch.FlagSearch("\\Seen")));
    i4srch.addChild(child);
    i4srch.addChild(new ImapSearch.ContentSearch("white"));
    assertTrue(handler.doSEARCH("searchtag", i4srch, false, options));
    ByteArrayOutputStream baos = (ByteArrayOutputStream) handler.output;
    assertEquals("* SEARCH 2\r\nsearchtag OK SEARCH completed\r\n", baos.toString(),
        "Output of SEARCH");
  }

  @Test
  void testLogin() throws Exception {

    Account acct = createTestAccount(Provisioning.getInstance(),
        UUID.randomUUID() + "@" + UUID.randomUUID() + ".com");
    ImapHandler handler = new MockImapHandler();

    acct.setImapEnabled(true);
    acct.setPrefImapEnabled(true);
    handler.setCredentials(null);
    assertTrue(handler.authenticate(acct.getName(), null, "secret", "logintag", null));
    assertTrue(handler.isAuthenticated());

    acct.setImapEnabled(true);
    acct.setPrefImapEnabled(false);
    handler.setCredentials(null);
    assertTrue(handler.authenticate(acct.getName(), null, "secret", "logintag", null));
    assertFalse(handler.isAuthenticated());

    acct.setImapEnabled(false);
    acct.setPrefImapEnabled(true);
    handler.setCredentials(null);
    assertTrue(handler.authenticate(acct.getName(), null, "secret", "logintag", null));
    assertFalse(handler.isAuthenticated());

    acct.setImapEnabled(false);
    acct.setPrefImapEnabled(false);
    handler.setCredentials(null);
    assertTrue(handler.authenticate(acct.getName(), null, "secret", "logintag", null));
    assertFalse(handler.isAuthenticated());
  }

  static class MockImapPath extends ImapPath {

    MockImapPath(String owner, FolderStore folderStore, ImapCredentials creds)
        throws ServiceException {
      super(owner, folderStore, creds);
    }

    @Override
    protected boolean isSelectable() {
      return true;
    }

    @Override
    protected boolean isWritable() {
      return true;
    }

    @Override
    protected boolean isWritable(short rights) {
      return true;
    }
  }
}
