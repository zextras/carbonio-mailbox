/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.api.rest.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zextras.mailbox.util.MailboxServerExtension;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.AccessManager;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Cos;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Entry;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.ZimbraAuthToken;
import com.zimbra.cs.account.accesscontrol.ACLUtil;
import com.zimbra.cs.account.accesscontrol.Right;
import com.zimbra.cs.account.accesscontrol.GranteeType;
import com.zimbra.cs.account.accesscontrol.RightManager;
import com.zimbra.cs.account.accesscontrol.Rights.Admin;
import com.zimbra.cs.account.accesscontrol.Rights.User;
import com.zimbra.cs.account.accesscontrol.ZimbraACE;
import io.vavr.control.Try;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

@Tag("e2e")
class RightsServiceIT {

	@RegisterExtension
	static final MailboxServerExtension server = new MailboxServerExtension();

	private static RightsService rightsService;

	@BeforeAll
	static void setUp() {
		final AccountService accountService = new AccountService(Provisioning::getInstance, null);
		rightsService = new RightsService(Provisioning::getInstance, AccessManager::getInstance,
				RightsServiceIT::rightManager, accountService);
	}

	@Test
	void globalAdminTokenHasPresetRightOnAccount() throws Exception {
		final Account admin = server.getAccountFactory().asGlobalAdmin().create();
		final Account target = server.getAccountFactory().create();

		assertTrue(tokenHasRight(admin, "getAccountInfo", "account", target.getId()).get());
	}

	@Test
	void delegatedAdminHasRightGrantedOnTargetDomain() throws Exception {
		final Domain domain = createDomain();
		final Account delegatedAdmin = createDelegatedAdmin();
		grant(delegatedAdmin, domain, Admin.R_getAccountInfo);
		final Account target = createAccountIn(domain);

		assertTrue(tokenHasRight(delegatedAdmin, "getAccountInfo", "account", target.getId()).get());
	}

	@Test
	void delegatedAdminWithoutGrantIsDenied() throws Exception {
		final Account delegatedAdmin = createDelegatedAdmin();
		final Account target = createAccountIn(createDomain());

		assertFalse(tokenHasRight(delegatedAdmin, "getAccountInfo", "account", target.getId()).get());
	}

	@Test
	void delegatedAdminIsDeniedOnGlobalAdminAccount() throws Exception {
		final Domain domain = createDomain();
		final Account delegatedAdmin = createDelegatedAdmin();
		grant(delegatedAdmin, domain, Admin.R_getAccountInfo);
		final Account target = server.getAccountFactory().withDomain(domain.getName()).asGlobalAdmin().create();

		assertFalse(tokenHasRight(delegatedAdmin, "getAccountInfo", "account", target.getId()).get());
	}

	@Test
	void adminAccountWithUserTokenIsDenied() throws Exception {
		final Account admin = server.getAccountFactory().asGlobalAdmin().create();
		final Account target = server.getAccountFactory().create();

		final Try<Boolean> result = rightsService.tokenHasRight(
				new ZimbraAuthToken(admin).getEncoded(), "getAccountInfo", "account", target.getId());

		assertFalse(result.get());
	}

	@Test
	void delegatedAdminHasAttrRightGrantedOnDomain() throws Exception {
		final Domain domain = createDomain();
		final Account delegatedAdmin = createDelegatedAdmin();
		grant(delegatedAdmin, domain, Admin.R_getDomain);

		assertTrue(tokenHasRight(delegatedAdmin, "getDomain", "domain", domain.getId()).get());
	}

	@Test
	void delegatedAdminWithoutAttrRightOnDomainIsDenied() throws Exception {
		final Domain domain = createDomain();
		final Account delegatedAdmin = createDelegatedAdmin();

		assertFalse(tokenHasRight(delegatedAdmin, "getDomain", "domain", domain.getId()).get());
	}

