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

	private static volatile MailboxSetupHelper sharedSetup;
	private static volatile boolean initialized = false;

	private final MailboxTestData mailboxTestData;
	private final MailboxSetupHelper setup;

	public MailboxTestExtension(MailboxTestData mailboxTestData, MailboxSetupHelper setup) {
		this.mailboxTestData = mailboxTestData;
		this.setup = setup;
	}

	@Override
	public void afterAll(ExtensionContext extensionContext) {
		// Infrastructure is shared — tearDown runs once via JVM shutdown hook.
	}

	/**
	 * Infrastructure (LDAP, HSQLDB, StoreManager, etc.) is expensive to start and stop.
	 * Instead of a full setUp/tearDown cycle for every test class, we:
	 *
	 * <ul>
	 *   <li>First class: full {@code setUp()} — starts LDAP, creates DB, initializes all services.
	 *       A JVM shutdown hook is registered to run {@code tearDown()} once when the fork exits.</li>
	 *   <li>Subsequent classes: lightweight reset — clears LDAP entries and DB rows, clears
	 *       index cache, then re-creates the base server/domain/config. The MailboxManager is
	 *       reset to discard cached mailbox instances from the previous class.</li>
	 * </ul>
	 */
	@Override
	public void beforeAll(ExtensionContext extensionContext) throws Exception {
		if (!initialized) {
			synchronized (MailboxTestExtension.class) {
				if (!initialized) {
					setup.setUp(mailboxTestData);
					sharedSetup = setup;
					initialized = true;
					Runtime.getRuntime().addShutdownHook(new Thread(() -> {
						try {
							sharedSetup.tearDown();
						} catch (Exception e) {
							// best-effort cleanup on JVM exit
						}
					}));
					return;
				}
			}
		}
		// Tear down and reinitialize everything except the LDAP server
		sharedSetup.resetAndSetUp(mailboxTestData);
	}

	public void initData() throws Exception {
		getActiveSetup().initData(mailboxTestData);
	}

	public void clearData() throws Exception {
		getActiveSetup().clearData();
	}

	private MailboxSetupHelper getActiveSetup() {
		return sharedSetup != null ? sharedSetup : setup;
	}
}
