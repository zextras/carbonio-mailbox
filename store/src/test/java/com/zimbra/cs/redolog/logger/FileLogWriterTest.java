// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.cs.redolog.logger;

import static org.junit.jupiter.api.Assertions.*;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.cs.mailbox.MailboxOperation;
import com.zimbra.cs.redolog.RedoLogManager;
import com.zimbra.cs.redolog.op.RedoableOp;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

public class FileLogWriterTest extends MailboxTestSuite {

	@TempDir
	public File folder;

	private RedoLogManager mockRedoLogManager;
	private FileLogWriter logWriter;

	@BeforeEach
	public void setUp() throws Exception {
		mockRedoLogManager = Mockito.mock(RedoLogManager.class);

		logWriter =
				new FileLogWriter(mockRedoLogManager, new File(folder.getAbsolutePath(), "logfile"),
						10 /* fsync interval in ms */);
	}

	@Test
	void openLogClose() throws Exception {
		assertTrue(logWriter.isEmpty(), "file starts empty");
		logWriter.open();
		assertTrue(logWriter.isEmpty(), "file empty after open");

		RedoableOp op = Mockito.mock(RedoableOp.class,
				Mockito.withSettings()
						.useConstructor(MailboxOperation.Preview)
						.defaultAnswer(Mockito.CALLS_REAL_METHODS));

		logWriter.log(op, new ByteArrayInputStream("some bytes".getBytes()),
				false /* asynchronous */);
		// The file is the size of the header plus the op bytes (10)
		assertEquals(FileHeader.HEADER_LEN + 10, logWriter.getSize(), "file size incorrect.");
		logWriter.close();
		// store some fields from the current writer.
		final long createTime = logWriter.getCreateTime();
		final long sequence = logWriter.getSequence();

		// reset the FileLogWriter
		logWriter =
				new FileLogWriter(mockRedoLogManager, new File(folder.getAbsolutePath(), "logfile"),
						10 /* fsync interval in ms */);
		assertEquals(FileHeader.HEADER_LEN + 10, logWriter.getSize(), "file size incorrect.");
		logWriter.open();

		assertEquals(createTime, logWriter.getCreateTime());
		assertEquals(sequence, logWriter.getSequence());
	}

	@Test
	void logBeforeOpen() throws Exception {
		assertThrows(IOException.class, () -> {
			logWriter.log(null, null, false);
		});
	}
}