	@Test
	void delegatedAdminHasPresetRightGrantedOnDomain() throws Exception {
		final Domain domain = createDomain();
		final Account delegatedAdmin = createDelegatedAdmin();
		grant(delegatedAdmin, domain, Admin.R_countAccount);

		assertTrue(tokenHasRight(delegatedAdmin, "countAccount", "domain", domain.getId()).get());
	}

	@Test
	void globalAdminHasAttrRightOnCos() throws Exception {
		final Account admin = server.getAccountFactory().asGlobalAdmin().create();
		final Cos cos = createCos();

		assertTrue(tokenHasRight(admin, "getCos", "cos", cos.getId()).get());
	}

	@Test
	void delegatedAdminWithoutGrantOnCosIsDenied() throws Exception {
		final Account delegatedAdmin = createDelegatedAdmin();
		final Cos cos = createCos();

		assertFalse(tokenHasRight(delegatedAdmin, "listCos", "cos", cos.getId()).get());
	}

	@Test
	void plainUserAccountIdIsDeniedOnAttrRight() throws Exception {
		final Account user = server.getAccountFactory().create();
		final Domain domain = createDomain();

		assertFalse(rightsService.accountHasRight(user.getId(), "getDomain", "domain", domain.getId()).get());
	}

	@Test
	void globalAdminAccountIdHasRight() throws Exception {
		final Account admin = server.getAccountFactory().asGlobalAdmin().create();
		final Account target = server.getAccountFactory().create();

		assertTrue(rightsService.accountHasRight(admin.getId(), "getAccountInfo", "account", target.getId()).get());
	}

	@Test
	void missingSubjectAccountIdIsDenied() throws Exception {
		final Account target = server.getAccountFactory().create();

		assertFalse(rightsService.accountHasRight(
				UUID.randomUUID().toString(), "getAccountInfo", "account", target.getId()).get());
	}

	@Test
	void missingTargetIsDenied() throws Exception {
		final Account admin = server.getAccountFactory().asGlobalAdmin().create();

		assertFalse(tokenHasRight(admin, "getAccountInfo", "account", UUID.randomUUID().toString()).get());
	}

	@Test
	void targetInSuspendedDomainIsDenied() throws Exception {
		final Account admin = server.getAccountFactory().asGlobalAdmin().create();
		final Domain domain = createDomain();
		final Account target = createAccountIn(domain);
		Provisioning.getInstance().modifyAttrs(domain,
				new HashMap<>(Map.of(ZAttrProvisioning.A_zimbraDomainStatus, "suspended")));

		assertFalse(tokenHasRight(admin, "getAccountInfo", "account", target.getId()).get());
	}

	@Test
	void unknownRightIsInvalid() throws Exception {
		assertInvalidRequest(globalAdminCheck("notARight", "account", UUID.randomUUID().toString()));
	}

	@Test
	void rightOnWrongTargetTypeIsInvalid() throws Exception {
		assertInvalidRequest(globalAdminCheck("getAccountInfo", "domain", createDomain().getId()));
	}

	@Test
	void globalAdminHasComboRight() throws Exception {
		assertTrue(globalAdminCheck("domainAdminRights", "domain", createDomain().getId()).get());
	}

	@Test
	void userTokenHasUserRightGrantedByTarget() throws Exception {
		final Account user = server.getAccountFactory().create();
		final Account target = server.getAccountFactory().create();
		grant(user, target, User.R_sendAs);

		final Try<Boolean> result = rightsService.tokenHasRight(
				new ZimbraAuthToken(user).getEncoded(), "sendAs", "account", target.getId());

		assertTrue(result.get());
	}

	@Test
	void userWithoutUserRightIsDenied() throws Exception {
		final Account user = server.getAccountFactory().create();
		final Account target = server.getAccountFactory().create();

		assertFalse(rightsService.accountHasRight(user.getId(), "sendAs", "account", target.getId()).get());
	}

