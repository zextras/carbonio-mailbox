// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index.query;

import static org.junit.jupiter.api.Assertions.*;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.service.util.ItemId;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link InQuery}.
 *
 * @author ysasaki
 */
public final class InQueryTest extends MailboxTestSuite {

	@Test
	void inAnyFolder() throws Exception {
		final Account account = createAccount().create();
		Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);

		Query query = InQuery.create(mbox, new ItemId(account.getId(), 1), null, true);
		assertEquals("Q(UNDER:ANY_FOLDER)", query.toString());

		query = InQuery.create(mbox, new ItemId(account.getId(), 1), null, false);
		assertEquals("Q(IN:USER_ROOT)", query.toString());

		query = InQuery.create(mbox, new ItemId("1-1-1", 1), null, true);
		assertEquals("Q(UNDER:1-1-1:1)", query.toString());

		query = InQuery.create(mbox, new ItemId("1-1-1", 1), null, false);
		assertEquals("Q(IN:1-1-1:1)", query.toString());
	}

}
