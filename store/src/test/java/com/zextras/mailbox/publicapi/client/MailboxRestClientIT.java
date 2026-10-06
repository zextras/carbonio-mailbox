/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.publicapi.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.zextras.mailbox.rest.client.Account;
import com.zextras.mailbox.rest.client.ForbiddenException;
import com.zextras.mailbox.rest.client.MailboxRestClient;
import com.zextras.mailbox.rest.client.UnauthorizedException;
import com.zextras.mailbox.util.MailboxServerExtension;
import com.zimbra.cs.account.ZimbraAuthToken;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

@Tag("e2e")
class MailboxRestClientIT {

	@RegisterExtension
	static final MailboxServerExtension server = new MailboxServerExtension();

	private static MailboxRestClient client;

	@BeforeAll
	static void setUp() {
		client = MailboxRestClient.builder(server.getPublicApiEndpoint()).build();
	}

	@Test
	void getsOwnAccount() throws Exception {
		final com.zimbra.cs.account.Account user = server.getAccountFactory().create();

		final Account result = client.getAccount(new ZimbraAuthToken(user).getEncoded(), user.getId());

		assertEquals(user.getId(), result.id());
		assertEquals(user.getName(), result.name());
		assertEquals(user.getCOSId(), result.cosId());
		assertEquals(user.getDomainId(), result.domainId());
		assertEquals(user.getAccountStatus().toString(), result.status());
	}

	@Test
	void getsMyAccount() throws Exception {
		final com.zimbra.cs.account.Account admin = server.getAccountFactory().asGlobalAdmin().create();

		final Account result = client.getMyAccount(new ZimbraAuthToken(admin).getEncoded());

		assertEquals(admin.getId(), result.id());
		assertEquals(true, result.isGlobalAdmin());
	}

	@Test
	void getMyAccountWithInvalidTokenIsUnauthorized() {
		assertThrows(UnauthorizedException.class, () -> client.getMyAccount("invalid-token"));
	}

	@Test
	void adminGetsAnotherAccount() throws Exception {
		final com.zimbra.cs.account.Account admin = server.getAccountFactory().asGlobalAdmin().create();
		final com.zimbra.cs.account.Account user = server.getAccountFactory().create();

		final Account result = client.getAccount(new ZimbraAuthToken(admin, true, null).getEncoded(), user.getId());

		assertEquals(user.getId(), result.id());
	}

	@Test
	void anotherUsersAccountIsForbidden() throws Exception {
		final com.zimbra.cs.account.Account user = server.getAccountFactory().create();
		final com.zimbra.cs.account.Account other = server.getAccountFactory().create();

		assertThrows(ForbiddenException.class,
				() -> client.getAccount(new ZimbraAuthToken(user).getEncoded(), other.getId()));
	}

	@Test
	void invalidTokenIsUnauthorized() throws Exception {
		final com.zimbra.cs.account.Account user = server.getAccountFactory().create();

		assertThrows(UnauthorizedException.class, () -> client.getAccount("invalid-token", user.getId()));
	}
}
