/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.authz.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zextras.carbonio.authz.AuthzClient;
import com.zextras.carbonio.authz.AuthzRight;
import com.zextras.carbonio.authz.AuthzTargetType;
import com.zextras.carbonio.authz.BadRequestException;
import com.zextras.carbonio.authz.RightInfo;
import com.zextras.carbonio.authz.RightSubject;
import com.zextras.carbonio.authz.RightTarget;
import com.zextras.carbonio.authz.UnauthorizedException;
import com.zextras.mailbox.util.MailboxServerExtension;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.ZimbraAuthToken;
import com.zimbra.cs.account.accesscontrol.Right;
import com.zimbra.cs.account.accesscontrol.RightManager;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

@Tag("e2e")
class AuthzClientIT {

	@RegisterExtension
	static final MailboxServerExtension server = new MailboxServerExtension();

	private static AuthzClient client;

	@BeforeAll
	static void setUp() {
		client = AuthzClient.builder(server.getAuthzApiEndpoint()).build();
	}

	@Test
	void listsRights() throws Exception {
		final List<RightInfo> rights = client.listRights("cos");

		assertTrue(rights.stream().anyMatch(right -> right.name().equals("configureQuota")));
	}

	@Test
	void checksRightByAccount() throws Exception {
		final Account admin = server.getAccountFactory().asGlobalAdmin().create();
		final Account user = server.getAccountFactory().create();
		final RightTarget target = RightTarget.account(user.getId());

		assertTrue(client.checkRight(RightSubject.account(admin.getId()), "configureQuota", target));
		assertFalse(client.checkRight(RightSubject.account(user.getId()), "configureQuota", target));
	}

	@Test
	void checksRightByToken() throws Exception {
		final Account admin = server.getAccountFactory().asGlobalAdmin().create();
		final Account user = server.getAccountFactory().create();
		final String adminToken = new ZimbraAuthToken(admin, true, null).getEncoded();

		assertTrue(client.checkRight(RightSubject.token(adminToken), "getAccountInfo",
				RightTarget.account(user.getId())));
	}

	@Test
	void checksRightWithEnums() throws Exception {
		final Account admin = server.getAccountFactory().asGlobalAdmin().create();
		final Account user = server.getAccountFactory().create();

		assertTrue(client.checkRight(RightSubject.account(admin.getId()), AuthzRight.CONFIGURE_QUOTA,
				RightTarget.of(AuthzTargetType.ACCOUNT, user.getId())));
	}

	@Test
	void authzRightsMatchTheCatalogue() throws Exception {
		final RightManager rightManager = RightManager.getInstance();
		final Set<String> catalogue = Stream.concat(
						rightManager.getAllUserRights().values().stream(),
						rightManager.getAllAdminRights().values().stream())
				.filter(right -> !right.isComboRight())
				.map(Right::getName)
				.collect(Collectors.toSet());

		assertEquals(catalogue,
				Arrays.stream(AuthzRight.values()).map(AuthzRight::rightName).collect(Collectors.toSet()));
	}

	@Test
	void invalidTokenIsUnauthorized() throws Exception {
		final Account user = server.getAccountFactory().create();

		assertThrows(UnauthorizedException.class, () -> client.checkRight(
				RightSubject.token("invalid-token"), "getAccountInfo", RightTarget.account(user.getId())));
	}

	@Test
	void unknownTargetTypeIsBadRequest() {
		assertThrows(BadRequestException.class, () -> client.listRights("planet"));
	}
}
