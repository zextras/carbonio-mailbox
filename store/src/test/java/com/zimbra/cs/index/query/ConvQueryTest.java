// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index.query;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.zextras.mailbox.util.AccountUtil;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public final class ConvQueryTest {

  @BeforeAll
  public static void init() throws Exception {
    MailboxTestUtil.initServer();
  }

  @BeforeEach
  public void setUp() throws Exception {
    MailboxTestUtil.clearData();
  }

 @Test
 void remoteConvId() throws Exception {
   final Account account = AccountUtil.createAccount();
   Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(account.getId());
  assertEquals("<DB[CONV:\"11111111-1111-1111-1111-111111111111:111\" ]>",
    ConvQuery.create(mbox, "11111111-1111-1111-1111-111111111111:111").compile(mbox, true).toString());
 }

}
