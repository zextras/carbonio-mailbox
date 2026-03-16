/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.api.resource;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.zextras.mailbox.util.MailboxServerExtension;
import com.zextras.mailbox.util.TestHttpClient.Response;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.ZimbraAuthToken;
import java.util.Map;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

@Tag("e2e")
class AccountResourceIT {
	@RegisterExtension
	static final MailboxServerExtension server = new MailboxServerExtension();

	@Test
	void returnsAccountInfo() throws Exception {
		final Account account = server.getAccountFactory()
				.create();

		final Response response = server.getHttpClient().get(
				server.getInternalApiEndpoint() + "/accounts/" + account.getId() + "/info");

		assertEquals(200, response.statusCode());
		assertThatJson(response.body())
				.node("id").isEqualTo(account.getId())
				.node("name").isEqualTo(account.getName())
				.node("cosId").isEqualTo(account.getCOSId())
				.node("domainId").isEqualTo(account.getDomainId())
				.node("isGlobalAdmin").isEqualTo(false);
	}

	@Test
	void accountInfoIsGlobalAdmin() throws Exception {
		final Account account = server.getAccountFactory()
				.asGlobalAdmin()
				.create();

		final Response response = server.getHttpClient().get(
				server.getInternalApiEndpoint() + "/accounts/" + account.getId() + "/info");

		assertThatJson(response.body()).node("isGlobalAdmin").isEqualTo(true);
	}

	@Test
	void accountInfoDelegatedAdmin() throws Exception {
		final Account account = server.getAccountFactory()
				.withAttribute(ZAttrProvisioning.A_zimbraIsDelegatedAdminAccount, "TRUE")
				.create();

		final Response response = server.getHttpClient().get(
				server.getInternalApiEndpoint() + "/accounts/" + account.getId() + "/info");

		assertThatJson(response.body()).node("isGlobalAdmin").isEqualTo(false);
	}

	@Test
	void accountInfoDomainAdmin() throws Exception {
		final Account account = server.getAccountFactory()
				.withAttribute(ZAttrProvisioning.A_zimbraIsDomainAdminAccount, "TRUE")
				.create();

		final Response response = server.getHttpClient().get(
				server.getInternalApiEndpoint() + "/accounts/" + account.getId() + "/info");

		assertThatJson(response.body()).node("isGlobalAdmin").isEqualTo(false);
	}

	@Test
	void notFound() throws Exception {
		final Response response = server.getHttpClient().get(
						server.getInternalApiEndpoint() + "/accounts/not-existent-id/info");

		assertEquals(404, response.statusCode());
	}

	@Test
	void myselfInfo() throws Exception {
		final Account account = server.getAccountFactory().create();
		final String token = new ZimbraAuthToken(account).getEncoded();

		final Response response = server.getHttpClient()
				.get(server.getInternalApiEndpoint() + "/accounts/myself/info",
						Map.of("Cookie", "ZM_AUTH_TOKEN=" + token));

		assertEquals(200, response.statusCode());
		assertThatJson(response.body())
				.node("id").isEqualTo(account.getId())
				.node("name").isEqualTo(account.getName())
				.node("isGlobalAdmin").isEqualTo(false);
	}

	@Test
	void myselfInfoWithoutToken() throws Exception {
		final Response response = server.getHttpClient()
				.get(server.getInternalApiEndpoint() + "/accounts/myself/info");

		assertEquals(401, response.statusCode());
	}

	@Test
	void myselfInfoWithInvalidToken() throws Exception {
		final Response response = server.getHttpClient()
				.get(server.getInternalApiEndpoint() + "/accounts/myself/info",
						Map.of("Cookie", "ZM_AUTH_TOKEN=invalid-token"));

		assertEquals(401, response.statusCode());
	}

}
