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
import java.util.HashMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit test for {@link FieldQuery}.
 *
 * @author ysasaki
 */
public final class FieldQueryTest {
  private static Mailbox mailbox;

  @BeforeClass
  public static void init() throws Exception {
    MailboxTestUtil.initServer();
    Provisioning prov = Provisioning.getInstance();
    prov.createAccount("test@zimbra.com", "secret", new HashMap<String, Object>());
    mailbox =
        MailboxManager.getInstance().getMailboxByAccountId(MockProvisioning.DEFAULT_ACCOUNT_ID);
  }

  @Before
  public void setUp() throws Exception {
    MailboxTestUtil.clearData();
  }

  @Test
  public void textFieldQuery() throws Exception {
    Query query = FieldQuery.create(mailbox, "company", "zimbra");
    Assert.assertEquals("Q(l.field:company:zimbra)", query.toString());
  }

  @Test
  public void numericFieldQuery() throws Exception {
    Query query = FieldQuery.create(mailbox, "capacity", "3");
    Assert.assertEquals("Q(#capacity#:3)", query.toString());

    query = FieldQuery.create(mailbox, "capacity", ">3");
    Assert.assertEquals("Q(#capacity#:>3)", query.toString());

    query = FieldQuery.create(mailbox, "capacity", ">=3");
    Assert.assertEquals("Q(#capacity#:>=3)", query.toString());

    query = FieldQuery.create(mailbox, "capacity", "<-3");
    Assert.assertEquals("Q(#capacity#:<-3)", query.toString());

    query = FieldQuery.create(mailbox, "capacity", "<=-3");
    Assert.assertEquals("Q(#capacity#:<=-3)", query.toString());
  }

  @Test
  public void wildcard() throws Exception {
    Query query = FieldQuery.create(mailbox, "firstname", "*");
    Assert.assertEquals("Q(l.field:firstname:*[*])", query.toString());
  }
}
