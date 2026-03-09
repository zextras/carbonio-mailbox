/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.api.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zextras.mailbox.util.MailboxServerExtension;
import com.zextras.mailbox.util.TestHttpClient.Response;
import com.zimbra.cs.account.Account;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

@Tag("e2e")
class MailboxResourceIT {

	@RegisterExtension
	static final MailboxServerExtension server = new MailboxServerExtension();

	@Test
	void getMailUsageShouldReturnUsageForExistingAccount() throws Exception {
		final Account account = server.getAccountFactory().create();

		final Response response = server.getHttpClient().get(
				server.getInternalApiEndpoint() + "/accounts/mail/usage/" + account.getId());

		assertEquals(200, response.statusCode());
		assertTrue(response.body().contains("{\"used\":0}"),
				"Response should contain 'used' field. Was: \n" + response.body());
	}

	@Test
	void getMailUsageNotFoundForNonExistentAccount() throws Exception {
		final Response response = server.getHttpClient().get(
				server.getInternalApiEndpoint() + "/accounts/mail/usage/non-existent-id");

		assertEquals(404, response.statusCode());
	}
}
