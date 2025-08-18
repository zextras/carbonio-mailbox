/*
 * SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox;

import com.zextras.mailbox.util.AccountCreator;
import com.zextras.mailbox.util.AccountCreator.Factory;
import com.zextras.mailbox.util.MailboxTestData;
import com.zextras.mailbox.util.MailboxTestExtension;
import com.zextras.mailbox.util.MailboxSetupHelper;
import com.zimbra.cs.account.Provisioning;
import org.junit.jupiter.api.extension.RegisterExtension;

public abstract class MailboxTestSuite {

	private static final MailboxTestData mailboxTestData = new MailboxTestData("localhost",
			"test.com",
			"f4806430-b434-4e93-9357-a02d9dd796b8");

	@RegisterExtension
	protected static MailboxTestExtension mailboxTestExtension = new MailboxTestExtension(mailboxTestData,
			MailboxSetupHelper.create());

	protected AccountCreator.Factory getAccountCreator() {
		return new Factory(Provisioning.getInstance(),
				MailboxTestSuite.mailboxTestExtension.getDefaultDomain());
	}

}
