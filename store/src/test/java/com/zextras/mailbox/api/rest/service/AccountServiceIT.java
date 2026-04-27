/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.api.rest.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.soap.SoapProvisioning;
import com.zimbra.cs.mailbox.ACL;
import com.zimbra.cs.mailbox.Folder;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.mailbox.acl.AclPushSerializer;
import com.zextras.mailbox.api.rest.resource.dto.AccountSearchResponse;
import com.zextras.mailbox.util.MailboxServerExtension;
import io.vavr.control.Try;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

@Tag("e2e")
class AccountServiceIT {

	@RegisterExtension
	static final MailboxServerExtension server = new MailboxServerExtension();

	private static AccountService accountService;

	@BeforeAll
	static void setUp() throws Exception {
		final MailboxService mailboxService = new MailboxService(
				Provisioning::getInstance,
				() -> {
					try {
						return MailboxManager.getInstance();
					} catch (ServiceException e) {
						throw new RuntimeException(e);
					}
				},
				() -> {
					try {
						return SoapProvisioning.getAdminInstance();
					} catch (ServiceException e) {
						throw new RuntimeException(e);
					}
				});
		accountService = new AccountService(Provisioning::getInstance, mailboxService);
	}

	@Test
	void returnsEmptyWhenNoShares() throws Exception {
		final Account user = server.getAccountFactory().create();

		Try<List<Account>> result = accountService.getSharedAccounts(user.getId());

		assertTrue(result.isSuccess());
		assertTrue(result.get().isEmpty());
	}

	@Test
	void returnsSharedAccountWhenRootFolderIsShared() throws Exception {
		final Account owner = server.getAccountFactory().create();
		final Account user = server.getAccountFactory().create();
		shareRootFolder(owner, user);

		Try<List<Account>> result = accountService.getSharedAccounts(user.getId());

		assertTrue(result.isSuccess());
		assertEquals(1, result.get().size());
		assertEquals(owner.getId(), result.get().get(0).getId());
	}

	@Test
	void ignoresNonRootFolderShares() throws Exception {
		final Account owner = server.getAccountFactory().create();
		final Account user = server.getAccountFactory().create();
		shareSubFolder(owner, user, "a-subfolder");

		Try<List<Account>> result = accountService.getSharedAccounts(user.getId());

		assertTrue(result.isSuccess());
		assertTrue(result.get().isEmpty());
	}

	@Test
	void deduplicatesMultipleSharesFromSameOwner() throws Exception {
		final Account owner = server.getAccountFactory().create();
		final Account user1 = server.getAccountFactory().create();
		final Account user2 = server.getAccountFactory().create();
		shareRootFolder(owner, user1);
		shareRootFolder(owner, user2);

		Try<List<Account>> result = accountService.getSharedAccounts(user1.getId());

		assertTrue(result.isSuccess());
		assertEquals(1, result.get().size());
	}

	@Test
	void skipsDeletedOwners() throws Exception {
		final Account owner = server.getAccountFactory().create();
		final Account user = server.getAccountFactory().create();
		shareRootFolder(owner, user);
		Provisioning.getInstance().deleteAccount(owner.getId());

		Try<List<Account>> result = accountService.getSharedAccounts(user.getId());

		assertTrue(result.isSuccess());
		assertTrue(result.get().isEmpty());
	}

	@Test
	void failsForNonExistentAccount() {
		Try<List<Account>> result = accountService.getSharedAccounts("non-existent-id");

		assertTrue(result.isFailure());
		assertInstanceOf(ServiceException.class, result.getCause());
	}

	private static void shareRootFolder(Account owner, Account grantee) throws ServiceException {
		Mailbox mailbox = MailboxManager.getInstance().getMailboxByAccount(owner);
		Folder rootFolder = mailbox.getFolderById(new OperationContext(owner), Mailbox.ID_FOLDER_USER_ROOT);
		ACL.Grant grant = mailbox.grantAccess(
				new OperationContext(owner), Mailbox.ID_FOLDER_USER_ROOT,
				grantee.getId(), ACL.GRANTEE_USER, ACL.RIGHT_READ, null);
		String serialized = AclPushSerializer.serialize(rootFolder, grant);
		owner.addSharedItem(serialized);
	}

