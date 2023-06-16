// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.collect.Lists;

import static org.junit.jupiter.api.Assertions.*;

import com.zimbra.common.soap.SoapProtocol;
import com.zimbra.cs.account.MockProvisioning;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.DeliveryOptions;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.mailbox.Message;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.mime.ParsedMessage;

/**
 * Unit test for {@link LuceneQueryOperation}.
 *
 * @author ysasaki
 */
public final class LuceneQueryOperationTest {

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
 void notClause() throws Exception {
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(MockProvisioning.DEFAULT_ACCOUNT_ID);
  DeliveryOptions dopt = new DeliveryOptions().setFolderId(Mailbox.ID_FOLDER_INBOX);
  mbox.addMessage(null, new ParsedMessage("From: test1@zimbra.com".getBytes(), false), dopt, null);
  Message msg2 = mbox.addMessage(null, new ParsedMessage("From: test2@zimbra.com".getBytes(), false), dopt, null);
  Message msg3 = mbox.addMessage(null, new ParsedMessage("From: test3@zimbra.com".getBytes(), false), dopt, null);
  MailboxTestUtil.index(mbox);

  SearchParams params = new SearchParams();
  params.setQueryString("-from:test1@zimbra.com");
  params.setTypes(EnumSet.of(MailItem.Type.MESSAGE));
  params.setSortBy(SortBy.NONE);
  ZimbraQuery query = new ZimbraQuery(new OperationContext(mbox), SoapProtocol.Soap12, mbox, params);
  ZimbraQueryResults results = query.execute();
  List<Integer> expecteds = Lists.newArrayList();
  List<Integer> matches = Lists.newArrayList();
  assertTrue(results.hasNext());
  matches.add(results.getNext().getItemId());
  assertTrue(results.hasNext());
  matches.add(results.getNext().getItemId());
  assertFalse(results.hasNext());
  expecteds.add(msg2.getId());
  expecteds.add(msg3.getId());
  Collections.sort(matches);
  Collections.sort(expecteds);
  assertEquals(expecteds.get(0), matches.get(0), "Match Item ID");
  assertEquals(expecteds.get(1), matches.get(1), "Match Item ID");
  results.close();
 }

 @Test
 void notClauses() throws Exception {
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(MockProvisioning.DEFAULT_ACCOUNT_ID);
  DeliveryOptions dopt = new DeliveryOptions().setFolderId(Mailbox.ID_FOLDER_INBOX);
  mbox.addMessage(null, new ParsedMessage("From: test1@zimbra.com".getBytes(), false), dopt, null);
  Message msg2 = mbox.addMessage(null, new ParsedMessage("From: test2@zimbra.com".getBytes(), false), dopt, null);
  Message msg3 = mbox.addMessage(null, new ParsedMessage("From: test3@zimbra.com".getBytes(), false), dopt, null);
  MailboxTestUtil.index(mbox);

  SearchParams params = new SearchParams();
  params.setQueryString("-from:(test1 zimbra.com)");
  params.setTypes(EnumSet.of(MailItem.Type.MESSAGE));
  params.setSortBy(SortBy.NONE);
  ZimbraQuery query = new ZimbraQuery(new OperationContext(mbox), SoapProtocol.Soap12, mbox, params);
  ZimbraQueryResults results = query.execute();
  List<Integer> expecteds = Lists.newArrayList();
  List<Integer> matches = Lists.newArrayList();
  assertTrue(results.hasNext());
  matches.add(results.getNext().getItemId());
  assertTrue(results.hasNext());
  matches.add(results.getNext().getItemId());
  assertFalse(results.hasNext());
  expecteds.add(msg2.getId());
  expecteds.add(msg3.getId());
  Collections.sort(matches);
  Collections.sort(expecteds);
  assertEquals(expecteds.get(0), matches.get(0), "Match Item ID");
  assertEquals(expecteds.get(1), matches.get(1), "Match Item ID");
  results.close();
 }

 @Test
 void andClauses() throws Exception {
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(MockProvisioning.DEFAULT_ACCOUNT_ID);
  DeliveryOptions dopt = new DeliveryOptions().setFolderId(Mailbox.ID_FOLDER_INBOX);
  Message msg1 = mbox.addMessage(null, new ParsedMessage("From: test1@zimbra.com".getBytes(), false), dopt, null);
  mbox.addMessage(null, new ParsedMessage("From: test2@zimbra.com".getBytes(), false), dopt, null);
  mbox.addMessage(null, new ParsedMessage("From: test3@zimbra.com".getBytes(), false), dopt, null);
  MailboxTestUtil.index(mbox);

  SearchParams params = new SearchParams();
  params.setQueryString("from:test1 from:zimbra.com -from:vmware.com");
  params.setTypes(EnumSet.of(MailItem.Type.MESSAGE));
  params.setSortBy(SortBy.NONE);
  ZimbraQuery query = new ZimbraQuery(new OperationContext(mbox), SoapProtocol.Soap12, mbox, params);
  ZimbraQueryResults results = query.execute();
  assertTrue(results.hasNext());
  assertEquals(msg1.getId(), results.getNext().getItemId());
  assertFalse(results.hasNext());
  results.close();
 }

 @Test
 void subjectQuery() throws Exception {
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(MockProvisioning.DEFAULT_ACCOUNT_ID);
  DeliveryOptions dopt = new DeliveryOptions().setFolderId(Mailbox.ID_FOLDER_INBOX);
  Message msg = mbox.addMessage(null, new ParsedMessage("Subject: one two three".getBytes(), false), dopt, null);
  MailboxTestUtil.index(mbox);

  // phrase query
  SearchParams params = new SearchParams();
  params.setQueryString("subject:\"one two three\"");
  params.setTypes(EnumSet.of(MailItem.Type.MESSAGE));
  params.setSortBy(SortBy.NONE);
  ZimbraQuery query = new ZimbraQuery(new OperationContext(mbox), SoapProtocol.Soap12, mbox, params);
  ZimbraQueryResults results = query.execute();
  assertTrue(results.hasNext());
  assertEquals(msg.getId(), results.getNext().getItemId());
  results.close();

  // verify subject is not repeated during index
  params = new SearchParams();
  params.setQueryString("subject:\"three one\"");
  params.setTypes(EnumSet.of(MailItem.Type.MESSAGE));
  params.setSortBy(SortBy.NONE);
  query = new ZimbraQuery(new OperationContext(mbox), SoapProtocol.Soap12, mbox, params);
  results = query.execute();
  assertFalse(results.hasNext());
  results.close();
 }

}
