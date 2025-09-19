// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.cs.redolog.op;

import static org.junit.jupiter.api.Assertions.*;

import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.mailbox.Message;
import com.zimbra.cs.mime.ParsedMessage;
import com.zimbra.cs.mime.ParsedMessageDataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class CreateMessageTest {

	@BeforeAll
	public static void init() throws Exception {
		MailboxTestUtil.initServer();
	}


	private Mailbox createTestMailbox() throws Exception {
		Account account = Provisioning.getInstance().createAccount(UUID.randomUUID() +
				"@zimbra.com", "secret", new HashMap<String, Object>(
				Map.of(Provisioning.A_zimbraId, UUID.randomUUID().toString())
		));
		return MailboxManager.getInstance().getMailboxByAccount(account);
	}

	private CreateMessage createTestMessageRedoableOp(Mailbox mailbox) throws Exception {
		ParsedMessage pm = MailboxTestUtil.generateMessage("test");
		final long msgSize = pm.getRawInputStream().available();
		final CreateMessage op = new CreateMessage(mailbox.getId() /* mailboxId */, "rcpt@example.com",
				false /* shared */, "message digest",
				msgSize /* msgSize */, 6 /* folderId */,
				true /* noICal */, 0 /* flags */,
				new String[]{"tag"});
		op.setMessageBodyInfo(new ParsedMessageDataSource(pm), msgSize);
		op.setMessageId(-1);
		op.setConvId(-1);
		return op;
	}

	@Test
	void startSetsTimestamp() throws Exception {
		final CreateMessage op = createTestMessageRedoableOp(createTestMailbox());
		assertEquals(-1, op.mReceivedDate, "receivedDate != -1 before start");
		op.start(7);
		assertEquals(7, op.mReceivedDate, "receivedDate != 7");
		assertEquals(7, op.getTimestamp(), "timestamp != 7");
	}

	@Test
	void redo() throws Exception {
		final Mailbox testMailbox = createTestMailbox();
		final CreateMessage op = createTestMessageRedoableOp(testMailbox);
		op.redo();

		// Look in the mailbox and see if the message is there.
		Message msg =
				testMailbox.getMessageById(op.getOperationContext(), testMailbox.getLastItemId());
		assertEquals("test", msg.getSubject(), "subject != test");
		assertEquals("Bob Evans <bob@example.com>", msg.getSender(), "sender != bob@example.com");
		assertEquals(6, msg.getFolderId(), "folderId != 6");
	}
}
