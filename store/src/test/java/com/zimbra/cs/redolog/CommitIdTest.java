// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.cs.redolog;

import static org.junit.jupiter.api.Assertions.*;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.redolog.CommitId;
import com.zimbra.cs.redolog.TransactionId;
import com.zimbra.cs.redolog.op.CommitTxn;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CommitIdTest extends EasyMockSupport {
    private CommitId id;
    private CommitTxn commitTxn;

    @BeforeEach
    public void setUp() {
        commitTxn = createMock(CommitTxn.class);
        EasyMock.expect(commitTxn.getTimestamp()).andReturn((long)1);
        EasyMock.expect(commitTxn.getTransactionId())
            .andReturn(new TransactionId(2, 3));
    }

 @Test
 void id() {
  replayAll();
  id = new CommitId(4, commitTxn);
  assertEquals(4, id.getRedoSeq(), "Sequence != 4");
  assertEquals("4-1-2-3", id.encodeToString(), "encodeToString != 4-1-2-3");
 }

 @Test
 void matches() {
  EasyMock.expect(commitTxn.getTimestamp()).andReturn((long) 1);
  EasyMock.expect(commitTxn.getTransactionId())
    .andReturn(new TransactionId(2, 3));
  // Change timestamp for the next try.
  EasyMock.expect(commitTxn.getTimestamp()).andReturn((long) 5);
  EasyMock.expect(commitTxn.getTransactionId())
    .andReturn(new TransactionId(2, 3));
  replayAll();
  id = new CommitId(4, commitTxn);
  assertTrue(id.matches(commitTxn));

  assertFalse(id.matches(commitTxn));
 }

 @Test
 void encodeDecode() throws Exception {
  replayAll();
  id = new CommitId(4, commitTxn);
  assertEquals(id, CommitId.decodeFromString(id.encodeToString()), "id mismatch on decode");
 }

 @Test
 void decodeJunk() throws Exception {
  assertThrows(ServiceException.class, () -> {
   CommitId.decodeFromString("not-a-Commit-Id");
  });
 }
}
