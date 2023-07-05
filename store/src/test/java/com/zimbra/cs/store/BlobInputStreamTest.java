// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.store;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class BlobInputStreamTest {
    @BeforeEach
    public void startUp() {
        BlobInputStream.setFileDescriptorCache(new FileDescriptorCache(null));
    }

    @AfterEach
    public void tearDown() {
        BlobInputStream.setFileDescriptorCache(null);
    }

    private static final byte[] CONTENT = "0123456789".getBytes();

    private File createTempFile() throws IOException {
        File file = File.createTempFile(BlobInputStreamTest.class.getSimpleName(), ".msg");
        file.deleteOnExit();
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(CONTENT);
        fos.close();

        return file;
    }

 @Test
 void deleteHalfway() throws Exception {
  // set up file and build base stream
  File file = createTempFile();
  BlobInputStream bis = new BlobInputStream(file, file.length());

  // make sure you can read from it (this also puts it in the FD cache)
  assertEquals(CONTENT.length, bis.read(new byte[100]), "can read 10 bytes");

  // get a full-length substream
  InputStream copy = bis.newStream(0, CONTENT.length);
  assertNotNull(copy, "can create substream before delete");
  assertEquals(CONTENT.length, copy.read(new byte[100]), "can read 10 bytes from full substream");
  try (InputStream badStartIs = bis.newStream(-1, CONTENT.length)) {
   fail("Shouldn't be able to create newStream with start < 0");
  } catch (IllegalArgumentException iae) {
   assertTrue(iae.getMessage().contains(file.getAbsolutePath()), "Blob name included in exception message");
  }

  // rely on the FD cache to keep it readable through a file delete
  file.delete();
  assertFalse(file.exists(), "file is gone");
  InputStream substream = bis.newStream(2, 8);
  assertNotNull(substream, "can create substream after delete");
  assertEquals(6, substream.read(new byte[100]), "can read 6 bytes from substream after delete");

  try (InputStream badStartIs = bis.newStream(-1, CONTENT.length)) {
   fail("Shouldn't be able to create newStream with start < 0 - especially after delete");
  } catch (IllegalArgumentException iae) {
   assertTrue(iae.getMessage().contains(file.getAbsolutePath()), "Blob name included in exception message");
  }

  // set up file again
  file = createTempFile();
  bis = new BlobInputStream(file, file.length());

  // new file is not in FD cache, so it shouldn't be readable after a file delete
  file.delete();
  assertFalse(file.exists(), "file is gone");
  assertNull(bis.newStream(0, CONTENT.length), "can't create substream after delete");
 }
}
