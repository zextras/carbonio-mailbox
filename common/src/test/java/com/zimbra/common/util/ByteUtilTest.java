// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.zip.ZipException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.zimbra.common.util.ByteUtil.PositionInputStream;
import com.zimbra.common.util.ByteUtil.SegmentInputStream;
import com.zimbra.common.util.ByteUtil.TeeOutputStream;

class ByteUtilTest {

  private static final byte[] HELLO = bytes("hello world");

  @Test
  void putAndGetFileContent(@TempDir Path tempDir) throws IOException {
    String path = tempDir.resolve("data.bin").toString();
    ByteUtil.putContent(path, HELLO);
    assertArrayEquals(HELLO, ByteUtil.getContent(new java.io.File(path)));
  }

  @Test
  void countBytesClosesStreamByDefault() throws IOException {
    TrackingInputStream in = new TrackingInputStream(HELLO);
    assertEquals(HELLO.length, ByteUtil.countBytes(in));
    assertTrue(in.closed);
  }

  @Test
  void countBytesCanLeaveStreamOpen() throws IOException {
    TrackingInputStream in = new TrackingInputStream(HELLO);
    assertEquals(HELLO.length, ByteUtil.countBytes(in, false));
    assertFalse(in.closed);
  }

  @Test
  void drainReadsToEnd() throws IOException {
    TrackingInputStream in = new TrackingInputStream(HELLO);
    assertSame(in, ByteUtil.drain(in));
    assertEquals(-1, in.read());
    assertTrue(in.closed);

    TrackingInputStream open = new TrackingInputStream(HELLO);
    ByteUtil.drain(open, false);
    assertEquals(0, open.available());
    assertFalse(open.closed);
  }

  @Test
  void getContentReadsWholeStreamAndCloses() throws IOException {
    TrackingInputStream in = new TrackingInputStream(HELLO);
    assertArrayEquals(HELLO, ByteUtil.getContent(in, 4));
    assertTrue(in.closed);
  }

  @Test
  void getContentCanLeaveStreamOpen() throws IOException {
    TrackingInputStream in = new TrackingInputStream(HELLO);
    assertArrayEquals(HELLO, ByteUtil.getContent(in, -1, false));
    assertFalse(in.closed);
  }

  @Test
  void getContentWithinSizeLimit() throws IOException {
    assertArrayEquals(HELLO, ByteUtil.getContent(new ByteArrayInputStream(HELLO), -1, 100L));
    assertArrayEquals(HELLO, ByteUtil.getContent(new ByteArrayInputStream(HELLO), -1, Long.MAX_VALUE));
  }

  @Test
  void getContentOverSizeLimitThrows() {
    assertThrows(IOException.class, () -> ByteUtil.getContent(new ByteArrayInputStream(HELLO), -1, 3L));
  }

  @Test
  void getPartialContent() throws IOException {
    assertArrayEquals(new byte[0], ByteUtil.getPartialContent(new ByteArrayInputStream(HELLO), 0, -1));
    assertArrayEquals(HELLO, ByteUtil.getPartialContent(new ByteArrayInputStream(HELLO), -1, -1));
  }

  @Test
  void getContentFromReader() throws IOException {
    assertEquals("", ByteUtil.getContent((Reader) null, -1, true));
    assertEquals("", ByteUtil.getContent(new StringReader("abc"), 0, true));
    assertEquals("abc", ByteUtil.getContent(new StringReader("abc"), -1, true));
    assertEquals("ab", ByteUtil.getContent(new StringReader("abc"), 2, true));
    assertEquals("x".repeat(3000), ByteUtil.getContent(new StringReader("x".repeat(3000)), 5000, false));
  }

  @Test
  void getContentFromReaderIgnoresCloseFailure() throws IOException {
    Reader failingReader = new Reader() {
      private final Reader delegate = new StringReader("abc");

      @Override
      public int read(char[] buf, int off, int len) throws IOException {
        return delegate.read(buf, off, len);
      }

      @Override
      public void close() throws IOException {
        throw new IOException("cannot close");
      }
    };
    assertEquals("abc", ByteUtil.getContent(failingReader, -1, true));
  }

