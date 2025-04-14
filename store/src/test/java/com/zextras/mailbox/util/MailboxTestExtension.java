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

	private final String defaultDomain;

	public String getServerName() {
		return serverName;
	}

	public String getDefaultDomain() {
		return defaultDomain;
	}

	private final String serverName;

	public MailboxTestExtension(String defaultDomain, String serverName) {
		this.defaultDomain = defaultDomain;
		this.serverName = serverName;
	}

	@Override
	public void afterAll(ExtensionContext extensionContext) throws Exception {
		// TODO: use a MailboxTestBuilder and pass domain and server
		MailboxTestUtil.tearDown();
	}

	@Override
	public void beforeAll(ExtensionContext extensionContext) throws Exception {
		MailboxTestUtil.setUp();
	}
}
