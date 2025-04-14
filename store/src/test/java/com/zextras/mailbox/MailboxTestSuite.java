/*
 * SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox;

import com.zextras.mailbox.util.MailboxTestExtension;
import com.zextras.mailbox.util.MailboxTestUtil;
import org.junit.jupiter.api.extension.RegisterExtension;

public abstract class MailboxTestSuite {

	private static final String SERVER_NAME = "localhost";
	private static final String DEFAULT_DOMAIN = "test.com";

	@RegisterExtension
	protected static MailboxTestExtension mailboxTestExtension = new MailboxTestExtension(DEFAULT_DOMAIN,SERVER_NAME);

	protected void initDefaultData() throws Exception {
		MailboxTestUtil.initData();
	}
	protected void clearAllData() throws Exception {
		MailboxTestUtil.clearData();
	}

}