  @Test
  void closeInputStream() {
    TrackingInputStream in = new TrackingInputStream(HELLO);
    ByteUtil.closeStream(in);
    assertTrue(in.closed);
    assertDoesNotThrow(() -> ByteUtil.closeStream((InputStream) null));
    assertDoesNotThrow(() -> ByteUtil.closeStream(new FailingCloseInputStream()));
  }

  @Test
  void closeInputStreamDrainsPipedStream() throws IOException {
    PipedInputStream piped = new PipedInputStream();
    try (PipedOutputStream writer = new PipedOutputStream(piped)) {
      writer.write(HELLO);
    }
    ByteUtil.closeStream(piped);
    assertThrows(IOException.class, piped::read);
  }

  @Test
  void closeInputStreamIgnoresDrainFailureOfUnconnectedPipe() {
    assertDoesNotThrow(() -> ByteUtil.closeStream(new PipedInputStream()));
  }

  @Test
  void closeOutputStream() {
    TrackingOutputStream out = new TrackingOutputStream();
    ByteUtil.closeStream(out);
    assertTrue(out.closed);
    assertDoesNotThrow(() -> ByteUtil.closeStream((OutputStream) null));
    assertDoesNotThrow(() -> ByteUtil.closeStream(new FailingCloseOutputStream()));
  }

  @Test
  void closeWriter() {
    assertDoesNotThrow(() -> ByteUtil.closeWriter(null));
    assertDoesNotThrow(() -> ByteUtil.closeWriter(new StringWriter()));
    assertDoesNotThrow(() -> ByteUtil.closeWriter(new FailingCloseWriter()));
  }

  @Test
  void indexOf() {
    byte[] source = bytes("abcabd");
    assertEquals(3, ByteUtil.indexOf(source, 0, bytes("abd")));
    assertEquals(3, ByteUtil.indexOf(source, 1, bytes("ab")));
    assertEquals(-1, ByteUtil.indexOf(source, 0, bytes("abx")));
    assertEquals(0, ByteUtil.indexOf(source, 0, bytes("a")));
  }

  @Test
  void isASCII() {
    assertFalse(ByteUtil.isASCII(null));
    assertTrue(ByteUtil.isASCII(bytes("text\t\r\n~")));
    assertFalse(ByteUtil.isASCII(new byte[] {0x01}));
    assertFalse(ByteUtil.isASCII(new byte[] {0x7F}));
    assertFalse(ByteUtil.isASCII(new byte[] {(byte) 0xC3}));
  }

  @Test
  void compressAndUncompressRoundTrip() throws IOException {
    byte[] compressed = ByteUtil.compress(HELLO);
    assertTrue(ByteUtil.isGzipped(compressed));
    assertArrayEquals(HELLO, ByteUtil.uncompress(compressed));
  }

  @Test
  void uncompressRejectsNonGzipData() {
    assertThrows(ZipException.class, () -> ByteUtil.uncompress(HELLO));
  }

  @Test
  void isGzippedBytes() {
    assertFalse(ByteUtil.isGzipped((byte[]) null));
    assertFalse(ByteUtil.isGzipped(new byte[] {0x1f}));
    assertFalse(ByteUtil.isGzipped(HELLO));
  }

  @Test
  void isGzippedStreamResetsPosition() throws IOException {
    InputStream gzipped = new BufferedInputStream(new ByteArrayInputStream(ByteUtil.compress(HELLO)));
    assertTrue(ByteUtil.isGzipped(gzipped));
    assertEquals(0x1f, gzipped.read());

    InputStream plain = new ByteArrayInputStream(HELLO);
    assertFalse(ByteUtil.isGzipped(plain));
    assertEquals('h', plain.read());
  }

  @Test
  void getDataLengthClosesStream() throws IOException {
    TrackingInputStream in = new TrackingInputStream(HELLO);
    assertEquals(HELLO.length, ByteUtil.getDataLength(in));
    assertTrue(in.closed);
  }

