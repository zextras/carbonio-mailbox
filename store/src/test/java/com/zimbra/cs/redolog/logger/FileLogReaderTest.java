// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.cs.redolog.logger;

import static org.junit.jupiter.api.Assertions.*;

import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.redolog.RedoLogManager;
import com.zimbra.cs.redolog.TransactionId;
import com.zimbra.cs.redolog.op.CopyItem;
import com.zimbra.cs.redolog.op.RedoableOp;
import org.easymock.EasyMock;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

public class FileLogReaderTest {
    private FileLogReader logReader;
    private FileLogWriter logWriter;
    private File logfile;

 @TempDir
 public File folder;

    @BeforeAll
    public static void init() throws Exception {
        MailboxTestUtil.initServer();
    }

    @BeforeEach
    public void setUp() throws Exception {
        logfile = File.createTempFile("logfile", null, folder);
        RedoLogManager mockRedoLogManager =
            EasyMock.createNiceMock(RedoLogManager.class);
        logReader = new FileLogReader(logfile);
        logWriter = new FileLogWriter(mockRedoLogManager, logfile,
                                      0 /* no fsync thread */);
    }

    private void writeOp(TransactionId id) throws IOException {
        logWriter.open();
        RedoableOp op = EasyMock.createMockBuilder(CopyItem.class)
                            .withConstructor()
                            .addMockedMethod("getTransactionId")
                            .createMock();
        EasyMock.expect(op.getTransactionId()).andStubReturn(id);

        EasyMock.replay(op);
        logWriter.log(op, op.getInputStream(), true /* synchronous */);
        logWriter.close();
    }

 @Test
 void openReadClose() throws Exception {
  writeOp(new TransactionId(7, 3));

  logReader.open();
  assertEquals(FileHeader.HEADER_LEN, logReader.position(), "Read file to unexpected position");
  RedoableOp op = logReader.getNextOp();
  assertEquals(FileHeader.HEADER_LEN,
    logReader.getLastOpStartOffset());
  assertEquals(op.getTransactionId(), new TransactionId(7, 3), "mismateched transactionid");
  assertNull(logReader.getNextOp(), "More ops in file.");
  logReader.close();
 }

 @Test
 void skipsJunkInFile() throws Exception {
  // open and close with logwriter to get a header.
  logWriter.open();
  logWriter.close();

  // seek to end of file and write junk
  RandomAccessFile raf = new RandomAccessFile(logfile, "rw");
  raf.seek(raf.length());
  raf.writeChars("This is junk in the file");
  raf.close();

  // Write using logWriter
  writeOp(new TransactionId(7, 3));

  // seek to end of file and write junk
  raf = new RandomAccessFile(logfile, "rw");
  raf.seek(raf.length());
  raf.writeChars("This is other junk in the file");
  raf.close();
  // Write using logWriter
  writeOp(new TransactionId(8, 4));

  logReader.open();
  assertEquals(FileHeader.HEADER_LEN, logReader.position());
  RedoableOp op = logReader.getNextOp();
  assertEquals(FileHeader.HEADER_LEN + 48,
    logReader.getLastOpStartOffset(),
    "Should skip 48 bytes of junk");
  assertEquals(op.getTransactionId(), new TransactionId(7, 3), "TransactionId mismatch");

  op = logReader.getNextOp();
  assertEquals(op.getTransactionId(), new TransactionId(8, 4), "TransactionId mismatch");

  assertNull(logReader.getNextOp(), "More ops in file.");
 }

 @Test
 void readBeforeOpen() throws Exception {
  assertThrows(IOException.class, () -> {
   logReader.getNextOp();
  });
 }

 @Test
 void junkFileFails() throws Exception {
  RandomAccessFile raf = new RandomAccessFile(logfile, "rw");
  // need at least HEADER_LEN worth of junk
  byte[] array = new byte[FileHeader.HEADER_LEN];
  Arrays.fill(array, (byte) 'a');
  raf.writeBytes("This is junk in the file");
  raf.write(array);
  raf.close();
  try {
   logReader.open();
  } catch (IOException e) {
   assertTrue(
     e.getCause().getMessage().contains("Missing magic bytes"),
     "Cause should contain 'missing magic bytes' "
       + "Got: " + e.getCause().getMessage());
   return;
  }
  fail("No exception thrown.");
 }
}
