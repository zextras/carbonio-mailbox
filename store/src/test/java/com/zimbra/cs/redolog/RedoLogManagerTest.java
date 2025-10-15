// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.cs.redolog;

import static org.junit.jupiter.api.Assertions.*;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.common.localconfig.LC;
import com.zimbra.cs.mailbox.MailboxOperation;
import com.zimbra.cs.redolog.op.RedoableOp;
import java.io.File;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class RedoLogManagerTest extends MailboxTestSuite {

	private RedoLogManager redoLogManager;

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
	void internalState() {
		final String volumeDirectory = LC.zimbra_home.value();
		assertEquals(volumeDirectory + "/redolog/redo.log",
				redoLogManager.getLogFile().getPath());
		assertEquals(volumeDirectory + "/redolog/archive",
				redoLogManager.getArchiveDir().getPath());
		assertEquals(volumeDirectory + "/redolog/archive",
				redoLogManager.getRolloverDestDir().getPath());
		assertFalse(redoLogManager.getInCrashRecovery(),
				"crashRecovery == true");
	}

	@Test
	void transactionIdIncrements() throws Exception {
		TransactionId id = redoLogManager.getNewTxnId();

		// Get the next transaction ID
		TransactionId nextId = redoLogManager.getNewTxnId();
		assertEquals(
				id.getCounter() + 1, nextId.getCounter(),
				"nextId.getCounter() should be one more than id.getCounter()");
	}

	@Test
	void rolloverIncrementsLogSequence() throws Exception {
		long currentSequence = redoLogManager.getCurrentLogSequence();
		File previousFile = redoLogManager.forceRollover();
		// No change, since no ops have been logged
		assertEquals(
				currentSequence, redoLogManager.getCurrentLogSequence(),
				"Log sequence should not change. Nothing logged before rollover.");
		assertNull(
				previousFile,
				"No rollover occured, so previous log file should be NULL.");

		RedoableOp op = Mockito.mock(RedoableOp.class,
				Mockito.withSettings()
						.useConstructor(MailboxOperation.Preview)
						.defaultAnswer(Mockito.CALLS_REAL_METHODS));
		// Run the operation and log.
		op.start(7 /* timestamp */);
		op.log();
		assertEquals(currentSequence,
				redoLogManager.getCurrentLogSequence(),
				"sequence number should be the same as before.");
		previousFile = redoLogManager.forceRollover();

		assertEquals(
				currentSequence + 1, redoLogManager.getCurrentLogSequence(),
				"Forced rollover after a log should increment sequence number");
		assertTrue(
				previousFile.getName().contains("seq" + currentSequence + ".log"));
	}
}
