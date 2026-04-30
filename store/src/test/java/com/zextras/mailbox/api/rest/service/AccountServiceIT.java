/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.api.rest.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.soap.SoapProvisioning;
import com.zimbra.cs.mailbox.ACL;
import com.zimbra.cs.mailbox.Folder;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.mailbox.acl.AclPushTask;
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
		mailbox.grantAccess(
				new OperationContext(owner), Mailbox.ID_FOLDER_USER_ROOT,
				grantee.getId(), ACL.GRANTEE_USER, ACL.RIGHT_READ, null);
		AclPushTask.doWork();
	}

	private static void shareSubFolder(Account owner, Account grantee, String folderName)
			throws ServiceException {
		Mailbox mailbox = MailboxManager.getInstance().getMailboxByAccount(owner);
		Folder folder = mailbox.createFolder(
				new OperationContext(owner), folderName, Mailbox.ID_FOLDER_USER_ROOT,
				new Folder.FolderOptions());
		mailbox.grantAccess(
				new OperationContext(owner), folder.getId(),
				grantee.getId(), ACL.GRANTEE_USER, ACL.RIGHT_READ, null);
		AclPushTask.doWork();
	}
}
