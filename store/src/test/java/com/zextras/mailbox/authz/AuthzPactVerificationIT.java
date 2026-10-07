/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.authz;

import au.com.dius.pact.provider.junit5.HttpTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactFolder;
import com.zextras.mailbox.util.MailboxServerExtension;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.ZimbraAuthToken;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;

@Tag("e2e")
@Provider("carbonio-authz")
@PactFolder("pacts")
class AuthzPactVerificationIT {

	@RegisterExtension
	static final MailboxServerExtension server = new MailboxServerExtension();

	@BeforeEach
	void target(PactVerificationContext context) {
		context.setTarget(new HttpTestTarget("localhost", server.getAuthzApiPort()));
	}

	@TestTemplate
	@ExtendWith(PactVerificationInvocationContextProvider.class)
	void verify(PactVerificationContext context) {
		context.verifyInteraction();
	}

	@State("a global admin and an account")
	Map<String, Object> globalAdminAndAccount() throws Exception {
		final Account admin = server.getAccountFactory().asGlobalAdmin().create();
		return Map.of(
				"adminToken", new ZimbraAuthToken(admin, true, null).getEncoded(),
				"accountId", server.getAccountFactory().create().getId());
	}

	@State("a user and another account")
	Map<String, Object> userAndAnotherAccount() throws Exception {
		final Account user = server.getAccountFactory().create();
		return Map.of(
				"userToken", new ZimbraAuthToken(user).getEncoded(),
				"accountId", server.getAccountFactory().create().getId());
	}

	@State("an account")
	Map<String, Object> anAccount() throws Exception {
		return Map.of("accountId", server.getAccountFactory().create().getId());
	}
}
