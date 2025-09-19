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
    final Account account = AccountUtil.createAccount();
    mailbox = MailboxManager.getInstance().getMailboxByAccountId(account.getId());
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
