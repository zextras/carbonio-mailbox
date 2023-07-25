// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.cs.redolog.logger;

import java.io.File;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

import java.io.RandomAccessFile;

public class FileHeaderTest {
    private FileHeader hdr;
    private RandomAccessFile raFile;

 @TempDir
 public File folder;

    @BeforeEach
    public void setUp() throws Exception {
        raFile = new RandomAccessFile(File.createTempFile("headerfile.txt", null, folder), "rw");
    }

 @Test
 void uninitializedHeader() throws Exception {
  hdr = new FileHeader();
  assertFalse(hdr.getOpen(), "file is open");
  assertEquals(0, hdr.getFileSize(), "file size is not 0");
  assertEquals(0, hdr.getSequence(), "header sequence is not 0");
  assertEquals("unknown", hdr.getServerId(), "server id is set");
  assertEquals(0, hdr.getFirstOpTstamp(), "unexpected first op time");
  assertEquals(0, hdr.getLastOpTstamp(), "unexpected last op time");
  assertEquals(0, hdr.getCreateTime(), "unexpected create time");

  hdr.write(raFile);
  FileHeader fromFile = new FileHeader("should be overwritten");
  fromFile.read(raFile);
  assertEquals(hdr, fromFile, "header from file should match serialized data");
 }

 @Test
 void setAllFields() throws Exception {
  hdr = new FileHeader("serverId");
  hdr.setOpen(true);
  hdr.setFileSize(1);
  hdr.setSequence(2);
  hdr.setFirstOpTstamp(3);
  hdr.setLastOpTstamp(4);
  hdr.setCreateTime(5);

  assertTrue(hdr.getOpen(), "open != true");
  assertEquals(1, hdr.getFileSize());
  assertEquals(2, hdr.getSequence());
  assertEquals("serverId", hdr.getServerId());
  assertEquals(3, hdr.getFirstOpTstamp());
  assertEquals(4, hdr.getLastOpTstamp());
  assertEquals(5, hdr.getCreateTime());

  hdr.write(raFile);
  FileHeader fromFile = new FileHeader("should be overwritten");
  fromFile.read(raFile);
  assertEquals(hdr, fromFile, "header from file should match serialized data");
 }

 @Test
 void junkFile() throws Exception {
  assertThrows(IOException.class, () -> {
   raFile.write("this is not a valid header".getBytes());
   hdr = new FileHeader();
   hdr.read(raFile);
  });
 }

 @Test
 void versionTooHigh() throws Exception {
  hdr = new FileHeader();
  hdr.write(raFile);
  // Fake up a bad version
  final int versionLocation =
    7 /* magic */ + 1 /* open */ + 8 /* file size */ +
      8 /* sequence */ + 1 /* serverid length */ + 127 /* serverid */ +
      8 /* firstOpTstamp */ + 8 /* lastOpTstamp */;
  raFile.seek(versionLocation);
  // Read the major version and add 1 to get something invalid.
  // Version has no public methods to accomplish this.
  short majorVersion = raFile.readShort();
  raFile.seek(versionLocation);
  raFile.writeShort(majorVersion + 1);
  try {
   hdr.read(raFile);
  } catch (IOException e) {
   assertTrue(e.getMessage().contains(
       "is higher than the highest known version"),
     "Version in file should be too high.");
   return;
  }
  fail("no exception thrown");
 }
}
