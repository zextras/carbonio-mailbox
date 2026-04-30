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
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.Flag;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.Message;
import io.vavr.control.Try;
import java.util.List;
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
	void deliversNotificationToInboxWithExpectedHeadersBodyAndFlags() throws Exception {
		final Account recipient = server.getAccountFactory().create();
		final String subject = "Your storage is almost full";
		final String body = "<html><body>Quota warning</body></html>";

		final Try<Integer> result = service.send(
				new SendNotificationRequest(subject, body, List.of(recipient.getId())));

		assertTrue(result.isSuccess());
		assertEquals(1, result.get());

		final List<Message> inbox = inboxMessagesOf(recipient);
		assertEquals(1, inbox.size());

		final Message delivered = inbox.get(0);
		assertEquals(subject, delivered.getSubject());
		assertEquals("postmaster@test.com", delivered.getSender());
		assertTrue(delivered.isUnread());
		assertTrue(delivered.isTagged(Flag.FlagInfo.HIGH_PRIORITY));

		final MimeMessage mime = delivered.getMimeMessage();
		assertTrue(mime.getContentType().toLowerCase().startsWith("text/html"));
		assertEquals(recipient.getName(), mime.getRecipients(MimeMessage.RecipientType.TO)[0].toString());
		assertEquals(body, ((String) mime.getContent()).trim());
	}

	@Test
	void deliversToEachRecipientWhenMultiple() throws Exception {
		final Account first = server.getAccountFactory().create();
		final Account second = server.getAccountFactory().create();

		final Try<Integer> result = service.send(new SendNotificationRequest(
				"subj", "<p>body</p>", List.of(first.getId(), second.getId())));

		assertTrue(result.isSuccess());
		assertEquals(2, result.get());
		assertEquals(1, inboxMessagesOf(first).size());
		assertEquals(1, inboxMessagesOf(second).size());
	}

	@Test
	void skipsUnknownRecipientButStillDeliversToOthers() throws Exception {
		final Account recipient = server.getAccountFactory().create();

		final Try<Integer> result = service.send(new SendNotificationRequest(
				"subj", "<p>body</p>",
				List.of("00000000-0000-0000-0000-000000000000", recipient.getId())));

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
}
