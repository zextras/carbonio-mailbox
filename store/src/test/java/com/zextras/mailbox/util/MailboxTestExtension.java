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

	public String getServerName() {
		return mailboxTestData.serverName();
	}

	public String getDefaultDomain() {
		return mailboxTestData.defaultDomain();
	}

	public MailboxTestExtension(MailboxTestData mailboxTestData) {
		this.mailboxTestData = mailboxTestData;
	}

	@Override
	public void afterAll(ExtensionContext extensionContext) throws Exception {
		// TODO: use a MailboxTestBuilder and pass domain and server
		MailboxTestUtil.tearDown();
	}

	@Override
	public void beforeAll(ExtensionContext extensionContext) throws Exception {
		MailboxTestUtil.setUp(mailboxTestData);
	}

	public void initData() throws Exception {
		MailboxTestUtil.initData(mailboxTestData);
	}

	public void clearData() throws Exception {
		MailboxTestUtil.clearData();
	}
}