	@Test
	void accountIdHasUserRightGrantedByTarget() throws Exception {
		final Account user = server.getAccountFactory().create();
		final Account target = server.getAccountFactory().create();
		grant(user, target, User.R_viewFreeBusy);

		assertTrue(rightsService.accountHasRight(user.getId(), "viewFreeBusy", "account", target.getId()).get());
	}

	@Test
	void userRightOnWrongTargetTypeIsInvalid() throws Exception {
		final Account user = server.getAccountFactory().create();

		assertInvalidRequest(rightsService.accountHasRight(user.getId(), "sendToDistList", "account", user.getId()));
	}

	@Test
	void unknownTargetTypeIsInvalid() throws Exception {
		assertInvalidRequest(globalAdminCheck("getAccountInfo", "planet", UUID.randomUUID().toString()));
	}

	@Test
	void missingTargetIdIsInvalid() throws Exception {
		assertInvalidRequest(globalAdminCheck("getAccountInfo", "account", null));
	}

	@Test
	void listsUserAndAdminRightsApplicableToTargetType() {
		final List<String> names = rightNamesOn("account");

		assertTrue(names.containsAll(List.of("configureQuota", "getAccountInfo", "getMailboxInfo", "sendAs")));
		assertFalse(names.contains("countAccount"));
	}

	@Test
	void listsAttrRightsForEveryTargetTypeTheyApplyTo() {
		assertTrue(rightNamesOn("cos").contains("configureQuota"));
		assertFalse(rightNamesOn("domain").contains("configureQuota"));
	}

	@Test
	void doesNotListComboRights() {
		assertFalse(rightNamesOn("domain").contains("domainAdminRights"));
	}

	@Test
	void listingUnknownTargetTypeIsInvalid() {
		final ServiceException exception = assertInstanceOf(ServiceException.class,
				rightsService.rightsOn("planet").getCause());
		assertEquals(ServiceException.INVALID_REQUEST, exception.getCode());
	}

	private static List<String> rightNamesOn(String targetType) {
		return rightsService.rightsOn(targetType).get().stream().map(Right::getName).toList();
	}

	private static Try<Boolean> globalAdminCheck(String right, String targetType, String targetId)
			throws Exception {
		return tokenHasRight(server.getAccountFactory().asGlobalAdmin().create(), right, targetType, targetId);
	}

	private static Try<Boolean> tokenHasRight(Account subject, String right, String targetType, String targetId)
			throws Exception {
		final String token = new ZimbraAuthToken(subject, true, null).getEncoded();
		return rightsService.tokenHasRight(token, right, targetType, targetId);
	}

	private static void assertInvalidRequest(Try<Boolean> result) {
		final ServiceException exception = assertInstanceOf(ServiceException.class, result.getCause());
		assertEquals(ServiceException.INVALID_REQUEST, exception.getCode());
	}

	private static RightManager rightManager() {
		try {
			return RightManager.getInstance();
		} catch (ServiceException e) {
			throw new RuntimeException(e);
		}
	}

	private static Domain createDomain() throws ServiceException {
		return Provisioning.getInstance().createDomain(UUID.randomUUID() + ".com", new HashMap<>());
	}

	private static Cos createCos() throws ServiceException {
		return Provisioning.getInstance().createCos(UUID.randomUUID().toString(), new HashMap<>());
	}

	private static Account createAccountIn(Domain domain) throws ServiceException {
		return server.getAccountFactory().withDomain(domain.getName()).create();
	}

	private static Account createDelegatedAdmin() throws ServiceException {
		return server.getAccountFactory()
				.withAttribute(ZAttrProvisioning.A_zimbraIsDelegatedAdminAccount, "TRUE")
				.create();
	}

	private static void grant(Account grantee, Entry target, Right right) throws ServiceException {
		final ZimbraACE ace = new ZimbraACE(grantee.getId(), GranteeType.GT_USER, right, null, null);
		ACLUtil.grantRight(Provisioning.getInstance(), target, Set.of(ace));
	}
}
