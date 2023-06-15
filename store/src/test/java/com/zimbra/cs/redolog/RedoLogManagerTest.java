// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.cs.redolog;

import static org.junit.jupiter.api.Assertions.*;

import com.zimbra.cs.mailbox.MailboxOperation;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.redolog.op.RedoableOp;
import org.easymock.EasyMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File;

public class RedoLogManagerTest {
    private RedoLogManager redoLogManager;

    @BeforeAll
    public static void init() throws Exception {
        MailboxTestUtil.initServer();
    }

    @BeforeEach
    public void setUp() throws Exception {
        redoLogManager = RedoLogProvider.getInstance().getRedoLogManager();
        redoLogManager.start();
    }

    @AfterEach
    public void tearDown() throws Exception {
        redoLogManager.stop();
    }

 @Test
 void internalState() throws Exception {
  assertEquals("build/test/redo/redo.log",
    redoLogManager.getLogFile().getPath(),
    "path != build/test/redo/redo.log");
  assertEquals("build/test/redo",
    redoLogManager.getArchiveDir().getPath(),
    "archiveDir != build/test/redo");
  assertEquals("build/test/redo",
    redoLogManager.getRolloverDestDir().getPath(),
    "rolloverDestDir != build/test/redo");
  assertFalse(redoLogManager.getInCrashRecovery(),
    "crashRecovery == true");
 }

 @Test
 void transactionIdIncrements() throws Exception {
  TransactionId id = redoLogManager.getNewTxnId();

  // Get the next transaction ID
  TransactionId nextId = redoLogManager.getNewTxnId();
  assertEquals(
    id.getCounter() + 1, nextId.getCounter(), "nextId.getCounter() should be one more than id.getCounter()");
 }

 @Test
 void rolloverIncrementsLogSequence() throws Exception {
  long currentSequence = redoLogManager.getCurrentLogSequence();
  File previousFile = redoLogManager.forceRollover();
  // No change, since no ops have been logged
  assertEquals(
    currentSequence, redoLogManager.getCurrentLogSequence(), "Log sequence should not change. Nothing logged before rollover.");
  assertNull(
    previousFile,
    "No rollover occured, so previous log file should be NULL.");

  RedoableOp op = EasyMock.createMockBuilder(RedoableOp.class)
    .withConstructor(MailboxOperation.Preview)
    .createMock();

  // Run the operation and log.
  op.start(7 /* timestamp */);
  op.log();
  assertEquals(currentSequence,
    redoLogManager.getCurrentLogSequence(),
    "sequence number should be the same as before.");
  previousFile = redoLogManager.forceRollover();

  assertEquals(
    currentSequence + 1, redoLogManager.getCurrentLogSequence(), "Forced rollover after a log should increment sequence number");
  assertTrue(
    previousFile.getName().contains("seq" + currentSequence + ".log"));
 }
}
