// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailbox;

import static org.junit.jupiter.api.Assertions.*;

import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import java.util.HashMap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link SearchFolder}.
 *
 * @author ysasaki
 */
public final class SearchFolderTest {
  private static Account account;

    @BeforeAll
    public static void init() throws Exception {
        MailboxTestUtil.initServer();
        Provisioning prov = Provisioning.getInstance();
        account = prov.createAccount("test@zimbra.com", "secret", new HashMap<String, Object>());
    }

    @BeforeEach
    public void setUp() throws Exception {
        MailboxTestUtil.clearData();
    }

 @Test
 void defaultFolderFlags() throws Exception {
  account.setDefaultFolderFlags("*");
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(account.getId());
  SearchFolder folder = mbox.createSearchFolder(null, Mailbox.ID_FOLDER_USER_ROOT,
    "test", "test", "message", "none", 0, (byte) 0);
  assertTrue(folder.isFlagSet(Flag.BITMASK_SUBSCRIBED));
 }

 @Test
 void flagGuard() throws Exception {
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(account.getId());
  SearchFolder folder = mbox.createSearchFolder(null, Mailbox.ID_FOLDER_USER_ROOT,
    "test", "test", "message", "none", Flag.BITMASK_UNCACHED, (byte) 0);
  assertFalse(folder.isFlagSet(Flag.BITMASK_UNCACHED));
 }

}
