/*
 * SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.util;

import com.zimbra.cs.mailbox.MailboxManager;
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
	public void afterAll(ExtensionContext extensionContext) throws Exception {
		// Don't tear down — infrastructure is shared across test classes.
		// Data cleanup happens in beforeAll of the next class.
		// Full teardown runs via JVM shutdown hook.
	}

	/**
	 * Infrastructure (LDAP, HSQLDB, StoreManager, etc.) is expensive to start and stop.
	 * Instead of doing a full setUp/tearDown cycle for every test class, we:
	 *
	 * <ul>
	 *   <li>First class: full {@code setUp()} — starts LDAP, creates DB, initializes all services.
	 *       A JVM shutdown hook is registered to run {@code tearDown()} once when the fork exits.</li>
	 *   <li>Subsequent classes: lightweight reset — clears LDAP entries and DB rows via
	 *       {@code clearData()}, then re-creates the base server/domain/config via
	 *       {@code initData()}. The MailboxManager is reset to discard cached mailbox instances
	 *       from the previous class.</li>
	 * </ul>
	 *
	 * This avoids ~2s of LDAP/DB start-stop overhead per test class.
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
		setup.clearData();
		setup.initData(mailboxTestData);
		MailboxManager.setInstance(new MailboxManager());
	}

	public void initData() throws Exception {
		setup.initData(mailboxTestData);
	}

	public void clearData() throws Exception {
		setup.clearData();
	}
}