  @Test
  void fsSafeBase64ReplacesSlash() {
    byte[] data = {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
    String encoded = ByteUtil.encodeFSSafeBase64(data);
    assertEquals(",,,,", encoded);
    assertArrayEquals(data, ByteUtil.decodeFSSafeBase64(encoded));
    assertArrayEquals(HELLO, ByteUtil.decodeFSSafeBase64(ByteUtil.encodeFSSafeBase64(HELLO)));
  }

  @Test
  void ldapBase64RoundTrip() {
    String encoded = ByteUtil.encodeLDAPBase64(HELLO);
    assertEquals(Base64.getEncoder().encodeToString(HELLO), encoded);
    assertArrayEquals(HELLO, ByteUtil.decodeLDAPBase64(encoded));
  }

  @Test
  void sha1Digest() throws IOException {
    String expectedHex = "a9993e364706816aba3e25717850c26c9cd0d89d";
    assertEquals(expectedHex, ByteUtil.getSHA1Digest(bytes("abc"), false));
    assertEquals(expectedHex, ByteUtil.getSHA1Digest(new ByteArrayInputStream(bytes("abc")), false));
    assertEquals(fsSafeBase64Digest("SHA-1", "abc"), ByteUtil.getSHA1Digest(bytes("abc"), true));
    assertEquals(
        fsSafeBase64Digest("SHA-1", "abc"), ByteUtil.getSHA1Digest(new ByteArrayInputStream(bytes("abc")), true));
  }

  @Test
  void sha256Digest() throws IOException {
    String expectedHex = "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad";
    assertEquals(expectedHex, ByteUtil.getSHA256Digest(bytes("abc"), false));
    assertEquals(expectedHex, ByteUtil.getSHA256Digest(new ByteArrayInputStream(bytes("abc")), false));
    assertEquals(fsSafeBase64Digest("SHA-256", "abc"), ByteUtil.getSHA256Digest(bytes("abc"), true));
    assertEquals(fsSafeBase64Digest("SHA-256", "abc"), ByteUtil.getDigest(bytes("abc")));
  }

  @Test
  void digestWithUnknownAlgorithmThrows() {
    assertThrows(RuntimeException.class, () -> ByteUtil.getDigest("NO-SUCH-ALGO", HELLO, false));
    TrackingInputStream in = new TrackingInputStream(HELLO);
    assertThrows(RuntimeException.class, () -> ByteUtil.getDigest("NO-SUCH-ALGO", in, false));
    assertTrue(in.closed);
  }

  @Test
  void skip() throws IOException {
    InputStream in = new ByteArrayInputStream(HELLO);
    assertEquals(0, ByteUtil.skip(in, 0));
    assertEquals(6, ByteUtil.skip(in, 6));
    assertEquals('w', in.read());
    assertEquals(4, ByteUtil.skip(in, 100));
  }

  @Test
  void skipLargerThanBuffer() throws IOException {
    InputStream in = new ByteArrayInputStream(new byte[10_000]);
    assertEquals(9_000, ByteUtil.skip(in, 9_000));
  }

  @Test
  void copyClosesStreamsWhenAsked() throws IOException {
    TrackingInputStream in = new TrackingInputStream(HELLO);
    TrackingOutputStream out = new TrackingOutputStream();
    assertEquals(HELLO.length, ByteUtil.copy(in, true, out, true));
    assertArrayEquals(HELLO, out.toByteArray());
    assertTrue(in.closed);
    assertTrue(out.closed);
  }

  @Test
  void copyLeavesStreamsOpen() throws IOException {
    TrackingInputStream in = new TrackingInputStream(HELLO);
    TrackingOutputStream out = new TrackingOutputStream();
    ByteUtil.copy(in, false, out, false);
    assertFalse(in.closed);
    assertFalse(out.closed);
  }

  @Test
  void copyWithMaxLength() throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    assertEquals(5, ByteUtil.copy(new ByteArrayInputStream(HELLO), true, out, false, 5));
    assertArrayEquals(bytes("hello"), out.toByteArray());
    assertEquals(0, ByteUtil.copy(new ByteArrayInputStream(HELLO), true, new ByteArrayOutputStream(), false, 0));
    assertEquals(HELLO.length, ByteUtil.copy(new ByteArrayInputStream(HELLO), true, new ByteArrayOutputStream(), false, 100));
    assertEquals(
        10_000, ByteUtil.copy(new ByteArrayInputStream(new byte[20_000]), true, new ByteArrayOutputStream(), false, 10_000));
  }

