// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.cs.redolog.logger;

import static org.junit.jupiter.api.Assertions.*;

import com.zimbra.cs.mailbox.MailboxOperation;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.redolog.RedoLogManager;
import com.zimbra.cs.redolog.op.RedoableOp;
import org.easymock.EasyMock;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

public class  FileLogWriterTest {
 @TempDir
 public File folder;

    private RedoLogManager mockRedoLogManager;
    private FileLogWriter logWriter;

    @BeforeAll
    public static void init() throws Exception {
        MailboxTestUtil.initServer();
    }

    @BeforeEach
    public void setUp() throws Exception {
        mockRedoLogManager = EasyMock.createNiceMock(RedoLogManager.class);

        logWriter =
            new FileLogWriter(mockRedoLogManager, File.createTempFile("logfile", null, folder),
                              10 /* fsync interval in ms */);
    }

 @Test
 void openLogClose() throws Exception {
  assertTrue(logWriter.isEmpty(), "file starts empty");
  logWriter.open();
  assertTrue(logWriter.isEmpty(), "file empty after open");

  RedoableOp op = EasyMock.createMockBuilder(RedoableOp.class)
    .withConstructor(MailboxOperation.Preview)
    .createMock();

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
    new FileLogWriter(mockRedoLogManager, File.createTempFile("logfile", null, folder),
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
