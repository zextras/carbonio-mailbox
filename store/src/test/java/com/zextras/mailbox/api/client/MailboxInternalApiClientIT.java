/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.api.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zextras.mailbox.internalapi.client.Account;
import com.zextras.mailbox.internalapi.client.BadRequestException;
import com.zextras.mailbox.internalapi.client.Cos;
import com.zextras.mailbox.internalapi.client.MailUsage;
import com.zextras.mailbox.internalapi.client.MailboxInternalApiClient;
import com.zextras.mailbox.internalapi.client.NotFoundException;
import com.zextras.mailbox.internalapi.client.RightInfo;
import com.zextras.mailbox.internalapi.client.RightSubject;
import com.zextras.mailbox.internalapi.client.RightTarget;
import com.zextras.mailbox.internalapi.client.SharedAccount;
import com.zextras.mailbox.internalapi.client.UnauthorizedException;
import com.zextras.mailbox.util.MailboxServerExtension;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.ZimbraAuthToken;
import com.zimbra.cs.mailbox.ACL;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.mailbox.acl.AclPushTask;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

@Tag("e2e")
class MailboxInternalApiClientIT {

	@RegisterExtension
	static final MailboxServerExtension server = new MailboxServerExtension();

	private static MailboxInternalApiClient client;

	@BeforeAll
	static void setUp() {
		client = MailboxInternalApiClient.builder(server.getInternalApiEndpoint()).build();
	}

	@Test
	void getsAccountById() throws Exception {
		final com.zimbra.cs.account.Account account = server.getAccountFactory().create();

		final Account result = client.getAccount(account.getId());

		assertAccount(account, result);
		assertEquals(account.getAccountStatus().toString(), result.status());
	}

	@Test
	void getsAccountByEmail() throws Exception {
		final com.zimbra.cs.account.Account account = server.getAccountFactory().create();

		assertAccount(account, client.getAccountByEmail(account.getName()));
	}

	@Test
	void getsMyAccount() throws Exception {
		final com.zimbra.cs.account.Account account = server.getAccountFactory().asGlobalAdmin().create();

		final Account result = client.getMyAccount(new ZimbraAuthToken(account).getEncoded());

		assertAccount(account, result);
		assertTrue(result.isGlobalAdmin());
	}

	@Test
	void getsAccountsInBatch() throws Exception {
		final com.zimbra.cs.account.Account first = server.getAccountFactory().create();
		final com.zimbra.cs.account.Account second = server.getAccountFactory().create();

		assertEquals(2, client.getAccountsByIds(List.of(first.getId(), second.getId())).size());
		assertEquals(2, client.getAccountsByEmails(List.of(first.getName(), second.getName())).size());
	}

	@Test
	void getsSharedAccounts() throws Exception {
		final com.zimbra.cs.account.Account owner = server.getAccountFactory().create();
		final com.zimbra.cs.account.Account grantee = server.getAccountFactory().create();
		shareRootFolder(owner, grantee);

		final List<SharedAccount> result = client.getSharedAccounts(grantee.getId());

		assertEquals(List.of(new SharedAccount(owner.getId(), owner.getName(), owner.getDomainId(),
				owner.getCOSId())), result);
	}

	@Test
	void getsMailUsage() throws Exception {
		final com.zimbra.cs.account.Account account = server.getAccountFactory().create();

		final MailUsage result = client.getMailUsage(account.getId());

		assertEquals(account.getId(), result.id());
		assertEquals(account.getName(), result.name());
	}

	@Test
	void getsCos() throws Exception {
		final com.zimbra.cs.account.Cos cos =
				Provisioning.getInstance().createCos(UUID.randomUUID().toString(), new HashMap<>());

		assertEquals(new Cos(cos.getId(), cos.getName()), client.getCos(cos.getId()));
	}

	@Test
	void listsRights() throws Exception {
		final List<RightInfo> rights = client.listRights("cos");

		assertTrue(rights.stream().anyMatch(right -> right.name().equals("configureQuota")));
	}

	@Test
	void checksRight() throws Exception {
		final com.zimbra.cs.account.Account admin = server.getAccountFactory().asGlobalAdmin().create();
		final com.zimbra.cs.account.Account user = server.getAccountFactory().create();
		final RightTarget target = RightTarget.account(user.getId());

		assertTrue(client.checkRight(RightSubject.account(admin.getId()), "configureQuota", target));
		assertFalse(client.checkRight(RightSubject.account(user.getId()), "configureQuota", target));
	}

	@Test
	void missingAccountIsNotFound() {
		assertThrows(NotFoundException.class, () -> client.getAccount(UUID.randomUUID().toString()));
	}

	@Test
	void invalidTokenIsUnauthorized() {
		assertThrows(UnauthorizedException.class, () -> client.getMyAccount("invalid-token"));
	}

	@Test
	void unknownTargetTypeIsBadRequest() {
		assertThrows(BadRequestException.class, () -> client.listRights("planet"));
	}

	private static void assertAccount(com.zimbra.cs.account.Account expected, Account actual)
			throws ServiceException {
		assertEquals(expected.getId(), actual.id());
		assertEquals(expected.getName(), actual.name());
		assertEquals(expected.getCOSId(), actual.cosId());
		assertEquals(expected.getDomainId(), actual.domainId());
	}

	private static void shareRootFolder(com.zimbra.cs.account.Account owner,
			com.zimbra.cs.account.Account grantee) throws ServiceException {
		final Mailbox mailbox = MailboxManager.getInstance().getMailboxByAccount(owner);
		mailbox.grantAccess(new OperationContext(owner), Mailbox.ID_FOLDER_USER_ROOT,
				grantee.getId(), ACL.GRANTEE_USER, ACL.RIGHT_READ, null);
		AclPushTask.doWork();
	}
}
