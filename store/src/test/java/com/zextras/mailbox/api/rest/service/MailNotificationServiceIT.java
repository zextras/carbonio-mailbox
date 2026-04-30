/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.api.rest.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zextras.mailbox.api.rest.resource.dto.SendNotificationRequest;
import com.zextras.mailbox.util.MailboxServerExtension;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.Flag;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.Message;
import io.vavr.control.Try;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import javax.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

@Tag("e2e")
class MailNotificationServiceIT {

	@RegisterExtension
	static final MailboxServerExtension server = new MailboxServerExtension();

	private static MailNotificationService service;

	@BeforeAll
	static void setUp() {
		service = new MailNotificationService(
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
	void notificationLandsInInboxAsHighPriorityMessage() throws Exception {
		final Account recipient = server.getAccountFactory().create();
		final String subject = "Your storage is almost full";
		final String body = "<html><body>Quota warning</body></html>";

		final Try<Integer> result = service.send(
				new SendNotificationRequest(subject, body, List.of(recipient.getId())));

		final List<Message> inbox = inboxMessagesOf(recipient);
		final Message delivered = inbox.get(0);
		final MimeMessage mime = delivered.getMimeMessage();
		assertTrue(result.isSuccess());
		assertEquals(1, result.get());
		assertEquals(1, inbox.size());
		assertEquals(subject, delivered.getSubject());
		assertEquals("postmaster@" + recipient.getDomainName(), delivered.getSender());
		assertTrue(delivered.isUnread());
		assertTrue(delivered.isTagged(Flag.FlagInfo.HIGH_PRIORITY));
		assertEquals("text/html; charset=UTF-8", mime.getContentType());
		assertEquals(recipient.getName(), mime.getRecipients(MimeMessage.RecipientType.TO)[0].toString());
		assertEquals(body, ((String) mime.getContent()).trim());
	}

	@Test
	void eachRecipientReceivesNotification() throws Exception {
		final Account first = server.getAccountFactory().create();
		final Account second = server.getAccountFactory().create();
		final String subject = "shared subject";

		final Try<Integer> result = service.send(new SendNotificationRequest(
				subject, "<p>body</p>", List.of(first.getId(), second.getId())));

		assertTrue(result.isSuccess());
		assertEquals(2, result.get());
		assertEquals(subject, onlyInboxMessageOf(first).getSubject());
		assertEquals(subject, onlyInboxMessageOf(second).getSubject());
	}

	@Test
	void senderDomainMatchesRecipientDomain() throws Exception {
		final Account first = createAccountOnNewDomain();
		final Account second = createAccountOnNewDomain();
		final Account third = createAccountOnNewDomain();

		final Try<Integer> result = service.send(new SendNotificationRequest(
				"subj", "<p>body</p>", List.of(first.getId(), second.getId(), third.getId())));

		assertTrue(result.isSuccess());
		assertEquals("postmaster@" + first.getDomainName(), onlyInboxMessageOf(first).getSender());
		assertEquals("postmaster@" + second.getDomainName(), onlyInboxMessageOf(second).getSender());
		assertEquals("postmaster@" + third.getDomainName(), onlyInboxMessageOf(third).getSender());
	}

	@Test
	void unknownRecipientDoesNotBlockOthers() throws Exception {
		final Account recipient = server.getAccountFactory().create();

		String unknownAccountId = "00000000-0000-0000-0000-000000000000";
		final Try<Integer> result = service.send(new SendNotificationRequest(
				"subj", "<p>body</p>",
				List.of(unknownAccountId, recipient.getId())));

		assertTrue(result.isSuccess());
		assertEquals(1, inboxMessagesOf(recipient).size());
	}

	private static List<Message> inboxMessagesOf(Account account) throws ServiceException {
		final Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);
		return mbox.getItemList(null, MailItem.Type.MESSAGE, Mailbox.ID_FOLDER_INBOX)
				.stream()
				.map(Message.class::cast)
				.toList();
	}

	private static Message onlyInboxMessageOf(Account account) throws ServiceException {
		final List<Message> inbox = inboxMessagesOf(account);
		assertEquals(1, inbox.size());
		return inbox.getFirst();
	}

	private static Account createAccountOnNewDomain() throws ServiceException {
		final Domain domain = Provisioning.getInstance()
				.createDomain(UUID.randomUUID() + ".com", new HashMap<>());
		return server.getAccountFactory().withDomain(domain.getName()).create();
	}
}
