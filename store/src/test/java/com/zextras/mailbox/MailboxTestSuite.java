/*
 * SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox;

import com.zextras.mailbox.util.AccountCreator;
import com.zextras.mailbox.util.AccountCreator.Factory;
import com.zextras.mailbox.util.MailboxSetupHelper;
import com.zextras.mailbox.util.MailboxTestData;
import com.zextras.mailbox.util.MailboxTestExtension;
import com.zimbra.cs.account.Provisioning;
import org.junit.jupiter.api.extension.RegisterExtension;

public abstract class MailboxTestSuite {

	protected static final String DEFAULT_DOMAIN_NAME = "test.com";
	protected static final String DEFAULT_DOMAIN_ID = "f4806430-b434-4e93-9357-a02d9dd796b8";
	protected static final String SERVER_NAME = "localhost";
	private static final MailboxTestData mailboxTestData = new MailboxTestData(SERVER_NAME,
			DEFAULT_DOMAIN_NAME,
			DEFAULT_DOMAIN_ID);

	@RegisterExtension
	private final static MailboxTestExtension mailboxTestExtension = new MailboxTestExtension(mailboxTestData,
			MailboxSetupHelper.create());

	protected static AccountCreator.Factory createAccountFactory() {
		return new Factory(Provisioning.getInstance(),
				MailboxTestSuite.DEFAULT_DOMAIN_NAME);
	}

	protected void clearData() throws Exception {
		mailboxTestExtension.clearData();
	}
	protected void initData() throws Exception {
		mailboxTestExtension.initData();
	}
}
