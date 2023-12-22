// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.imap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.zimbra.common.util.Pair;

import static org.junit.jupiter.api.Assertions.*;

import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.Folder;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.mailbox.Message;
import com.zimbra.cs.mailbox.Metadata;
import com.zimbra.cs.mailbox.MetadataList;
import qa.unittest.TestUtil;

public class LocalImapMailboxStoreTest {
    private static final String LOCAL_USER = "localimaptest@zimbra.com";
    private Account acct = null;
    private Mailbox mbox = null;
    @BeforeAll
    public static void init() throws Exception {
        MailboxTestUtil.initServer();
    }

    @BeforeEach
    public void setUp() throws Exception {
        MailboxTestUtil.clearData();
        Provisioning prov = Provisioning.getInstance();
        HashMap<String,Object> attrs = new HashMap<String,Object>();
        attrs.put(Provisioning.A_zimbraId, "12aa345b-2b47-44e6-8cb8-7fdfa18c1a9f");
        acct = prov.createAccount(LOCAL_USER, "secret", attrs);
        mbox = MailboxManager.getInstance().getMailboxByAccount(acct);
    }

    @AfterEach
    public void tearDown() throws Exception {
        MailboxTestUtil.clearData();
    }

 @Test
 void testListSubscriptions() throws Exception {
  String path = "testPath";
  MetadataList slist = new MetadataList();
  slist.add(path);

  //imitate subscription
  mbox.setConfig(null, "imap", new Metadata().put("subs", slist));

  //check that subscription was saved in mailbox configuration
  Metadata config = mbox.getConfig(null, "imap");
  assertNotNull(config);
  MetadataList rlist = config.getList("subs", true);
  assertNotNull(rlist);
  assertNotNull(rlist.get(0));
  assertTrue(rlist.get(0).equalsIgnoreCase(path));

  //test listSubscriptions method
  LocalImapMailboxStore localStore = new LocalImapMailboxStore(mbox);
  Set<String> subs = localStore.listSubscriptions(null);
  assertNotNull(subs);
  assertEquals(1, subs.size());
  String sub = subs.iterator().next();
  assertTrue(sub.equalsIgnoreCase(path));
 }

 @Test
 void testSaveSubscriptions() throws Exception {
  //verify that no subscriptions are saved yet
  LocalImapMailboxStore localStore = new LocalImapMailboxStore(mbox);
  Set<String> savedSubscriptions = localStore.listSubscriptions(null);
  assertEquals(0, savedSubscriptions.size());

  String path = "testPath";
  HashSet<String> subscriptions = new HashSet<String>();
  subscriptions.add(path);

  //test saving subscriptions
  localStore.saveSubscriptions(null, subscriptions);
  savedSubscriptions = localStore.listSubscriptions(null);
  assertNotNull(savedSubscriptions);
  assertEquals(1, savedSubscriptions.size());
  String sub = savedSubscriptions.iterator().next();
  assertTrue(sub.equalsIgnoreCase(path));
 }

 @Test
 void testOpenImapFolder() throws Exception {
  Folder folder = mbox.createFolder(null, "TestOpenImapFolder", new Folder.FolderOptions().setDefaultView(MailItem.Type.MESSAGE));
  int folderId = folder.getId();
  List<ImapMessage> expected = new LinkedList<ImapMessage>();
  for (int i = 1; i <= 10; i++) {
   Message msg = TestUtil.addMessage(mbox, folderId, String.format("imap message %s", i), System.currentTimeMillis());
   expected.add(new ImapMessage(msg));
  }
  //test opening folder without pagination
  List<ImapMessage> actual = mbox.openImapFolder(null, folderId);
  assertEquals(expected.size(), actual.size(), "expected and actual ImapMessage lists have different lengths");
  for (int i = 0; i < expected.size(); i++) {
   assertEquals(expected.get(i), actual.get(i));
  }
  //providing nulls for limit and cursor params should return all messages
  Pair<List<ImapMessage>, Boolean> paginated = mbox.openImapFolder(null, folderId, null, null);
  actual = paginated.getFirst();
  assertFalse(paginated.getSecond(), "hasMore should be False");
  assertEquals(expected.size(), actual.size(), "expected and actual ImapMessage lists have different lengths");
  for (int i = 0; i < expected.size(); i++) {
   assertEquals(expected.get(i), actual.get(i));
  }

  //test pagination without cursor
  paginated = mbox.openImapFolder(null, folderId, 5, null);
  actual = paginated.getFirst();
  assertEquals(5, actual.size(), "expected five results");
  for (int i = 0; i < actual.size(); i++) {
   assertEquals(expected.get(i).getMsgId(), actual.get(i).getMsgId());
  }
  assertTrue(paginated.getSecond());

  //test pagination with cursor
  int cursorId = actual.get(4).getMsgId();
  paginated = mbox.openImapFolder(null, folderId, 5, cursorId);
  actual = paginated.getFirst();
  assertEquals(5, actual.size(), "expected five results");
  assertEquals(5, actual.size());
  for (int i = 0; i < actual.size(); i++) {
   assertEquals(expected.get(i + 5).getMsgId(), actual.get(i).getMsgId());
  }
  assertFalse(paginated.getSecond());
 }
}