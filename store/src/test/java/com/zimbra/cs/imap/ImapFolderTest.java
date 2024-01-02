// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.imap;

import java.util.HashMap;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.zimbra.cs.account.Account;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.MailItem.Type;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxTestUtil;

public class ImapFolderTest {
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
 void testGetSubsequence() throws Exception {
  ImapCredentials creds = new ImapCredentials(acct, ImapCredentials.EnabledHack.NONE);
  ImapPath path = new ImapPath("trash", creds);
  byte params = 0;

  ImapFolder i4folder = new ImapFolder(path, params, null);
  i4folder.cache(new ImapMessage(1, Type.of((byte) 5), 11, 0, null), true);
  i4folder.cache(new ImapMessage(2, Type.of((byte) 5), 12, 0, null), true);
  i4folder.cache(new ImapMessage(3, Type.of((byte) 5), 13, 0, null), true);
  LocalImapMailboxStore localStore = new LocalImapMailboxStore(mbox);
  Set<ImapMessage> i4set = i4folder.getSubsequence(null, "1,2", false);
  assertNotNull(i4set);
  assertEquals(2, i4set.size());

  i4set = i4folder.getSubsequence(null, "1:3", false);
  assertNotNull(i4set);
  assertEquals(3, i4set.size());
 }
}