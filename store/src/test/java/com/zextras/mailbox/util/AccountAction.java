/*
 * SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.util;

import static com.zimbra.cs.account.GuestAccount.GUID_PUBLIC;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.ACLUtil;
import com.zimbra.cs.account.accesscontrol.GranteeType;
import com.zimbra.cs.account.accesscontrol.Right;
import com.zimbra.cs.account.accesscontrol.RightManager;
import com.zimbra.cs.account.accesscontrol.RightModifier;
import com.zimbra.cs.account.accesscontrol.ZimbraACE;
import com.zimbra.cs.mailbox.ACL;
import com.zimbra.cs.mailbox.DeliveryOptions;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.Message;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.mime.ParsedMessage;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import javax.mail.internet.MimeMessage;

/**
 * Performs actions on an account. Start with {@link #shareWith(Account)}
 */
public class AccountAction {

	private final Account account;
	private final MailboxManager mailboxManager;
	private final RightManager rightManager;

	/**
	 * Saves a message in current Account mailbox. It is useful when you want to "simulate" receiving
	 * of a message.
	 *
	 * @param message message to save
	 * @return saved {@link javax.mail.Message}
	 * @throws ServiceException
	 * @throws IOException
	 */
	public Message saveMsgInInbox(javax.mail.Message message) throws ServiceException, IOException {
		final var parsedMessage = new ParsedMessage((MimeMessage) message, false);
		final var deliveryOptions =
				new DeliveryOptions().setFolderId(Mailbox.ID_FOLDER_INBOX);
		return mailboxManager
				.getMailboxByAccount(account)
				.addMessage(null, parsedMessage, deliveryOptions, null);
	}

	public Message saveDraft(ParsedMessage message) throws ServiceException, IOException {
		return mailboxManager
				.getMailboxByAccount(account)
				.saveDraft(new OperationContext(account), message, Mailbox.ID_AUTO_INCREMENT);
	}

	public static class Factory {

		private final MailboxManager mailboxManager;
		private final RightManager rightManager;

		public Factory(MailboxManager mailboxManager, RightManager rightManager) {
			this.mailboxManager = mailboxManager;
			this.rightManager = rightManager;
		}

		public AccountAction forAccount(Account account) {
			return new AccountAction(account, mailboxManager, rightManager);
		}

		public static Factory getDefault() throws ServiceException {
			return new Factory(MailboxManager.getInstance(), RightManager.getInstance());
		}
	}

	private AccountAction(
			Account account, MailboxManager mailboxManager, RightManager rightManager) {
		this.account = account;
		this.mailboxManager = mailboxManager;
		this.rightManager = rightManager;
	}

	/**
	 * Shares current account with target
	 *
	 * @param target AKA "delegated"
	 * @throws ServiceException
	 */
	public AccountAction shareWith(Account target) throws ServiceException {
		grantRightTo(target, rightManager.getRight(Right.RT_sendAs));
		grantFolderRightTo(target, "rw", Mailbox.ID_FOLDER_USER_ROOT);
		return this;
	}

	public AccountAction grantFolderRightTo(Account target, String rights, int folderId)
			throws ServiceException {
		mailboxManager
				.getMailboxByAccount(account)
				.grantAccess(
						null, folderId, target.getId(), ACL.GRANTEE_USER, ACL.stringToRights(rights), null);
		return this;
	}

	public AccountAction grantRightTo(Account target, Right right) throws ServiceException {
		final Set<ZimbraACE> aces = new HashSet<>();
		aces.add(
				new ZimbraACE(
						account.getId(),
						GranteeType.GT_USER,
						RightManager.getInstance().getRight(right.getName()),
						RightModifier.RM_CAN_DELEGATE,
						null));
		ACLUtil.grantRight(Provisioning.getInstance(), target, aces);
		return this;
	}


	public AccountAction grantPublicFolderRight(int folderId, String rights) throws ServiceException {
		mailboxManager
				.getMailboxByAccount(account)
				.grantAccess(
						null, folderId, GUID_PUBLIC, ACL.GRANTEE_PUBLIC, ACL.stringToRights(rights), null);
		return this;
	}
}