	private static void shareSubFolder(Account owner, Account grantee, String folderName)
			throws ServiceException {
		Mailbox mailbox = MailboxManager.getInstance().getMailboxByAccount(owner);
		Folder folder = mailbox.createFolder(
				new OperationContext(owner), folderName, Mailbox.ID_FOLDER_USER_ROOT,
				new Folder.FolderOptions());
		ACL.Grant grant = mailbox.grantAccess(
				new OperationContext(owner), folder.getId(),
				grantee.getId(), ACL.GRANTEE_USER, ACL.RIGHT_READ, null);
		String serialized = AclPushSerializer.serialize(folder, grant);
		owner.addSharedItem(serialized);
	}

	@Test
	void searchAccountsByDisplayName() throws Exception {
		Account caller = server.getAccountFactory().create();
		Account target = server.getAccountFactory()
				.withAttribute(ZAttrProvisioning.A_displayName, "Matteo Galvagni")
				.create();

		Try<AccountSearchResponse> result = accountService.searchAccounts("matteo", caller.getId(), 100, 0);

		assertTrue(result.isSuccess());
		assertTrue(result.get().accounts().stream().anyMatch(e -> e.id().equals(target.getId())),
				"Expected target account to appear in search results");
	}

	@Test
	void searchAccountsByEmail() throws Exception {
		Account caller = server.getAccountFactory().create();
		Account target = server.getAccountFactory()
				.withUsername("findme-" + java.util.UUID.randomUUID())
				.create();

		Try<AccountSearchResponse> result = accountService.searchAccounts("findme", caller.getId(), 100, 0);

		assertTrue(result.isSuccess());
		assertTrue(result.get().accounts().stream().anyMatch(e -> e.id().equals(target.getId())),
				"Expected target account to appear in search results by email");
	}

	@Test
	void searchExcludesHiddenInGal() throws Exception {
		Account caller = server.getAccountFactory().create();
		Account visible = server.getAccountFactory()
				.withAttribute(ZAttrProvisioning.A_displayName, "VisibleUser-" + java.util.UUID.randomUUID())
				.create();
		Account hidden = server.getAccountFactory()
				.withAttribute(ZAttrProvisioning.A_displayName, "HiddenUser-" + java.util.UUID.randomUUID())
				.withAttribute(ZAttrProvisioning.A_zimbraHideInGal, "TRUE")
				.create();

		Try<AccountSearchResponse> result = accountService.searchAccounts("User", caller.getId(), 100, 0);

		assertTrue(result.isSuccess());
		assertTrue(result.get().accounts().stream().anyMatch(e -> e.id().equals(visible.getId())),
				"Expected visible account to appear");
		assertFalse(result.get().accounts().stream().anyMatch(e -> e.id().equals(hidden.getId())),
				"Expected hidden account to be excluded");
	}

	@Test
	void searchRespectsLimitAndOffset() throws Exception {
		String uniqueSuffix = "limitoffset-" + java.util.UUID.randomUUID();
		Account caller = server.getAccountFactory()
				.withAttribute(ZAttrProvisioning.A_displayName, uniqueSuffix + "-caller")
				.create();
		server.getAccountFactory()
				.withAttribute(ZAttrProvisioning.A_displayName, uniqueSuffix + "-a")
				.create();
		server.getAccountFactory()
				.withAttribute(ZAttrProvisioning.A_displayName, uniqueSuffix + "-b")
				.create();
		server.getAccountFactory()
				.withAttribute(ZAttrProvisioning.A_displayName, uniqueSuffix + "-c")
				.create();

		Try<AccountSearchResponse> result = accountService.searchAccounts(uniqueSuffix, caller.getId(), 2, 0);

		assertTrue(result.isSuccess());
		AccountSearchResponse response = result.get();
		assertEquals(2, response.accounts().size());
		assertEquals(4, response.total());
		assertTrue(response.more());
	}

	@Test
	void searchWithEmptyQueryReturnsAllAccounts() throws Exception {
		Account caller = server.getAccountFactory().create();

		Try<AccountSearchResponse> result = accountService.searchAccounts("", caller.getId(), 100, 0);

		assertTrue(result.isSuccess());
		assertTrue(result.get().total() > 0, "Empty query should return at least the existing accounts");
	}

	@Test
	void searchWithNonExistentCallerFails() {
		Try<AccountSearchResponse> result = accountService.searchAccounts("query", "non-existent-id", 10, 0);

		assertTrue(result.isFailure());
		assertInstanceOf(ServiceException.class, result.getCause());
	}
}
