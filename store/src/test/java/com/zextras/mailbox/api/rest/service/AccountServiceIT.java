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
import com.zimbra.cs.mailbox.Folder;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.OperationContext;
import com.zextras.mailbox.util.MailboxServerExtension;
import io.vavr.control.Try;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class AccountServiceIT {

	@RegisterExtension
	static final MailboxServerExtension server = new MailboxServerExtension();

	private static AccountService accountService;

	@BeforeAll
	static void setUp() throws Exception {
		accountService = new AccountService(
				Provisioning::getInstance,
				() -> {
					try {
						return MailboxManager.getInstance();
					} catch (ServiceException e) {
						throw new RuntimeException(e);
					}
				});
	}

	@Test
	void returnsEmptyWhenNoMountpoints() throws Exception {
		final Account user = server.getAccountFactory().create();
		mailboxOf(user);

		Try<List<Account>> result = accountService.getSharedAccounts(user.getId());

		assertTrue(result.isSuccess());
		assertTrue(result.get().isEmpty());
	}

	@Test
	void deduplicatesMultipleMountpointsToSameOwner() throws Exception {
		final Account owner = server.getAccountFactory().create();
		final Account user = server.getAccountFactory().create();
		final Folder folder1 = createFolder(owner, "shared-1");
		final Folder folder2 = createFolder(owner, "shared-2");
		createMountpoint(user, owner, folder1, "mount-1");
		createMountpoint(user, owner, folder2, "mount-2");

		Try<List<Account>> result = accountService.getSharedAccounts(user.getId());

		assertTrue(result.isSuccess());
		assertEquals(1, result.get().size());
		assertEquals(owner.getId(), result.get().get(0).getId());
	}

	@Test
	void excludesSelfMountpoints() throws Exception {
		final Account user = server.getAccountFactory().create();
		final Folder folder = createFolder(user, "my-folder");
		createMountpoint(user, user, folder, "self-mount");

		Try<List<Account>> result = accountService.getSharedAccounts(user.getId());

		assertTrue(result.isSuccess());
		assertTrue(result.get().isEmpty());
	}

	@Test
	void skipsDeletedOwners() throws Exception {
		final Account owner = server.getAccountFactory().create();
		final Account user = server.getAccountFactory().create();
		final Folder folder = createFolder(owner, "shared-folder");
		createMountpoint(user, owner, folder, "mount");
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

	private static Mailbox mailboxOf(Account account) throws ServiceException {
		return MailboxManager.getInstance().getMailboxByAccount(account);
	}

	private static Folder createFolder(Account owner, String name) throws ServiceException {
		return mailboxOf(owner).createFolder(
				new OperationContext(owner), name, Mailbox.ID_FOLDER_USER_ROOT,
				new Folder.FolderOptions().setDefaultView(MailItem.Type.MESSAGE));
	}

	private static void createMountpoint(Account user, Account owner, Folder remoteFolder,
			String name) throws ServiceException {
		mailboxOf(user).createMountpoint(
				new OperationContext(user), Mailbox.ID_FOLDER_USER_ROOT, name,
				owner.getId(), remoteFolder.getId(), remoteFolder.getUuid(),
				MailItem.Type.MESSAGE, 0, (byte) 0, false);
	}
}
