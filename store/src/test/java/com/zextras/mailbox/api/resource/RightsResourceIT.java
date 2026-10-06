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
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.ZimbraAuthToken;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

@Tag("e2e")
class RightsResourceIT {

	@RegisterExtension
	static final MailboxServerExtension server = new MailboxServerExtension();

	@Test
	void allowsGlobalAdminByToken() throws Exception {
		final Account admin = server.getAccountFactory().asGlobalAdmin().create();
		final Account target = server.getAccountFactory().create();

		final Response response = check(tokenSubject(adminToken(admin)), "getAccountInfo", accountTarget(target));

		assertEquals(200, response.statusCode());
		assertThatJson(response.body()).isObject().containsEntry("allowed", true);
	}

	@Test
	void deniesPlainUserByAccountId() throws Exception {
		final Account user = server.getAccountFactory().create();
		final Account target = server.getAccountFactory().create();

		final Response response = check(accountIdSubject(user), "getAccountInfo", accountTarget(target));

		assertEquals(200, response.statusCode());
		assertThatJson(response.body()).isObject().containsEntry("allowed", false);
	}

	@Test
	void invalidTokenIsUnauthorized() throws Exception {
		final Account target = server.getAccountFactory().create();

		final Response response = check(tokenSubject("invalid-token"), "getAccountInfo", accountTarget(target));

		assertEquals(401, response.statusCode());
	}

	@Test
	void bothSubjectsAreRejected() throws Exception {
		final Account admin = server.getAccountFactory().asGlobalAdmin().create();
		final Account target = server.getAccountFactory().create();
		final String subject = "{\"token\":\"" + adminToken(admin) + "\",\"accountId\":\"" + admin.getId() + "\"}";

		final Response response = check(subject, "getAccountInfo", accountTarget(target));

		assertEquals(400, response.statusCode());
	}

	@Test
	void rightOnWrongTargetTypeIsRejected() throws Exception {
		final Account admin = server.getAccountFactory().asGlobalAdmin().create();

		final Response response = check(accountIdSubject(admin), "getAccountInfo", "{\"type\":\"cos\",\"id\":\"any\"}");

		assertEquals(400, response.statusCode());
	}

	@Test
	void unknownRightIsRejected() throws Exception {
		final Account admin = server.getAccountFactory().asGlobalAdmin().create();
		final Account target = server.getAccountFactory().create();

		final Response response = check(accountIdSubject(admin), "notARight", accountTarget(target));

		assertEquals(400, response.statusCode());
	}

	@Test
	void listsRightsOnTargetType() throws Exception {
		final Response response = server.getHttpClient().get(server.getInternalApiEndpoint() + "/rights?targetType=cos");

		assertEquals(200, response.statusCode());
		assertThatJson(response.body()).isArray().anySatisfy(right -> assertThatJson(right).isObject()
				.containsEntry("name", "configureQuota")
				.containsEntry("type", "setAttrs")
				.containsEntry("userRight", false));
	}

	@Test
	void listingWithoutTargetTypeIsRejected() throws Exception {
		final Response response = server.getHttpClient().get(server.getInternalApiEndpoint() + "/rights");

		assertEquals(400, response.statusCode());
	}

	@Test
	void listingUnknownTargetTypeIsRejected() throws Exception {
		final Response response = server.getHttpClient().get(server.getInternalApiEndpoint() + "/rights?targetType=planet");

		assertEquals(400, response.statusCode());
	}

	private static Response check(String subject, String right, String target) throws Exception {
		final String body = "{\"subject\":" + subject + ",\"right\":\"" + right + "\",\"target\":" + target + "}";
		return server.getHttpClient().post(server.getInternalApiEndpoint() + "/rights/check", body);
	}

	private static String tokenSubject(String token) {
		return "{\"token\":\"" + token + "\"}";
	}

	private static String accountIdSubject(Account account) {
		return "{\"accountId\":\"" + account.getId() + "\"}";
	}

	private static String accountTarget(Account account) {
		return "{\"type\":\"account\",\"id\":\"" + account.getId() + "\"}";
	}

	private static String adminToken(Account account) throws Exception {
		return new ZimbraAuthToken(account, true, null).getEncoded();
	}
}
