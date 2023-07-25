// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index.query;

import java.util.HashMap;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.zimbra.cs.account.MockProvisioning;

import static org.junit.jupiter.api.Assertions.assertEquals;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.service.util.ItemId;

/**
 * Unit test for {@link InQuery}.
 *
 * @author ysasaki
 */
public final class InQueryTest {

    @BeforeAll
    public static void init() throws Exception {
        MailboxTestUtil.initServer();
        Provisioning prov = Provisioning.getInstance();
        prov.createAccount("test@zimbra.com", "secret", new HashMap<String, Object>());
    }

    @BeforeEach
    public void setUp() throws Exception {
        MailboxTestUtil.clearData();
    }

 @Test
 void inAnyFolder() throws Exception {
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(MockProvisioning.DEFAULT_ACCOUNT_ID);

  Query query = InQuery.create(mbox, new ItemId(MockProvisioning.DEFAULT_ACCOUNT_ID, 1), null, true);
  assertEquals("Q(UNDER:ANY_FOLDER)", query.toString());

  query = InQuery.create(mbox, new ItemId(MockProvisioning.DEFAULT_ACCOUNT_ID, 1), null, false);
  assertEquals("Q(IN:USER_ROOT)", query.toString());

  query = InQuery.create(mbox, new ItemId("1-1-1", 1), null, true);
  assertEquals("Q(UNDER:1-1-1:1)", query.toString());

  query = InQuery.create(mbox, new ItemId("1-1-1", 1), null, false);
  assertEquals("Q(IN:1-1-1:1)", query.toString());
 }

}
