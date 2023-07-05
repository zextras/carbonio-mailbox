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

/**
 * Unit test for {@link FieldQuery}.
 *
 * @author ysasaki
 */
public final class FieldQueryTest {
    private static Mailbox mailbox;

    @BeforeAll
    public static void init() throws Exception {
        MailboxTestUtil.initServer();
        Provisioning prov = Provisioning.getInstance();
        prov.createAccount("test@zimbra.com", "secret", new HashMap<String, Object>());
        mailbox = MailboxManager.getInstance().getMailboxByAccountId(MockProvisioning.DEFAULT_ACCOUNT_ID);
    }

    @BeforeEach
    public void setUp() throws Exception {
        MailboxTestUtil.clearData();
    }

 @Test
 void textFieldQuery() throws Exception {
  Query query = FieldQuery.create(mailbox, "company", "zimbra");
  assertEquals("Q(l.field:company:zimbra)", query.toString());
 }

 @Test
 void numericFieldQuery() throws Exception {
  Query query = FieldQuery.create(mailbox, "capacity", "3");
  assertEquals("Q(#capacity#:3)", query.toString());

  query = FieldQuery.create(mailbox, "capacity", ">3");
  assertEquals("Q(#capacity#:>3)", query.toString());

  query = FieldQuery.create(mailbox, "capacity", ">=3");
  assertEquals("Q(#capacity#:>=3)", query.toString());

  query = FieldQuery.create(mailbox, "capacity", "<-3");
  assertEquals("Q(#capacity#:<-3)", query.toString());

  query = FieldQuery.create(mailbox, "capacity", "<=-3");
  assertEquals("Q(#capacity#:<=-3)", query.toString());
 }

 @Test
 void wildcard() throws Exception {
  Query query = FieldQuery.create(mailbox, "firstname", "*");
  assertEquals("Q(l.field:firstname:*[*])", query.toString());
 }

}
