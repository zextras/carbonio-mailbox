// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.zmime;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import javax.mail.internet.SharedInputStream;

import org.junit.jupiter.api.Test;

import com.zimbra.common.util.ByteUtil;

public class ZSharedFileInputStreamTest {
    private void checkStream(ZSharedFileInputStream is, String expected) throws Exception {
        int length = expected.length();

        assertFalse(is.isBuffered(), "not buffered");
        assertEquals(0L, is.getPosition(), "position is 0");
        assertFalse(is.isBuffered(), "not buffered");
        assertEquals(length, is.available(), "available is " + length);
        assertArrayEquals(expected.getBytes(), ByteUtil.readInput(is, 100, 100), length + " bytes match");
        assertTrue(is.isBuffered(), "buffered");
        assertEquals(length, is.getPosition(), "position is " + length);

        assertEquals(-1, is.read(), "read() eof");
        byte[] buf = new byte[10];
        assertEquals(-1, is.read(buf), "read(byte[]) eof");
        assertEquals(-1, is.read(buf, 5, 2), "read(byte[], int, int) eof");

        is.close();
    }

  @Test
  void stream() throws Exception {
    File file = File.createTempFile("zsfist", ".tmp");
    file.deleteOnExit();
    FileOutputStream fos = new FileOutputStream(file);
    fos.write("0123456789".getBytes());
    fos.close();

    ZSharedFileInputStream is = new ZSharedFileInputStream(file);
    checkStream(is, "0123456789");
    checkStream(is.newStream(0, 10), "0123456789");
    checkStream(is.newStream(0, -1), "0123456789");
    checkStream(is.newStream(1, 9), "12345678");
    checkStream(is.newStream(1, -1), "123456789");

    ZSharedFileInputStream substream = is.newStream(2, 8);
    checkStream(substream, "234567");
    checkStream(substream.newStream(0, 6), "234567");
    checkStream(substream.newStream(0, -1), "234567");
    checkStream(substream.newStream(0, 4), "2345");
    checkStream(substream.newStream(2, 6), "4567");
    checkStream(substream.newStream(2, -1), "4567");
    checkStream(substream.newStream(2, 4), "45");
  }

  @Test
  void bis() throws Exception {
    final String CONTENT = "0123456789";
    File file = createFile(CONTENT);

    ZSharedFileInputStream in = new ZSharedFileInputStream(file);

    // Test reading all content
    String read = getContent(in, 100);
    assertEquals(CONTENT, read);
    checkEof(in);
    in.close();

    // Test reading beginning and end
    in = new ZSharedFileInputStream(file);
    assertEquals("01234", getContent(in, 5));
    assertEquals("56789", getContent(in, 100));
    checkEof(in);
    in.close();

    // Test invalid start/end
    try {
      in = new ZSharedFileInputStream(file, 6L, 5L);
      fail("Test with start=6 and end=5 should not have succeeded.");
    } catch (AssertionError e) {
    }

    // Test skip
    in = new ZSharedFileInputStream(file);
    assertEquals(2, in.skip(2));
    assertEquals("23", getContent(in, 2));
    assertEquals(3, in.skip(3));
    assertEquals("7", getContent(in, 1));
    assertEquals(2, in.skip(1000));
    checkEof(in);
    in.close();

    // Test mark
    in = new ZSharedFileInputStream(file);
    assertTrue(in.markSupported());
    try {
      in.reset();
      fail("reset() should not have succeeded");
    } catch (IOException e) {
    }
    assertEquals("012", getContent(in, 3));
    in.mark(3);
    assertEquals("34", getContent(in, 2));
    in.reset();
    assertEquals("34", getContent(in, 2));
    assertEquals("56", getContent(in, 2));
    try {
      in.reset();
    } catch (IOException e) {
      fail("reset() should not have succeeded");
    }
    in.close();

    // Test reading a byte array with an offset.
    in = new ZSharedFileInputStream(file);
    byte[] buf = new byte[5];
    for (int i = 0;i < 5;i++) {
      buf[i] = 57;
    }
    int numRead = in.read(buf, 3, 2);
    assertTrue(numRead == 1 || numRead == 2, "Unexpected number of bytes read: " + numRead);
    int[] untouchedIndexes = null;
    if (numRead == 1) {
      assertEquals((byte) '0', buf[3]);
      untouchedIndexes = new int[]{0, 1, 2, 4};
    } else if (numRead == 2) {
      assertEquals((byte) '0', buf[3]);
      assertEquals((byte) '1', buf[4]);
      untouchedIndexes = new int[]{0, 1, 2};
    }
    for (int i : untouchedIndexes) {
      assertEquals(57, buf[i], "Unexpected value at index " + i);
    }
    in.close();

    // Test reading into a byte array.
    in = new ZSharedFileInputStream(file);
    in.read();
    in.read();
    numRead = in.read(buf);
    assertTrue(numRead > 0);
    assertTrue(numRead <= 5);
    byte[] test = new byte[numRead];
    System.arraycopy(buf, 0, test, 0, numRead);
    assertTrue("23456".startsWith(new String(test)));
    in.close();

    // Test substream - all content
    InputStream sub = in.newStream(0, CONTENT.length());
    assertEquals(CONTENT, getContent(sub, 100));
    checkEof(sub);
    sub.close();

    // Test substream beginning
    sub = in.newStream(0, 5);
    assertEquals("01234", getContent(sub, 100));
    checkEof(sub);
    sub.close();

    // Test substream end
    sub = in.newStream(5, 10);
    assertEquals("56789", getContent(sub, 100));
    checkEof(sub);
    sub.close();

    sub = in.newStream(5, -1);
    assertEquals("56789", getContent(sub, 100));
    checkEof(sub);
    sub.close();

    // Test substream past EOF
    sub = in.newStream(5, 11);
    assertEquals("56789", getContent(sub, 100));
    checkEof(sub);
    sub.close();

    // Test substream middle
    sub = in.newStream(3, 6);
    assertEquals("345", getContent(sub, 100));
    checkEof(sub);
    sub.close();

    // Test substream position
    sub = in.newStream(3, 6);
    assertEquals(0, ((SharedInputStream) sub).getPosition());
    sub.read(new byte[2]);
    assertEquals(2, ((SharedInputStream) sub).getPosition());
    sub.close();

    // Test sub-substream
    InputStream subsub = ((ZSharedFileInputStream) sub).newStream(1, 3);
    assertEquals("45", getContent(subsub, 100));

    // Test position after reading 1 character
    in.close();
    in = new ZSharedFileInputStream(file);
    assertEquals(0, in.getPosition());
    in.read();
    assertEquals(1, in.getPosition());
    in.close();

    // Test reading byte arrays until the end of the file
    in = new ZSharedFileInputStream(file);
    buf = new byte[4];
    while ((numRead = in.read(buf)) >= 0) {
    }
    in.close();

    file.delete();
  }

