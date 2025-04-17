// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index;

import static org.junit.jupiter.api.Assertions.*;

import com.zimbra.common.soap.SoapProtocol;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.DeliveryOptions;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.mime.ParsedMessage;
import java.util.EnumSet;
import java.util.HashMap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link IntersectionQueryOperation}.
 *
 * @author ysasaki
 */
public final class IntersectionQueryOperationTest {

	@BeforeAll
	public static void init() throws Exception {
		MailboxTestUtil.initServer();
	}

	@BeforeEach
	public void setUp() throws Exception {
		MailboxTestUtil.clearData();
	}

	@Test
	void optimize() throws Exception {
		final Account account = Provisioning.getInstance()
				.createAccount("test@zimbra.com", "secret", new HashMap<String, Object>());
		Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(account.getId());
		DeliveryOptions dopt = new DeliveryOptions().setFolderId(Mailbox.ID_FOLDER_INBOX);
		mbox.addMessage(null, new ParsedMessage("From: test1@zimbra.com".getBytes(), false), dopt,
				null);
		mbox.addMessage(null, new ParsedMessage("From: test2@zimbra.com".getBytes(), false), dopt,
				null);
		mbox.addMessage(null, new ParsedMessage("From: test3@zimbra.com".getBytes(), false), dopt,
				null);
		MailboxTestUtil.index(mbox);

		SearchParams params = new SearchParams();
		params.setQueryString("in:inbox from:none*"); // wildcard
		params.setTypes(EnumSet.of(MailItem.Type.MESSAGE));
		params.setSortBy(SortBy.NONE);
		ZimbraQuery query = new ZimbraQuery(new OperationContext(mbox), SoapProtocol.Soap12, mbox,
				params);
		// this wildcard expansion results in no hits
		assertEquals("ZQ: Q(IN:Inbox) && Q(from:none*[*])", query.toString());
		// then intersection of something and no hits is always no hits
		assertEquals("((from:none*) AND IN:/Inbox )", query.toQueryString());
		ZimbraQueryResults results = query.execute();
		assertFalse(results.hasNext());
		results.close();

		params = new SearchParams();
		params.setQueryString("in:inbox content:the"); // stop-word
		params.setTypes(EnumSet.of(MailItem.Type.MESSAGE));
		params.setSortBy(SortBy.NONE);
		query = new ZimbraQuery(new OperationContext(mbox), SoapProtocol.Soap12, mbox, params);
		assertEquals("ZQ: Q(IN:Inbox) && Q(l.content:)", query.toString());
		assertEquals("", query.toQueryString());
	}

}
