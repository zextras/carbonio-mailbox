// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.store;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.zimbra.common.util.FileCache;
import com.zimbra.cs.mime.ParsedMessage;
import com.zimbra.cs.store.file.BlobConsistencyChecker;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import java.util.zip.GZIPOutputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.io.TempDir;

@EnabledOnOs(OS.LINUX)
class CompressedBlobStreamTest {

  @TempDir Path directory;
  private Path invalidGzip;

  @BeforeEach
  void createInvalidGzip() throws IOException {
    invalidGzip = Files.write(directory.resolve("invalid.msg"),
        new byte[] {0x1f, (byte) 0x8b, 0, 0, 0, 0, 0, 0, 0, 0});
  }

  @Test
  void closesBlobStreamWhenGzipCannotBeOpened() throws Exception {
    Blob blob = new Blob(invalidGzip.toFile()).setCompressed(true);

    assertClosedAfterFailure(blob::getInputStream);
  }

  @Test
  void closesCacheInputWhenGzipCannotBeOpened() throws Exception {
    FileCache<String> cache = FileCache.Builder.createWithStringKey(directory.toFile(), false).build();
    FileDescriptorCache descriptors = new FileDescriptorCache(cache);

    assertClosedAfterFailure(() ->
        descriptors.read(invalidGzip.toString(), 100, 0, new byte[1], 0, 1));
    assertEquals(0, descriptors.getSize());
  }

  @Test
  void closesConsistencyCheckerInputWhenGzipCannotBeOpened() throws Exception {
    TestConsistencyChecker checker = new TestConsistencyChecker();

    assertClosedAfterFailure(() -> checker.size(invalidGzip.toFile(), 100));
  }

  @Test
  void closesParsedMessageInputWhenGzipCannotBeOpened() throws Exception {
    assertClosedAfterFailure(() -> new ParsedMessage(invalidGzip.toFile(), null, false));
  }

  @Test
  void returnsReadableBlobStream() throws Exception {
    byte[] content = "A compressed mailbox message".getBytes(StandardCharsets.UTF_8);
    Path gzip = writeGzip(content);

    try (InputStream in = new Blob(gzip.toFile()).setCompressed(true).getInputStream()) {
      assertArrayEquals(content, in.readAllBytes());
    }
    assertEquals(0, openDescriptors(gzip));
  }

  @Test
  void measuresValidCompressedBlob() throws Exception {
    byte[] content = "A compressed mailbox message".getBytes(StandardCharsets.UTF_8);
    Path gzip = writeGzip(content);

    assertEquals(content.length, new TestConsistencyChecker().size(gzip.toFile(), content.length));
    assertEquals(0, openDescriptors(gzip));
  }

  private Path writeGzip(byte[] content) throws IOException {
    Path gzip = directory.resolve("valid.msg");
    try (GZIPOutputStream out = new GZIPOutputStream(Files.newOutputStream(gzip))) {
      out.write(content);
    }
    return gzip;
  }

  private void assertClosedAfterFailure(Executable operation) throws Exception {
    assertThrows(IOException.class, operation);
    assertEquals(0, openDescriptors(invalidGzip));
  }

  private long openDescriptors(Path file) throws IOException {
    try (Stream<Path> descriptors = Files.list(Path.of("/proc/self/fd"))) {
      return descriptors.filter(descriptor -> pointsTo(descriptor, file)).count();
    }
  }

  private boolean pointsTo(Path descriptor, Path file) {
    try {
      return Files.isSameFile(descriptor, file);
    } catch (IOException e) {
      return false;
    }
  }

  private static class TestConsistencyChecker extends BlobConsistencyChecker {
    long size(File file, long expected) throws IOException {
      return getDataSize(file, expected);
    }
  }
}
