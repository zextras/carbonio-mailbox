/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.api.resource;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zextras.mailbox.util.MailboxServerExtension;
import com.zextras.mailbox.util.TestHttpClient.Response;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.ZimbraAuthToken;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

@Tag("e2e")
class AccountResourceIT {
	@RegisterExtension
	static final MailboxServerExtension server = new MailboxServerExtension();

	private static final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void returnsAccountInfo() throws Exception {
		final Account account = server.getAccountFactory()
				.create();

		final Response response = server.getHttpClient().get(
				server.getInternalApiEndpoint() + "/accounts/" + account.getId());

		assertEquals(200, response.statusCode());
		assertInfo(response, account);
	}

	@Test
	void returnsCarbonioFeatures() throws Exception {
		final Account account = server.getAccountFactory()
				.withAttribute(ZAttrProvisioning.A_carbonioFeatureFilesEnabled, "FALSE")
				.withAttribute(ZAttrProvisioning.A_carbonioFeatureMailsAppEnabled, "TRUE")
				.create();

		final Response response = server.getHttpClient().get(
				server.getInternalApiEndpoint() + "/accounts/" + account.getId());


		assertThatJson(response.body())
				.node("features").isObject()
				.containsEntry(ZAttrProvisioning.A_carbonioFeatureFilesEnabled, false)
				.containsEntry(ZAttrProvisioning.A_carbonioFeatureMailsAppEnabled, true)
				.containsKey(ZAttrProvisioning.A_carbonioFeatureTasksEnabled)
		;
	}

	@Test
	void returnsCarbonioCapabilities() throws Exception {
		final Account account = server.getAccountFactory()
				.withAttribute("carbonioFilesMaxFileUploadSize", "209715200")
				.create();

		final Response response = server.getHttpClient().get(
				server.getInternalApiEndpoint() + "/accounts/" + account.getId());

		assertThatJson(response.body())
				.node("capabilities").isObject()
				.containsEntry("carbonioFilesMaxFileUploadSize", "209715200");
	}

	@Test
	void externalUserAccountInfo() throws Exception {
		final Account account = server.getAccountFactory()
				.create();
		account.setMailTransport("smtp://external.example.com");

		final Response response = server.getHttpClient().get(
				server.getInternalApiEndpoint() + "/accounts/" + account.getId());

		assertEquals(200, response.statusCode());
		assertThatJson(response.body()).isObject()
				.containsEntry("isExternal", true);
	}

	@Test
	void localeAccountInfo() throws Exception {
		final String frFr = "fr_FR";
		final Account account = server.getAccountFactory()
				.withAttribute(ZAttrProvisioning.A_zimbraLocale, frFr)
				.create();

		final Response response = server.getHttpClient().get(
				server.getInternalApiEndpoint() + "/accounts/" + account.getId());

		assertThatJson(response.body()).isObject()
				.containsEntry("locale", frFr);
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
				.containsEntry("isExternal", account.isAccountExternal())
				.containsEntry("locale", account.getLocaleAsString())
		;

	}

	@Test
	void accountInfoIsGlobalAdmin() throws Exception {
		final Account account = server.getAccountFactory()
				.asGlobalAdmin()
				.create();

		final Response response = server.getHttpClient().get(
				server.getInternalApiEndpoint() + "/accounts/" + account.getId());

		assertThatJson(response.body()).isObject().containsEntry("isGlobalAdmin", true);
	}

	@Test
	void accountInfoDelegatedAdmin() throws Exception {
		final Account account = server.getAccountFactory()
				.withAttribute(ZAttrProvisioning.A_zimbraIsDelegatedAdminAccount, "TRUE")
				.create();

		final Response response = server.getHttpClient().get(
				server.getInternalApiEndpoint() + "/accounts/" + account.getId());

		assertThatJson(response.body()).isObject().containsEntry("isGlobalAdmin", false);
	}

	@Test
	void accountInfoDomainAdmin() throws Exception {
		final Account account = server.getAccountFactory()
				.withAttribute(ZAttrProvisioning.A_zimbraIsDomainAdminAccount, "TRUE")
				.create();

		final Response response = server.getHttpClient().get(
				server.getInternalApiEndpoint() + "/accounts/" + account.getId());

		assertThatJson(response.body()).isObject().containsEntry("isGlobalAdmin", false);
	}

	@Test
	void notFound() throws Exception {
		final Response response = server.getHttpClient().get(
						server.getInternalApiEndpoint() + "/accounts/not-existent-id");

		assertEquals(404, response.statusCode());
	}

	@Test
	void myselfInfo() throws Exception {
		final Account account = server.getAccountFactory().create();
		final String token = new ZimbraAuthToken(account).getEncoded();

		final Response response = server.getHttpClient()
				.get(server.getInternalApiEndpoint() + "/accounts/myself",
						Map.of("Cookie", "ZM_AUTH_TOKEN=" + token));

		assertEquals(200, response.statusCode());
		assertInfo(response, account);
	}

