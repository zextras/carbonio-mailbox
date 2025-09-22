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
import org.junit.jupiter.api.Test;

public final class ConvQueryTest extends MailboxTestSuite {

	@Test
	void remoteConvId() throws Exception {
		final Account account = createAccount().create();
		Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(account.getId());
		assertEquals("<DB[CONV:\"11111111-1111-1111-1111-111111111111:111\" ]>",
				ConvQuery.create(mbox, "11111111-1111-1111-1111-111111111111:111").compile(mbox, true)
						.toString());
	}

}
