// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.cs.redolog;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.redolog.op.CommitTxn;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class CommitIdTest {
    private CommitId id;
    private CommitTxn commitTxn;

  @BeforeEach
  public void setUp() {
    commitTxn = Mockito.mock(CommitTxn.class);
    when(commitTxn.getTimestamp()).thenReturn(1L);
    when(commitTxn.getTransactionId()).thenReturn(new TransactionId(2, 3));
  }

  @Test
  void id() {
    id = new CommitId(4, commitTxn);
    assertEquals(4, id.getRedoSeq(), "Sequence != 4");
    assertEquals("4-1-2-3", id.encodeToString(), "encodeToString != 4-1-2-3");
  }

  @Test
  void matches() {
    // First call to matches()
    when(commitTxn.getTimestamp()).thenReturn(1L);
    when(commitTxn.getTransactionId()).thenReturn(new TransactionId(2, 3));

    id = new CommitId(4, commitTxn);
    assertTrue(id.matches(commitTxn));

    // Second call to matches() with different timestamp
    when(commitTxn.getTimestamp()).thenReturn(5L);
    when(commitTxn.getTransactionId()).thenReturn(new TransactionId(2, 3));

    assertFalse(id.matches(commitTxn));
  }

  @Test
  void encodeDecode() throws Exception {
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