	@Test
	void myselfInfoAdmin() throws Exception {
		final Account account = server.getAccountFactory().asGlobalAdmin().create();
		final String token = new ZimbraAuthToken(account).getEncoded();

		final Response response = server.getHttpClient()
				.get(server.getInternalApiEndpoint() + "/accounts/myself",
						Map.of("Cookie", "ZM_ADMIN_AUTH_TOKEN=" + token));

		assertEquals(200, response.statusCode());
		assertInfo(response, account);
	}

	@Test
	void myselfInfoWithoutToken() throws Exception {
		final Response response = server.getHttpClient()
				.get(server.getInternalApiEndpoint() + "/accounts/myself");

		assertEquals(401, response.statusCode());
	}

	@Test
	void myselfInfoWithInvalidToken() throws Exception {
		final Response response = server.getHttpClient()
				.get(server.getInternalApiEndpoint() + "/accounts/myself",
						Map.of("Cookie", "ZM_AUTH_TOKEN=invalid-token"));

		assertEquals(401, response.statusCode());
	}

	// --- GET /accounts?email= tests ---

	@Test
	void getAccountByEmailReturnsAccountInfo() throws Exception {
		final Account account = server.getAccountFactory().create();

		final Response response = server.getHttpClient().get(
				server.getInternalApiEndpoint() + "/accounts?email=" + account.getName());

		assertEquals(200, response.statusCode());
		assertInfo(response, account);
	}

	@Test
	void getAccountByEmailNotFound() throws Exception {
		final Response response = server.getHttpClient().get(
				server.getInternalApiEndpoint() + "/accounts?email=nonexistent@test.com");

		assertEquals(404, response.statusCode());
	}

	@Test
	void getAccountByEmailMissingParam() throws Exception {
		final Response response = server.getHttpClient().get(
				server.getInternalApiEndpoint() + "/accounts");

		assertEquals(400, response.statusCode());
	}

	// --- POST /accounts/batch tests ---

	@Test
	void batchGetAccountsReturnsAccounts() throws Exception {
		final Account account1 = server.getAccountFactory().create();
		final Account account2 = server.getAccountFactory().create();

		final String body = objectMapper.writeValueAsString(
				Map.of("ids", List.of(account1.getId(), account2.getId())));

		final Response response = server.getHttpClient().post(
				server.getInternalApiEndpoint() + "/accounts/batch", body);

		assertEquals(200, response.statusCode());
		assertThatJson(response.body()).isArray().hasSize(2);
	}

	@Test
	void batchGetAccountsSkipsUnknownIds() throws Exception {
		final Account account = server.getAccountFactory().create();

		final String body = objectMapper.writeValueAsString(
				Map.of("ids", List.of(account.getId(), "unknown-id-that-does-not-exist")));

		final Response response = server.getHttpClient().post(
				server.getInternalApiEndpoint() + "/accounts/batch", body);

		assertEquals(200, response.statusCode());
		assertThatJson(response.body()).isArray().hasSize(1);
	}

	@Test
	void batchGetAccountsReturnsBadRequestWhenExceeding100Ids() throws Exception {
		final List<String> ids = IntStream.range(0, 101)
				.mapToObj(i -> "fake-id-" + i)
				.collect(Collectors.toList());

		final String body = objectMapper.writeValueAsString(Map.of("ids", ids));

		final Response response = server.getHttpClient().post(
				server.getInternalApiEndpoint() + "/accounts/batch", body);

		assertEquals(400, response.statusCode());
	}

	@Test
	void batchGetAccountsReturnsEmptyListForAllUnknownIds() throws Exception {
		final String body = objectMapper.writeValueAsString(
				Map.of("ids", List.of("unknown-id-1", "unknown-id-2")));

		final Response response = server.getHttpClient().post(
				server.getInternalApiEndpoint() + "/accounts/batch", body);

		assertEquals(200, response.statusCode());
		assertThatJson(response.body()).isArray().isEmpty();
	}

	@Test
	void batchByEmailsReturnsMatchingAccounts() throws Exception {
		final Account account = server.getAccountFactory().create();

		final String body = objectMapper.writeValueAsString(
				Map.of("emails", List.of(account.getName())));

		final Response response = server.getHttpClient().post(
				server.getInternalApiEndpoint() + "/accounts/batch", body);

		assertEquals(200, response.statusCode());
		assertThatJson(response.body()).isArray().hasSize(1);
	}

	@Test
	void batchWithBothIdsAndEmailsReturns400() throws Exception {
		final String body = objectMapper.writeValueAsString(
				Map.of("ids", List.of("some-id"), "emails", List.of("a@b.com")));

		final Response response = server.getHttpClient().post(
				server.getInternalApiEndpoint() + "/accounts/batch", body);

		assertEquals(400, response.statusCode());
	}

	@Test
	void batchWithNeitherIdsNorEmailsReturns400() throws Exception {
		final String body = "{}";

		final Response response = server.getHttpClient().post(
				server.getInternalApiEndpoint() + "/accounts/batch", body);

		assertEquals(400, response.statusCode());
	}
}
