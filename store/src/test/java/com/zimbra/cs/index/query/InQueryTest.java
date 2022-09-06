// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index.query;

import com.zimbra.cs.account.MockProvisioning;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.service.util.ItemId;
import java.util.HashMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit test for {@link InQuery}.
 *
 * @author ysasaki
 */
public final class InQueryTest {

  @BeforeClass
  public static void init() throws Exception {
    MailboxTestUtil.initServer();
    Provisioning prov = Provisioning.getInstance();
    prov.createAccount("test@zimbra.com", "secret", new HashMap<String, Object>());
  }

  @Before
  public void setUp() throws Exception {
    MailboxTestUtil.clearData();
  }

  @Test
  public void inAnyFolder() throws Exception {
    Mailbox mbox =
        MailboxManager.getInstance().getMailboxByAccountId(MockProvisioning.DEFAULT_ACCOUNT_ID);

    Query query =
        InQuery.create(mbox, new ItemId(MockProvisioning.DEFAULT_ACCOUNT_ID, 1), null, true);
    Assert.assertEquals("Q(UNDER:ANY_FOLDER)", query.toString());

    query = InQuery.create(mbox, new ItemId(MockProvisioning.DEFAULT_ACCOUNT_ID, 1), null, false);
    Assert.assertEquals("Q(IN:USER_ROOT)", query.toString());

    query = InQuery.create(mbox, new ItemId("1-1-1", 1), null, true);
    Assert.assertEquals("Q(UNDER:1-1-1:1)", query.toString());

    query = InQuery.create(mbox, new ItemId("1-1-1", 1), null, false);
    Assert.assertEquals("Q(IN:1-1-1:1)", query.toString());
  }
}
