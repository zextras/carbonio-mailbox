// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.cs.redolog.op;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.mailbox.Message;
import com.zimbra.cs.mime.ParsedMessage;
import com.zimbra.cs.mime.ParsedMessageDataSource;
import com.zimbra.cs.redolog.RedoLogInput;
import com.zimbra.cs.redolog.RedoLogOutput;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CreateMessageTest {
    private CreateMessage op;
    private Mailbox mbox;

    @BeforeAll
    public static void init() throws Exception {
        MailboxTestUtil.initServer();
    }

    @BeforeEach
    public void setUp() throws Exception {
        Account account = Provisioning.getInstance().createAccount(
            "test@zimbra.com", "secret", new HashMap<String, Object>());
        mbox = MailboxManager.getInstance().getMailboxByAccount(account);

        ParsedMessage pm = MailboxTestUtil.generateMessage("test");
        final long msgSize = pm.getRawInputStream().available();

        op = new CreateMessage(mbox.getId() /* mailboxId */, "rcpt@example.com",
                               false /* shared */, "message digest",
                               msgSize /* msgSize */, 6 /* folderId */,
                               true /* noICal */, 0 /* flags */,
                               new String[] {"tag"});
        op.setMessageBodyInfo(new ParsedMessageDataSource(pm), msgSize);
        op.setMessageId(-1);
        op.setConvId(-1);
    }

    @AfterEach
    public void tearDown() throws Exception {
        MailboxTestUtil.clearData();
    }

 @Test
 void startSetsTimestamp() {
  assertEquals(-1, op.mReceivedDate, "receivedDate != -1 before start");
  op.start(7);
  assertEquals(7, op.mReceivedDate, "receivedDate != 7");
  assertEquals(7, op.getTimestamp(), "timestamp != 7");
 }

 @Test
 void serializeDeserialize() throws Exception {
  ByteArrayOutputStream out = new ByteArrayOutputStream();
  op.serializeData(new RedoLogOutput(out));

  // reset op
  op = new CreateMessage();
  op.deserializeData(
    new RedoLogInput(new ByteArrayInputStream(out.toByteArray())));
  assertEquals("rcpt@example.com", op.getRcptEmail());
  assertEquals(6, op.getFolderId());
  assertEquals(0, op.getFlags());
  assertEquals(new String[]{"tag"}, op.getTags());
  assertEquals(":streamed:", op.getPath());
  assertEquals("",
    CharStreams.toString(new InputStreamReader(
      op.getAdditionalDataStream(), Charsets.UTF_8)),
    "Input stream is not empty");
 }

 @Test
 void redo() throws Exception {
  op.redo();

  // Look in the mailbox and see if the message is there.
  Message msg =
    mbox.getMessageById(op.getOperationContext(), mbox.getLastItemId());
  assertEquals("test", msg.getSubject(), "subject != test");
  assertEquals("Bob Evans <bob@example.com>", msg.getSender(), "sender != bob@example.com");
  assertEquals(6, msg.getFolderId(), "folderId != 6");
 }
}
