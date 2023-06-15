// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.cs.redolog.op;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.zimbra.cs.mailbox.MailboxOperation;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.redolog.RedoCommitCallback;
import com.zimbra.cs.redolog.RedoLogInput;
import com.zimbra.cs.redolog.RedoLogOutput;
import com.zimbra.cs.redolog.TransactionId;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import org.easymock.EasyMock;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CommitTxnTest {
    private CommitTxn op;
    private RedoCommitCallback callback;
    private RedoableOp changeEntry;

    @BeforeAll
    public static void init() throws Exception {
        MailboxTestUtil.initServer();
    }

    @BeforeEach
    public void setUp() {
        callback = EasyMock.createStrictMock(RedoCommitCallback.class);
        changeEntry = EasyMock.createMockBuilder(CopyItem.class)
                          .withConstructor()
                          .addMockedMethod("getTransactionId")
                          .addMockedMethod("getMailboxId")
                          .addMockedMethod("getCommitCallback")
                          .createMock();
        EasyMock.expect(changeEntry.getTransactionId())
            .andStubReturn(new TransactionId(1, 2));
        EasyMock.expect(changeEntry.getMailboxId()).andStubReturn(5);
        EasyMock.expect(changeEntry.getCommitCallback())
            .andStubReturn(callback);

        EasyMock.replay(changeEntry);
    }

 @Test
 void testDefaultConstructor() {
  op = new CommitTxn();
  assertNull(op.getTransactionId());
 }

 @Test
 void testOpConstructor() {
  op = new CommitTxn(changeEntry);
  assertEquals(5, op.getMailboxId());
  assertEquals(new TransactionId(1, 2), op.getTransactionId());
  assertEquals(MailboxOperation.CopyItem, op.getTxnOpCode());
  assertEquals(callback, op.getCallback());
 }

 @Test
 void serializeDeserialize() throws Exception {
  op = new CommitTxn(changeEntry);
  ByteArrayOutputStream out = new ByteArrayOutputStream();
  op.serializeData(new RedoLogOutput(out));

  // reset op
  op = new CommitTxn();
  op.deserializeData(
    new RedoLogInput(new ByteArrayInputStream(out.toByteArray())));
  assertEquals(MailboxOperation.CopyItem, op.getTxnOpCode(), "opcode should be CopyItem after deserialize.");
 }
}
