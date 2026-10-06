/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.publicapi.resource;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.zextras.mailbox.util.MailboxServerExtension;
import com.zextras.mailbox.util.TestHttpClient.Response;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.ZimbraAuthToken;
import java.util.Map;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

@Tag("e2e")
class PublicAccountResourceIT {

	@RegisterExtension
	static final MailboxServerExtension server = new MailboxServerExtension();

	@Test
	void userReadsOwnAccount() throws Exception {
		final Account user = server.getAccountFactory().create();

		final Response response = getAccountInfo(user, "ZM_AUTH_TOKEN=" + new ZimbraAuthToken(user).getEncoded());

		assertEquals(200, response.statusCode());
		assertThatJson(response.body()).isObject()
				.containsEntry("id", user.getId())
				.containsEntry("name", user.getName());
	}

	@Test
	void adminReadsAccountWithAdminCookie() throws Exception {
		final Account admin = server.getAccountFactory().asGlobalAdmin().create();
		final Account user = server.getAccountFactory().create();

		final Response response = getAccountInfo(user,
				"ZM_ADMIN_AUTH_TOKEN=" + new ZimbraAuthToken(admin, true, null).getEncoded());

		assertEquals(200, response.statusCode());
	}

	@Test
	void readingAnotherAccountIsForbidden() throws Exception {
		final Account user = server.getAccountFactory().create();
		final Account other = server.getAccountFactory().create();

		final Response response = getAccountInfo(other, "ZM_AUTH_TOKEN=" + new ZimbraAuthToken(user).getEncoded());

		assertEquals(403, response.statusCode());
	}

	@Test
	void missingTokenIsUnauthorized() throws Exception {
		final Account user = server.getAccountFactory().create();

		final Response response = server.getHttpClient()
				.get(server.getPublicApiEndpoint() + "/accounts/" + user.getId() + "/info");

		assertEquals(401, response.statusCode());
	}

	@Test
	void invalidTokenIsUnauthorized() throws Exception {
		final Account user = server.getAccountFactory().create();

		assertEquals(401, getAccountInfo(user, "ZM_AUTH_TOKEN=invalid-token").statusCode());
	}

	private static Response getAccountInfo(Account account, String cookie) throws Exception {
		return server.getHttpClient().get(
				server.getPublicApiEndpoint() + "/accounts/" + account.getId() + "/info",
				Map.of("Cookie", cookie));
	}
}
