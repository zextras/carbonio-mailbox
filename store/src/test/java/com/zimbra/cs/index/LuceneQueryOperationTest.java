// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index;

import static org.junit.jupiter.api.Assertions.*;

import com.google.common.collect.Lists;
import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.common.soap.SoapProtocol;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.mailbox.DeliveryOptions;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.mailbox.Message;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.mime.ParsedMessage;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link LuceneQueryOperation}.
 *
 * @author ysasaki
 */

@Tag("flaky")
public final class LuceneQueryOperationTest extends MailboxTestSuite {

	private Account account;
	private String uniqueId;

	@BeforeEach
	public void init() throws Exception {
		account = createAccount().create();
		uniqueId = UUID.randomUUID().toString();
	}

	@Test
	void notClause() throws Exception {
		String domain = uniqueId + ".com";
		String addr1 = "user1@" + domain;
		String addr2 = "user2@" + domain;
		String addr3 = "user3@" + domain;
		Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(account.getId());
		DeliveryOptions dopt = new DeliveryOptions().setFolderId(Mailbox.ID_FOLDER_INBOX);
		mbox.addMessage(null, new ParsedMessage(("From: " + addr1).getBytes(), false), dopt,
				null);
		Message msg2 = mbox.addMessage(null,
				new ParsedMessage(("From: " + addr2).getBytes(), false), dopt, null);
		Message msg3 = mbox.addMessage(null,
				new ParsedMessage(("From: " + addr3).getBytes(), false), dopt, null);
		MailboxTestUtil.index(mbox);

		SearchParams params = new SearchParams();
		params.setQueryString("-from:" + addr1);
		params.setTypes(EnumSet.of(MailItem.Type.MESSAGE));
		params.setSortBy(SortBy.NONE);
		ZimbraQuery query = new ZimbraQuery(new OperationContext(mbox), SoapProtocol.Soap12, mbox,
				params);
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
		String domain = uniqueId + ".com";
		String addr1 = "user1@" + domain;
		String addr2 = "user2@" + domain;
		String addr3 = "user3@" + domain;
		Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(account.getId());
		DeliveryOptions dopt = new DeliveryOptions().setFolderId(Mailbox.ID_FOLDER_INBOX);
		mbox.addMessage(null, new ParsedMessage(("From: " + addr1).getBytes(), false), dopt,
				null);
		Message msg2 = mbox.addMessage(null,
				new ParsedMessage(("From: " + addr2).getBytes(), false), dopt, null);
		Message msg3 = mbox.addMessage(null,
				new ParsedMessage(("From: " + addr3).getBytes(), false), dopt, null);
		MailboxTestUtil.index(mbox);

		SearchParams params = new SearchParams();
		params.setQueryString("-from:(user1 " + domain + ")");
		params.setTypes(EnumSet.of(MailItem.Type.MESSAGE));
		params.setSortBy(SortBy.NONE);
		ZimbraQuery query = new ZimbraQuery(new OperationContext(mbox), SoapProtocol.Soap12, mbox,
				params);
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
		String domain = uniqueId + ".com";
		String addr1 = "user1@" + domain;
		String addr2 = "user2@" + domain;
		String addr3 = "user3@" + domain;
		Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(account.getId());
		DeliveryOptions dopt = new DeliveryOptions().setFolderId(Mailbox.ID_FOLDER_INBOX);
		Message msg1 = mbox.addMessage(null,
				new ParsedMessage(("From: " + addr1).getBytes(), false), dopt, null);
		mbox.addMessage(null, new ParsedMessage(("From: " + addr2).getBytes(), false), dopt,
				null);
		mbox.addMessage(null, new ParsedMessage(("From: " + addr3).getBytes(), false), dopt,
				null);
		MailboxTestUtil.index(mbox);

		SearchParams params = new SearchParams();
		params.setQueryString("from:user1 from:" + domain + " -from:vmware.com");
		params.setTypes(EnumSet.of(MailItem.Type.MESSAGE));
		params.setSortBy(SortBy.NONE);
		ZimbraQuery query = new ZimbraQuery(new OperationContext(mbox), SoapProtocol.Soap12, mbox,
				params);
		ZimbraQueryResults results = query.execute();
		assertTrue(results.hasNext());
		assertEquals(msg1.getId(), results.getNext().getItemId());
		assertFalse(results.hasNext());
		results.close();
	}

	@Test
	void subjectQuery() throws Exception {
		Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(account.getId());
		DeliveryOptions dopt = new DeliveryOptions().setFolderId(Mailbox.ID_FOLDER_INBOX);
		Message msg = mbox.addMessage(null,
				new ParsedMessage("Subject: one two three".getBytes(), false), dopt, null);
		MailboxTestUtil.index(mbox);

		// phrase query
		SearchParams params = new SearchParams();
		params.setQueryString("subject:\"one two three\"");
		params.setTypes(EnumSet.of(MailItem.Type.MESSAGE));
		params.setSortBy(SortBy.NONE);
		ZimbraQuery query = new ZimbraQuery(new OperationContext(mbox), SoapProtocol.Soap12, mbox,
				params);
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

	@Test
	void subjectQueryPhrase() throws Exception {
		Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(account.getId());
		DeliveryOptions dopt = new DeliveryOptions().setFolderId(Mailbox.ID_FOLDER_INBOX);
		Message msg1 = mbox.addMessage(null,
				new ParsedMessage("Subject: one two three".getBytes(), false), dopt, null);
		Message msg2 = mbox.addMessage(null,
				new ParsedMessage("Subject: one three two".getBytes(), false), dopt, null);
		MailboxTestUtil.index(mbox);

		SearchParams params = new SearchParams();
		params.setQueryString("subject:\"one two three\"");
		params.setTypes(EnumSet.of(MailItem.Type.MESSAGE));
		params.setSortBy(SortBy.NONE);

		ZimbraQuery query = new ZimbraQuery(new OperationContext(mbox), SoapProtocol.Soap12, mbox,
				params);
		ZimbraQueryResults results = query.execute();

		assertTrue(results.hasNext());
		assertEquals(msg1.getId(), results.getNext().getItemId());

		assertFalse(results.hasNext());
		results.close();
	}

	@Test
	void subjectQueryPhraseMultiMatches() throws Exception {
		Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(account.getId());
		DeliveryOptions dopt = new DeliveryOptions().setFolderId(Mailbox.ID_FOLDER_INBOX);
		Message msg1 = mbox.addMessage(null,
				new ParsedMessage("Subject: one two three".getBytes(), false), dopt, null);
		Message msg2 = mbox.addMessage(null,
				new ParsedMessage("Subject: one two three".getBytes(), false), dopt, null);
		MailboxTestUtil.index(mbox);

		SearchParams params = new SearchParams();
		params.setQueryString("subject:\"one two three\"");
		params.setTypes(EnumSet.of(MailItem.Type.MESSAGE));
		params.setSortBy(SortBy.NONE);

		ZimbraQuery query = new ZimbraQuery(new OperationContext(mbox), SoapProtocol.Soap12, mbox,
				params);
		ZimbraQueryResults results = query.execute();

		assertTrue(results.hasNext());
		assertEquals(msg1.getId(), results.getNext().getItemId());

		assertTrue(results.hasNext());
		assertEquals(msg2.getId(), results.getNext().getItemId());
		results.close();
	}
}