  @Test
  void readInputWithNonPositiveLimit() throws IOException {
    assertArrayEquals(new byte[0], ByteUtil.readInput(new ByteArrayInputStream(HELLO), 10, 0));
  }

  @Test
  void readInputWithoutSizeHint() throws IOException {
    assertArrayEquals(new byte[0], ByteUtil.readInput(new ByteArrayInputStream(new byte[0]), 0, 10));
    assertArrayEquals(HELLO, ByteUtil.readInput(new ByteArrayInputStream(HELLO), 0, 100));
  }

  @Test
  void readInputStopsAtLimit() throws IOException {
    byte[] data = new byte[2_000];
    assertEquals(1_500, ByteUtil.readInput(new ByteArrayInputStream(data), 0, 1_500).length);
    assertArrayEquals(bytes("hello"), ByteUtil.readInput(new ByteArrayInputStream(HELLO), 20, 5));
  }

  @Test
  void readInputWithTooLargeSizeHint() throws IOException {
    assertArrayEquals(HELLO, ByteUtil.readInput(new ByteArrayInputStream(HELLO), 50, 100));
  }

  @Test
  void readInputWithExactSizeHint() throws IOException {
    assertArrayEquals(HELLO, ByteUtil.readInput(new ByteArrayInputStream(HELLO), HELLO.length, 100));
  }

  @Test
  void readInputWithTooSmallSizeHint() throws IOException {
    assertArrayEquals(HELLO, ByteUtil.readInput(new ByteArrayInputStream(HELLO), 2, 100));
  }

  @Test
  void utf8RoundTrip() throws IOException {
    for (String value : new String[] {null, "", "héllo"}) {
      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      ByteUtil.writeUTF8(new DataOutputStream(bytes), value);
      assertEquals(value, ByteUtil.readUTF8(dataInputOf(bytes.toByteArray())));
    }
  }

  @Test
  void writeUTF8RejectsTooLongString() {
    String tooLong = "a".repeat(32 * 1024 * 1024 + 1);
    assertThrows(
        IOException.class, () -> ByteUtil.writeUTF8(new DataOutputStream(OutputStream.nullOutputStream()), tooLong));
  }

  @Test
  void readUTF8RejectsInvalidLengths() throws IOException {
    assertThrows(IOException.class, () -> ByteUtil.readUTF8(dataInputOfLength(32 * 1024 * 1024 + 1)));
    assertThrows(IOException.class, () -> ByteUtil.readUTF8(dataInputOfLength(-2)));
  }

  @Test
  void teeOutputStreamWritesToBothStreams() throws IOException {
    ByteArrayOutputStream first = new ByteArrayOutputStream();
    ByteArrayOutputStream second = new ByteArrayOutputStream();
    try (TeeOutputStream tee = new TeeOutputStream(first, second)) {
      tee.write('>');
      tee.write(HELLO, 0, 5);
      tee.flush();
    }
    assertEquals(">hello", first.toString(StandardCharsets.UTF_8));
    assertEquals(">hello", second.toString(StandardCharsets.UTF_8));
  }

