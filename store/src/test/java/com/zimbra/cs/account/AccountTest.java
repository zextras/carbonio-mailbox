/*
 * SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zimbra.cs.account;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.common.service.ServiceException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AccountTest extends MailboxTestSuite {


	@Test
	void shouldReturnGMT_IfPrefTimezoneEmpty() throws ServiceException {
		final Account account = createAccount().
				withAttribute(Provisioning.A_zimbraPrefTimeZoneId, "").
				create();
		final String preferredTimezone = account.getPreferredTimezone();
		Assertions.assertEquals("GMT", preferredTimezone);
	}

	@Test
	void shouldReturnFirstTimezoneGMT_IfPrefTimezoneMultipleValues() throws ServiceException {
		final Account account = createAccount().
				withAttribute(Provisioning.A_zimbraPrefTimeZoneId, new String[]{"Europe/London", "Europe/Berlin"}).
				create();
		final String preferredTimezone = account.getPreferredTimezone();
		Assertions.assertEquals("Europe/London", preferredTimezone);
	}

}