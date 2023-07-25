// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.cs.redolog.op;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.zimbra.cs.mailbox.MailboxOperation;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.redolog.RedoLogInput;
import com.zimbra.cs.redolog.RedoLogOutput;
import com.zimbra.cs.redolog.TransactionId;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import org.easymock.EasyMock;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AbortTxnTest {
    private AbortTxn op;
    private RedoableOp changeEntry;

    @BeforeAll
    public static void init() throws Exception {
        MailboxTestUtil.initServer();
    }

    @BeforeEach
    public void setUp() {
        changeEntry = EasyMock.createMockBuilder(CopyItem.class)
                          .withConstructor()
                          .addMockedMethod("getTransactionId")
                          .addMockedMethod("getMailboxId")
                          .createMock();
        EasyMock.expect(changeEntry.getTransactionId())
            .andStubReturn(new TransactionId(1, 2));
        EasyMock.expect(changeEntry.getMailboxId()).andStubReturn(5);

        EasyMock.replay(changeEntry);
    }

 @Test
 void testDefaultConstructor() {
  op = new AbortTxn();
  assertNull(op.getTransactionId(),
    "TransactionId is set on new op.");
 }

 @Test
 void testOpConstructor() {
  op = new AbortTxn(changeEntry);
  assertEquals(5, op.getMailboxId(), "mailboxid != 5");
  assertEquals(new TransactionId(1, 2), op.getTransactionId(), "Transactionid != 1, 2");
  assertEquals(MailboxOperation.CopyItem, op.getTxnOpCode());
 }

 @Test
 void serializeDeserialize() throws Exception {
  op = new AbortTxn(changeEntry);
  ByteArrayOutputStream out = new ByteArrayOutputStream();
  op.serializeData(new RedoLogOutput(out));

  // reset op
  op = new AbortTxn();
  op.deserializeData(
    new RedoLogInput(new ByteArrayInputStream(out.toByteArray())));
  assertEquals(MailboxOperation.CopyItem, op.getTxnOpCode(), "opcode should be CopyItem after deserialize.");
 }
}