  @Test
  void teeOutputStreamWritesOnceWhenBothStreamsAreTheSame() throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    TeeOutputStream tee = new TeeOutputStream(out, out);
    tee.write('a');
    tee.write(HELLO, 0, 1);
    tee.flush();
    assertEquals("ah", out.toString(StandardCharsets.UTF_8));
  }

  @Test
  void teeOutputStreamSkipsNullFirstStream() throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    TeeOutputStream tee = new TeeOutputStream(null, out);
    tee.write('a');
    tee.write(HELLO, 0, 1);
    tee.flush();
    assertEquals("ah", out.toString(StandardCharsets.UTF_8));
  }

  @Test
  void positionInputStreamTracksPosition() throws IOException {
    TrackingInputStream source = new TrackingInputStream(HELLO);
    PositionInputStream in = new PositionInputStream(source);

    in.read();
    in.read(new byte[3], 0, 3);
    assertEquals(4, in.getPosition());

    in.mark(100);
    assertEquals(2, in.skip(2));
    assertEquals(6, in.getPosition());
    in.reset();
    assertEquals(4, in.getPosition());

    in.skip(100);
    assertEquals(-1, in.read());
    assertEquals(-1, in.read(new byte[3], 0, 3));
    assertEquals(HELLO.length, in.getPosition());

    in.close();
    assertTrue(source.closed);
  }

  @Test
  void segmentInputStreamReadsOnlyTheSegment() throws IOException {
    SegmentInputStream segment = SegmentInputStream.create(new ByteArrayInputStream(HELLO), 6, 9);

    assertEquals(3, segment.available());
    assertEquals('w', segment.read());
    byte[] buf = new byte[10];
    assertEquals(2, segment.read(buf, 0, buf.length));
    assertEquals("or", new String(buf, 0, 2, StandardCharsets.UTF_8));
    assertEquals(-1, segment.read());
    assertEquals(-1, segment.read(buf, 0, buf.length));
    assertEquals(0, segment.skip(10));
  }

  @Test
  void segmentInputStreamSkipIsBoundedBySegment() throws IOException {
    SegmentInputStream segment = SegmentInputStream.create(new ByteArrayInputStream(HELLO), 0, 5);
    assertEquals(5, segment.skip(100));
    assertEquals(0, segment.skip(-1));
    assertEquals(-1, segment.read());
  }

  @Test
  void segmentInputStreamWithEndBeforeStartIsEmpty() throws IOException {
    SegmentInputStream segment = SegmentInputStream.create(new ByteArrayInputStream(HELLO), 3, 1);
    assertEquals(0, segment.available());
    assertEquals(-1, segment.read());
  }

  @Test
  void segmentInputStreamRejectsStartBeyondEnd() {
    assertThrows(
        StartOutOfBoundsException.class,
        () -> SegmentInputStream.create(new ByteArrayInputStream(HELLO), 100, 200));
  }

  private static byte[] bytes(String s) {
    return s.getBytes(StandardCharsets.UTF_8);
  }

  private static String fsSafeBase64Digest(String algorithm, String data) {
    try {
      byte[] digest = MessageDigest.getInstance(algorithm).digest(bytes(data));
      return Base64.getEncoder().encodeToString(digest).replace('/', ',');
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  private static DataInputStream dataInputOf(byte[] data) {
    return new DataInputStream(new ByteArrayInputStream(data));
  }

  private static DataInputStream dataInputOfLength(int length) throws IOException {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    new DataOutputStream(bytes).writeInt(length);
    return dataInputOf(bytes.toByteArray());
  }

  private static class TrackingInputStream extends ByteArrayInputStream {
    boolean closed;

    TrackingInputStream(byte[] data) {
      super(data);
    }

    @Override
    public void close() throws IOException {
      closed = true;
      super.close();
    }
  }

  private static class TrackingOutputStream extends ByteArrayOutputStream {
    boolean closed;

    @Override
    public void close() throws IOException {
      closed = true;
      super.close();
    }
  }

  private static class FailingCloseInputStream extends InputStream {
    @Override
    public int read() {
      return -1;
    }

    @Override
    public void close() throws IOException {
      throw new IOException("cannot close");
    }
  }

  private static class FailingCloseOutputStream extends OutputStream {
    @Override
    public void write(int b) {}

    @Override
    public void close() throws IOException {
      throw new IOException("cannot close");
    }
  }

  private static class FailingCloseWriter extends Writer {
    @Override
    public void write(char[] buf, int off, int len) {}

    @Override
    public void flush() {}

    @Override
    public void close() throws IOException {
      throw new IOException("cannot close");
    }
  }
}
