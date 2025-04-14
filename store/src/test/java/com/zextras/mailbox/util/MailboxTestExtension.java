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

	@Override
	public void afterAll(ExtensionContext extensionContext) throws Exception {
		MailboxTestUtil.tearDown();
	}

	@Override
	public void beforeAll(ExtensionContext extensionContext) throws Exception {
		MailboxTestUtil.setUp();
	}
}
