// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.util;

import static org.junit.jupiter.api.Assertions.*;

import com.google.common.collect.Lists;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;
import java.util.zip.GZIPInputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileUtilTest {

  @TempDir private File tempDir;

  @Test
  void sortByMtime() throws InterruptedException {
    List<File> files = Lists.newArrayList();
    File temp1 = new VirtualFile("temp1");
    files.add(temp1);
    Thread.sleep(50);

    File temp2 = new VirtualFile("temp2");
    files.add(temp2);

    assertTrue(temp1.lastModified() != temp2.lastModified());

    FileUtil.sortFilesByModifiedTime(files, true);
    assertEquals(temp2, files.get(0));
    FileUtil.sortFilesByModifiedTime(files);
    assertEquals(temp1, files.get(0));
  }

  @Test
  void deleteDir() throws IOException {
    populateTempDir();
    FileUtil.deleteDir(tempDir);
    assertFalse(tempDir.exists());
  }

  @Test
  void deleteDirContents() throws IOException {
    populateTempDir();
    FileUtil.deleteDirContents(tempDir);
    assertTrue(tempDir.exists());
    assertEquals(0, Objects.requireNonNull(tempDir.listFiles()).length);
  }

  private void populateTempDir() throws IOException {
    Files.createFile(new File(tempDir, "FileUtilTest").toPath());
    File childDir = new File(tempDir, "child");
    assertTrue(childDir.mkdir());
    Files.createFile(new File(childDir, "FileUtilTest").toPath());
  }

  @Test
  void copyFileSuccessfully() throws IOException {
    File srcFile = new File(tempDir, "source.txt");
    File destFile = new File(tempDir, "destination.txt");
    Files.writeString(srcFile.toPath(), "Sample content");

    FileUtil.copy(srcFile, destFile);

    assertTrue(destFile.exists());
    assertEquals("Sample content", Files.readString(destFile.toPath()));
  }

  @Test
  void copyFileFailsWhenSourceDoesNotExist() {
    File srcFile = new File(tempDir, "nonexistent.txt");
    File destFile = new File(tempDir, "destination.txt");

    IOException exception = assertThrows(IOException.class, () -> FileUtil.copy(srcFile, destFile));
    assertTrue(exception.getMessage().contains("nonexistent.txt"));
  }

  @Test
  void deleteFileSuccessfully() throws IOException {
    File file = new File(tempDir, "fileToDelete.txt");
    Files.createFile(file.toPath());

    FileUtil.delete(file);

    assertFalse(file.exists());
  }

  @Test
  void listFilesRecursivelyReturnsAllFiles() throws IOException {
    File subDir = new File(tempDir, "subDir");
    assertTrue(subDir.mkdir());
    File file1 = new File(tempDir, "file1.txt");
    File file2 = new File(subDir, "file2.txt");
    Files.createFile(file1.toPath());
    Files.createFile(file2.toPath());

    List<File> files = FileUtil.listFilesRecursively(tempDir);

    assertEquals(2, files.size());
    assertTrue(files.contains(file1));
    assertTrue(files.contains(file2));
  }

  @Test
  void getExtensionReturnsCorrectExtension() {
    assertEquals("txt", FileUtil.getExtension("file.txt"));
    assertEquals("", FileUtil.getExtension("file."));
    assertEquals("file", FileUtil.getExtension("file"));
    assertNull(FileUtil.getExtension(null));
  }

  @Test
  void trimFilenameReturnsCorrectFilename() {
    assertEquals("file.txt", FileUtil.trimFilename("/path/to/file.txt"));
    assertEquals("file.txt", FileUtil.trimFilename("C:\\path\\to\\file.txt"));
    assertNull(FileUtil.trimFilename("/path/to/"));
    assertNull(FileUtil.trimFilename(null));
  }

  @Test
  void copyByPath() throws IOException {
    File src = writeFile("src.txt", "by path");
    File dest = new File(tempDir, "dest.txt");

    FileUtil.copy(src.getPath(), dest.getPath());

    assertEquals("by path", Files.readString(dest.toPath()));
  }

  @Test
  void copyWithSyncAndEmptyFile() throws IOException {
    File src = writeFile("src.txt", "synced");
    File emptySrc = writeFile("empty.txt", "");

    FileUtil.copy(src, new File(tempDir, "dest.txt"), true);
    FileUtil.copy(emptySrc, new File(tempDir, "emptyDest.txt"), false);

    assertEquals("synced", Files.readString(new File(tempDir, "dest.txt").toPath()));
    assertEquals(0, new File(tempDir, "emptyDest.txt").length());
  }

  @Test
  void copyFailsWhenDestinationDirectoryIsMissing() throws IOException {
    File src = writeFile("src.txt", "content");
    File dest = new File(tempDir, "missing/dest.txt");

    assertThrows(IOException.class, () -> FileUtil.copy(src, dest));
    assertFalse(dest.exists());
  }

  @Test
  void compressAndUncompressRoundTrip() throws IOException {
    File src = writeFile("plain.txt", "compress me");
    File compressed = new File(tempDir, "plain.txt.gz");
    File uncompressed = new File(tempDir, "plain.out");

    FileUtil.compress(src, compressed, false);
    assertTrue(FileUtil.isGzipped(compressed));
    assertFalse(FileUtil.isGzipped(src));

    FileUtil.uncompress(compressed, uncompressed, false);
    assertEquals("compress me", Files.readString(uncompressed.toPath()));
  }

  @Test
  void compressWithSync() throws IOException {
    File src = writeFile("plain.txt", "synced");
    File compressed = new File(tempDir, "plain.txt.gz");

    FileUtil.compress(src, compressed, true);

    try (InputStream in = new GZIPInputStream(new FileInputStream(compressed))) {
      assertEquals("synced", new String(in.readAllBytes(), StandardCharsets.UTF_8));
    }
  }

  @Test
  void compressFailsWhenSourceIsMissing() {
    File dest = new File(tempDir, "out.gz");

    assertThrows(IOException.class, () -> FileUtil.compress(new File(tempDir, "missing"), dest, false));
    assertFalse(dest.exists());
  }

  @Test
  void compressFailsWhenDestinationDirectoryIsMissing() throws IOException {
    File src = writeFile("plain.txt", "content");
    File dest = new File(tempDir, "missing/out.gz");

    assertThrows(IOException.class, () -> FileUtil.compress(src, dest, false));
    assertFalse(dest.exists());
  }

  @Test
  void uncompressStreamFailsWhenDestinationDirectoryIsMissing() {
    File dest = new File(tempDir, "missing/out");

    assertThrows(
        IOException.class,
        () -> FileUtil.uncompress(new ByteArrayInputStream(new byte[0]), dest, false));
    assertFalse(dest.exists());
  }

  @Test
  void copyOIOOverloads() throws IOException {
    File src = writeFile("src.txt", "oio");

    FileUtil.copyOIO(src, new File(tempDir, "a"));
    FileUtil.copyOIO(src, new File(tempDir, "b"), true);
    FileUtil.copyOIO(src, new File(tempDir, "c"), new byte[2]);
    FileUtil.copyOIO(src, new File(tempDir, "d"), new byte[2], true);

    for (String name : List.of("a", "b", "c", "d")) {
      assertEquals("oio", Files.readString(new File(tempDir, name).toPath()));
    }
  }

  @Test
  void copyOIOFailsWhenSourceIsMissing() {
    File dest = new File(tempDir, "dest");

    assertThrows(IOException.class, () -> FileUtil.copyOIO(new File(tempDir, "missing"), dest));
    assertFalse(dest.exists());
  }

  @Test
  void copyStreamToFileClosesStreamWhenAsked() throws IOException {
    TrackingInputStream in = new TrackingInputStream("stream");
    File dest = new File(tempDir, "dest");

    FileUtil.copy(in, true, dest);

    assertEquals("stream", Files.readString(dest.toPath()));
    assertTrue(in.closed);
  }

  @Test
  void copyStreamToFileLeavesStreamOpen() throws IOException {
    TrackingInputStream in = new TrackingInputStream("stream");

    FileUtil.copy(in, false, new File(tempDir, "dest"));

    assertFalse(in.closed);
  }

  @Test
  void copyStreamToFileFailsWhenDestinationDirectoryIsMissing() {
    TrackingInputStream in = new TrackingInputStream("stream");
    File dest = new File(tempDir, "missing/dest");

    assertThrows(IOException.class, () -> FileUtil.copy(in, true, dest));
    assertTrue(in.closed);
    assertFalse(dest.exists());
  }

  @Test
  void copyDirectoryCopiesNestedFiles() throws IOException {
    File src = new File(tempDir, "src");
    File nested = new File(src, "nested");
    assertTrue(nested.mkdirs());
    Files.writeString(new File(src, "top.txt").toPath(), "top");
    Files.writeString(new File(nested, "deep.txt").toPath(), "deep");
    File dest = new File(tempDir, "dest");

    assertEquals(2, FileUtil.copyDirectory(src, dest));

    assertEquals("top", Files.readString(new File(dest, "top.txt").toPath()));
    assertEquals("deep", Files.readString(new File(dest, "nested/deep.txt").toPath()));
  }

  @Test
  void copyDirectoryOfMissingSourceCopiesNothing() throws IOException {
    assertEquals(0, FileUtil.copyDirectory(new File(tempDir, "missing"), new File(tempDir, "dest")));
  }

  @Test
  void listFilesRecursivelyOfMissingOrEmptyDirectory() {
    assertEquals(List.of(), FileUtil.listFilesRecursively(new File(tempDir, "missing")));
    assertEquals(List.of(), FileUtil.listFilesRecursively(tempDir));
  }

  @Test
  void ensureDirExistsCreatesNestedDirectories() throws IOException {
    File nested = new File(tempDir, "a/b/c");

    FileUtil.ensureDirExists(nested.getPath());
    FileUtil.ensureDirExists(nested);

    assertTrue(nested.isDirectory());
  }

  @Test
  void ensureDirExistsFailsUnderRegularFile() throws IOException {
    File file = writeFile("file", "");

    assertThrows(IOException.class, () -> FileUtil.ensureDirExists(new File(file, "child/grandchild")));
    assertFalse(FileUtil.mkdirs(new File(file, "child")));
  }

  @Test
  void sortFileArrayByModifiedTime() {
    File older = new FixedMtimeFile("older", 1_000);
    File newer = new FixedMtimeFile("newer", 2_000);
    File sameAsNewer = new FixedMtimeFile("same", 2_000);
    File[] files = {newer, older, sameAsNewer};

    FileUtil.sortFilesByModifiedTime(files);
    assertEquals(older, files[0]);

    FileUtil.sortFilesByModifiedTime(files, true);
    assertEquals(older, files[2]);
  }

  @Test
  void deleteMissingFileIsNoOp() {
    assertDoesNotThrow(() -> FileUtil.delete(new File(tempDir, "missing")));
  }

  @Test
  void deleteNonEmptyDirectoryFails() throws IOException {
    populateTempDir();

    assertThrows(IOException.class, () -> FileUtil.delete(tempDir));
  }

  @Test
  void deleteDirOfMissingDirectoryIsNoOp() {
    assertDoesNotThrow(() -> FileUtil.deleteDir(new File(tempDir, "missing")));
  }

  @Test
  void deleteDirContentsOfRegularFileIsNoOp() throws IOException {
    File file = writeFile("file", "content");

    FileUtil.deleteDirContents(file);

    assertTrue(file.exists());
  }

  @Test
  void deleteDirContentsFailsWhenFileCannotBeRemoved() throws IOException {
    File dir = new File(tempDir, "dir");
    assertTrue(dir.mkdir());
    File locked = undeletable(writeFile("dir/locked", "content"));
    File directory =
        new File(dir.getPath()) {
          @Override
          public File[] listFiles() {
            return new File[] {locked};
          }
        };

    assertThrows(IOException.class, () -> FileUtil.deleteDirContents(directory));
  }

  @Test
  void deleteDirFailsWhenDirectoryCannotBeRemoved() {
    File child = undeletable(new File(tempDir, "child"));
    assertTrue(child.mkdir());

    assertThrows(IOException.class, () -> FileUtil.deleteDir(child));
  }

  private static File undeletable(File file) {
    return new File(file.getPath()) {
      @Override
      public boolean delete() {
        return false;
      }
    };
  }

  @Test
  void trimFilenameEdgeCases() {
    assertNull(FileUtil.trimFilename(""));
    assertEquals("file.txt", FileUtil.trimFilename("file.txt"));
    assertEquals("file.txt", FileUtil.trimFilename("scheme:file.txt"));
  }

  @Test
  void renameMovesFile() throws IOException {
    File src = writeFile("src.txt", "moved");
    File dst = new File(tempDir, "dst.txt");

    FileUtil.rename(src, dst);

    assertFalse(src.exists());
    assertEquals("moved", Files.readString(dst.toPath()));
  }

  @Test
  void renameFailsWhenSourceIsMissing() {
    IOException exception = assertThrows(
        IOException.class, () -> FileUtil.rename(new File(tempDir, "missing"), new File(tempDir, "dst")));
    assertTrue(exception.getMessage().contains("doesn't exist"));
  }

  @Test
  void renameFailsWhenDestinationIsNonEmptyDirectory() throws IOException {
    File src = writeFile("src.txt", "content");
    File dst = new File(tempDir, "dst");
    assertTrue(dst.mkdir());
    Files.createFile(new File(dst, "occupied").toPath());

    IOException exception = assertThrows(IOException.class, () -> FileUtil.rename(src, dst));
    assertTrue(exception.getMessage().contains("already exists"));
  }

  @Test
  void renameFailsWhenDestinationDirectoryIsMissing() throws IOException {
    File src = writeFile("src.txt", "content");

    IOException exception =
        assertThrows(IOException.class, () -> FileUtil.rename(src, new File(tempDir, "missing/dst")));
    assertTrue(exception.getMessage().contains("file renaming failed"));
  }

  private File writeFile(String name, String content) throws IOException {
    File file = new File(tempDir, name);
    Files.writeString(file.toPath(), content);
    return file;
  }

  private static class TrackingInputStream extends ByteArrayInputStream {
    boolean closed;

    TrackingInputStream(String content) {
      super(content.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void close() throws IOException {
      closed = true;
      super.close();
    }
  }

  static class FixedMtimeFile extends File {
    private final long lastModified;

    FixedMtimeFile(String name, long lastModified) {
      super(name);
      this.lastModified = lastModified;
    }

    @Override
    public long lastModified() {
      return lastModified;
    }
  }

  /**
   * Overrides {@code lastModified} to return system time. {@code File.lastModified()} may return
   * values in second-level granularity, which causes the unit test to fail.
   */
  static class VirtualFile extends File {
    final long lastModified = System.currentTimeMillis();

    VirtualFile(String name) {
      super(name);
    }

    @Override
    public long lastModified() {
      return lastModified;
    }
  }
}
