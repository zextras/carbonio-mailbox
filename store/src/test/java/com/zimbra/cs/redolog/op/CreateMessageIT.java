package com.zimbra.cs.redolog.op;

import static org.junit.jupiter.api.Assertions.*;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.mime.ParsedMessage;
import com.zimbra.cs.mime.ParsedMessageDataSource;
import com.zimbra.cs.redolog.RedoLogInput;
import com.zimbra.cs.redolog.RedoLogOutput;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import org.junit.jupiter.api.Test;

public class CreateMessageIT extends MailboxTestSuite {

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

	private Mailbox createTestMailbox() throws Exception {
		Account account = createAccount().create();
		return MailboxManager.getInstance().getMailboxByAccount(account);
	}

	@Test
	void serializeDeserialize() throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		CreateMessage op = createTestMessageRedoableOp(createTestMailbox());
		op.serializeData(new RedoLogOutput(out));

		// reset op
		op = new CreateMessage();
		op.deserializeData(
				new RedoLogInput(new ByteArrayInputStream(out.toByteArray())));
		assertEquals("rcpt@example.com", op.getRcptEmail());
		assertEquals(6, op.getFolderId());
		assertEquals(0, op.getFlags());
		assertArrayEquals(new String[]{"tag"}, op.getTags());
		assertEquals(":streamed:", op.getPath());
		assertEquals("",
				CharStreams.toString(new InputStreamReader(
						op.getAdditionalDataStream(), Charsets.UTF_8)),
				"Input stream is not empty");
	}
}
