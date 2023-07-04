// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.cs.redolog.op;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.redolog.RedoLogInput;
import com.zimbra.cs.redolog.RedoLogOutput;
import com.zimbra.cs.redolog.TransactionId;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.LinkedHashSet;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class CheckpointTest {
    private Checkpoint op;

    @BeforeAll
    public static void init() throws Exception {
        MailboxTestUtil.initServer();
    }

 @Test
 void testDefaultConstructor() {
  op = new Checkpoint();
  assertEquals(0, op.getNumActiveTxns());
  assertNull(op.getTransactionId());
 }

 @Test
 void testSetConstructor() {
  LinkedHashSet<TransactionId> txns = new LinkedHashSet<TransactionId>();
  txns.add(new TransactionId(1, 2));
  txns.add(new TransactionId(3, 4));

  op = new Checkpoint(txns);
  assertEquals(new TransactionId(), op.getTransactionId());
  assertEquals(2, op.getNumActiveTxns(), "expected 2 active transactions.");
  assertEquals(txns, op.getActiveTxns(), "Transactions don't match.");
 }

 @Test
 void serializeDeserialize() throws Exception {
  LinkedHashSet<TransactionId> txns = new LinkedHashSet<TransactionId>();
  txns.add(new TransactionId(1, 2));
  txns.add(new TransactionId(3, 4));

  op = new Checkpoint(txns);
  ByteArrayOutputStream out = new ByteArrayOutputStream();
  op.serializeData(new RedoLogOutput(out));

  // reset op
  op = new Checkpoint();
  op.deserializeData(
    new RedoLogInput(new ByteArrayInputStream(out.toByteArray())));
  assertEquals(txns, op.getActiveTxns(), "Transactions don't match after deserialize.");
 }
}