  @Test
  void bisLarge() throws Exception {
    File file = createFile(5000);
    String content = ByteUtil.getContent(new FileReader(file), -1, true);

    ZSharedFileInputStream in = new ZSharedFileInputStream(file);
    assertEquals(content, getContent(in, 5000));
    in.close();

    // Test reading 1 char at a time, then a byte array.  This tests
    // the section of ZSharedFileInputStream.read(byte[]), where it reads
    // part of the data from the buffer and part from the file.
    in = new ZSharedFileInputStream(file);
    String firstChunk = getContent(in, 1000);
    assertEquals(content.substring(0, 1000), firstChunk);

    byte[] secondChunk = new byte[2000];
    int numRead = in.read(secondChunk);
    assertTrue(numRead > 0);
    byte[] test = new byte[numRead];
    System.arraycopy(secondChunk, 0, test, 0, numRead);
    assertEquals(content.substring(1000, 1000 + numRead), new String(test));
    int thirdChunkStartPos = 1000 + numRead;

    // Test bug 24715.  Make sure that we don't get IndexOutOfBoundsException
    // when reading another byte[]
    byte[] thirdChunk = new byte[2000];
    numRead = in.read(thirdChunk);
    assertTrue(numRead > 0);
    test = new byte[numRead];
    System.arraycopy(thirdChunk, 0, test, 0, numRead);
    assertEquals(content.substring(thirdChunkStartPos, thirdChunkStartPos + numRead), new String(thirdChunk));

    file.delete();
    in.close();
  }

    private File createFile(int numBytes) throws Exception {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < numBytes; i++) {
            sb.append((char) ('a' + random.nextInt(26)));
        }
        return createFile(sb.toString());
    }

    private File createFile(String data) throws Exception {
        File file = File.createTempFile("TestZSFIS", ".txt");
        FileWriter writer = new FileWriter(file);
        writer.write(data);
        writer.close();
        return file;
    }

    private String getContent(InputStream in, int maxBytes) throws Exception {
        StringBuilder builder = new StringBuilder();
        for (int i = 1; i <= maxBytes; i++) {
            int c = in.read();
            if (c <= 0)
                break;
            builder.append((char) c);
        }
        return builder.toString();
    }

    private void checkEof(InputStream in) throws Exception {
        assertEquals(-1, in.read());
        byte[] buf = new byte[10];
        assertEquals(-1, in.read(buf));
        assertEquals(-1, in.read(buf, 5, 2));
    }
}
