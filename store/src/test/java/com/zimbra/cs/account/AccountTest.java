/*
 * SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zimbra.cs.account;

import com.zextras.mailbox.MailboxTestSuite;
import com.zextras.mailbox.util.AccountCreator.Factory;
import com.zimbra.common.service.ServiceException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class AccountTest extends MailboxTestSuite {
	private static Factory accountCreatorFactory;

	@BeforeAll
	static void setup() {
		// TODO: this creational pattern is quite ugly, consider embedding the AccountCreator instance in the extension
		accountCreatorFactory = new Factory(Provisioning.getInstance(),
				MailboxTestSuite.mailboxTestExtension.getDefaultDomain());
	}

	@Test
	void shouldReturnGMT_IfPrefTimezoneEmpty() throws ServiceException {
		final Account account = accountCreatorFactory.get().
				withAttribute(Provisioning.A_zimbraPrefTimeZoneId, "").
				create();
		final String preferredTimezone = account.getPreferredTimezone();
		Assertions.assertEquals("GMT", preferredTimezone);
	}

}