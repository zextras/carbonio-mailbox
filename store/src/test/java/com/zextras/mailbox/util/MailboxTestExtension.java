/*
 * SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.util;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class MailboxTestExtension implements BeforeAllCallback, AfterAllCallback {

	private final MailboxTestData mailboxTestData;
	private final MailboxSetupHelper setup;

	public String getServerName() {
		return mailboxTestData.serverName();
	}

	public String getDefaultDomain() {
		return mailboxTestData.defaultDomain();
	}

	public MailboxTestExtension(MailboxTestData mailboxTestData, MailboxSetupHelper setup) {
		this.mailboxTestData = mailboxTestData;
		this.setup = setup;
	}

	@Override
	public void afterAll(ExtensionContext extensionContext) throws Exception {
		setup.tearDown();
	}

	@Override
	public void beforeAll(ExtensionContext extensionContext) throws Exception {
		setup.setUp(mailboxTestData);
	}

	public void initData() throws Exception {
		setup.initData(mailboxTestData);
	}

	public void clearData() throws Exception {
		setup.clearData();
	}
}
