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
import com.zimbra.common.service.ServiceException;
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
		assertInfo(response, account);
	}

	@Test
	void externalUserAccountInfo() throws Exception {
		final Account account = server.getAccountFactory()
				.create();
		account.setMailTransport("smtp://external.example.com");

		final Response response = server.getHttpClient().get(
				server.getInternalApiEndpoint() + "/accounts/" + account.getId() + "/info");

		assertEquals(200, response.statusCode());
		assertThatJson(response.body()).isObject()
				.containsEntry("isExternal", true);
		assertInfo(response, account);
	}

	private static void assertInfo(Response response, Account account) throws ServiceException {
		assertThatJson(response.body()).isObject()
				.containsEntry("id", account.getId())
				.containsEntry("name", account.getName())
				.containsEntry("displayName", account.getDisplayName())
				.containsEntry("cosId", account.getCOSId())
				.containsEntry("domainId", account.getDomainId())
				.containsEntry("status", account.getAccountStatus().toString())
				.containsEntry("isGlobalAdmin", account.isIsAdminAccount())
				.containsEntry("isExternal", account.isAccountExternal()
				);
	}

	@Test
	void accountInfoIsGlobalAdmin() throws Exception {
		final Account account = server.getAccountFactory()
				.asGlobalAdmin()
				.create();

		final Response response = server.getHttpClient().get(
				server.getInternalApiEndpoint() + "/accounts/" + account.getId() + "/info");

		assertThatJson(response.body()).isObject().containsEntry("isGlobalAdmin", true);
	}

	@Test
	void accountInfoDelegatedAdmin() throws Exception {
		final Account account = server.getAccountFactory()
				.withAttribute(ZAttrProvisioning.A_zimbraIsDelegatedAdminAccount, "TRUE")
				.create();

		final Response response = server.getHttpClient().get(
				server.getInternalApiEndpoint() + "/accounts/" + account.getId() + "/info");

		assertThatJson(response.body()).isObject().containsEntry("isGlobalAdmin", false);
	}

	@Test
	void accountInfoDomainAdmin() throws Exception {
		final Account account = server.getAccountFactory()
				.withAttribute(ZAttrProvisioning.A_zimbraIsDomainAdminAccount, "TRUE")
				.create();

		final Response response = server.getHttpClient().get(
				server.getInternalApiEndpoint() + "/accounts/" + account.getId() + "/info");

		assertThatJson(response.body()).isObject().containsEntry("isGlobalAdmin", false);
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
		assertInfo(response, account);
	}

	@Test
	void myselfInfoAdmin() throws Exception {
		final Account account = server.getAccountFactory().asGlobalAdmin().create();
		final String token = new ZimbraAuthToken(account).getEncoded();

		final Response response = server.getHttpClient()
				.get(server.getInternalApiEndpoint() + "/accounts/myself/info",
						Map.of("Cookie", "ZM_ADMIN_AUTH_TOKEN=" + token));

		assertEquals(200, response.statusCode());
		assertInfo(response, account);
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
