// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;

import com.google.common.collect.Lists;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
  void compressPropagatesFailureWritingFinalGzipBytes() throws IOException {
    File source = new File(tempDir, "source.txt");
    File destination = new File(tempDir, "destination.gz");
    Files.writeString(source.toPath(), "mail content");
    Files.createFile(destination.toPath());
    try (var outputs = mockConstruction(FileOutputStream.class, (output, context) ->
        doAnswer(invocation -> {
          throw new IOException("final gzip write failed");
        }).when(output).write(any(byte[].class), anyInt(), anyInt()))) {
      IOException failure =
          assertThrows(IOException.class, () -> FileUtil.compress(source, destination, false));
      assertEquals("final gzip write failed", failure.getMessage());
      // The header write succeeds. The failure is in the later deflater write.
      verify(outputs.constructed().get(0)).write(any(byte[].class));
    }
    assertFalse(destination.exists());
    assertEquals("mail content", Files.readString(source.toPath()));
  }

  @Test
  void compressProducesReadableGzipWithSyncEnabled() throws IOException {
    File source = new File(tempDir, "source.txt");
    File destination = new File(tempDir, "destination.gz");
    byte[] content = "message data".repeat(100).getBytes(StandardCharsets.UTF_8);
    Files.write(source.toPath(), content);

    FileUtil.compress(source, destination, true);

    try (var input = new GZIPInputStream(Files.newInputStream(destination.toPath()))) {
      assertArrayEquals(content, input.readAllBytes());
    }
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
  void listDirsRecursivelyReturnsAllDirectories() {
    File subDir1 = new File(tempDir, "subDir1");
    File subDir2 = new File(subDir1, "subDir2");
    assertTrue(subDir1.mkdir());
    assertTrue(subDir2.mkdir());

    List<File> dirs = FileUtil.listDirsRecursively(tempDir);

    assertEquals(3, dirs.size());
    assertTrue(dirs.contains(tempDir));
    assertTrue(dirs.contains(subDir1));
    assertTrue(dirs.contains(subDir2));
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
