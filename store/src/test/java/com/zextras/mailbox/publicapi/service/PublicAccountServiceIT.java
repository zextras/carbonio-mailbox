/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.publicapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import com.zextras.mailbox.api.rest.service.AccountService;
import com.zextras.mailbox.authz.service.RightsService;
import com.zextras.mailbox.util.MailboxServerExtension;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.AccessManager;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AuthTokenException;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.ZimbraAuthToken;
import com.zimbra.cs.account.accesscontrol.ACLUtil;
import com.zimbra.cs.account.accesscontrol.GranteeType;
import com.zimbra.cs.account.accesscontrol.RightManager;
import com.zimbra.cs.account.accesscontrol.Rights.Admin;
import com.zimbra.cs.account.accesscontrol.ZimbraACE;
import io.vavr.control.Try;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

@Tag("e2e")
class PublicAccountServiceIT {

	@RegisterExtension
	static final MailboxServerExtension server = new MailboxServerExtension();

	private static PublicAccountService publicAccountService;

	@BeforeAll
	static void setUp() {
		final AccountService accountService = new AccountService(Provisioning::getInstance, null);
		final RightsService rightsService = new RightsService(Provisioning::getInstance,
				AccessManager::getInstance, PublicAccountServiceIT::rightManager, accountService);
		publicAccountService = new PublicAccountService(accountService, rightsService);
	}

	@Test
	void userReadsOwnAccount() throws Exception {
		final Account user = server.getAccountFactory().create();

		final Try<Account> result = publicAccountService.getAccount(userToken(user), user.getId());

		assertEquals(user.getId(), result.get().getId());
	}

	@Test
	void globalAdminReadsAnyAccount() throws Exception {
		final Account admin = server.getAccountFactory().asGlobalAdmin().create();
		final Account user = server.getAccountFactory().create();

		assertEquals(user.getId(), publicAccountService.getAccount(adminToken(admin), user.getId()).get().getId());
	}

	@Test
	void delegatedAdminReadsAccountInGrantedDomain() throws Exception {
		final Domain domain = createDomain();
		final Account delegatedAdmin = createDelegatedAdmin();
		grantGetAccountInfo(delegatedAdmin, domain);
		final Account user = server.getAccountFactory().withDomain(domain.getName()).create();

		assertEquals(user.getId(),
				publicAccountService.getAccount(adminToken(delegatedAdmin), user.getId()).get().getId());
	}

	@Test
	void delegatedAdminWithoutGrantIsDenied() throws Exception {
		final Account delegatedAdmin = createDelegatedAdmin();
		final Account user = server.getAccountFactory().withDomain(createDomain().getName()).create();

		assertPermissionDenied(publicAccountService.getAccount(adminToken(delegatedAdmin), user.getId()));
	}

	@Test
	void globalAdminWithUserTokenIsDenied() throws Exception {
		final Account admin = server.getAccountFactory().asGlobalAdmin().create();
		final Account user = server.getAccountFactory().create();

		assertPermissionDenied(publicAccountService.getAccount(userToken(admin), user.getId()));
	}

	@Test
	void userReadingAnotherAccountIsDenied() throws Exception {
		final Account user = server.getAccountFactory().create();
		final Account other = server.getAccountFactory().create();

		assertPermissionDenied(publicAccountService.getAccount(userToken(user), other.getId()));
	}

	@Test
	void missingAccountIsDenied() throws Exception {
		final Account admin = server.getAccountFactory().asGlobalAdmin().create();

		assertPermissionDenied(publicAccountService.getAccount(adminToken(admin), UUID.randomUUID().toString()));
	}

	@Test
	void invalidTokenFails() {
		final Try<Account> result = publicAccountService.getAccount("invalid-token", UUID.randomUUID().toString());

		assertInstanceOf(AuthTokenException.class, result.getCause());
	}

	private static void assertPermissionDenied(Try<Account> result) {
		final ServiceException exception = assertInstanceOf(ServiceException.class, result.getCause());
		assertEquals(ServiceException.PERM_DENIED, exception.getCode());
	}

	private static String userToken(Account account) throws Exception {
		return new ZimbraAuthToken(account).getEncoded();
	}

	private static String adminToken(Account account) throws Exception {
		return new ZimbraAuthToken(account, true, null).getEncoded();
	}

	private static Domain createDomain() throws ServiceException {
		return Provisioning.getInstance().createDomain(UUID.randomUUID() + ".com", new HashMap<>());
	}

	private static Account createDelegatedAdmin() throws ServiceException {
		return server.getAccountFactory()
				.withAttribute(ZAttrProvisioning.A_zimbraIsDelegatedAdminAccount, "TRUE")
				.create();
	}

	private static void grantGetAccountInfo(Account grantee, Domain domain) throws ServiceException {
		final ZimbraACE ace = new ZimbraACE(grantee.getId(), GranteeType.GT_USER, Admin.R_getAccountInfo, null, null);
		ACLUtil.grantRight(Provisioning.getInstance(), domain, Set.of(ace));
	}

	private static RightManager rightManager() {
		try {
			return RightManager.getInstance();
		} catch (ServiceException e) {
			throw new RuntimeException(e);
		}
	}
}
