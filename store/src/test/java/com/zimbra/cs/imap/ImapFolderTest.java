// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.imap;

import static org.junit.jupiter.api.Assertions.*;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.mailbox.MailItem.Type;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ImapFolderTest extends MailboxTestSuite {

	private Account acct = null;
	private Mailbox mbox = null;

	@BeforeAll
	public static void init() throws Exception {
		MailboxTestUtil.initServer();
	}

	@BeforeEach
	public void setUp() throws Exception {
		acct = createAccount().create();
		mbox = MailboxManager.getInstance().getMailboxByAccount(acct);
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