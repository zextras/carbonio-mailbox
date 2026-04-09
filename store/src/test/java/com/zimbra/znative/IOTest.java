// SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.znative;

import static org.junit.jupiter.api.Assertions.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class IOTest {

  @TempDir Path tempDir;

  @Test
  void link_createsHardLink() throws IOException {
    Path src = tempDir.resolve("source.txt");
    Files.writeString(src, "hello");

    String destPath = tempDir.resolve("dest.txt").toString();
    IO.link(src.toString(), destPath);

    assertEquals("hello", Files.readString(Path.of(destPath)));
    // Hard link means same inode
    assertEquals(
        Files.getAttribute(src, "unix:ino"),
        Files.getAttribute(Path.of(destPath), "unix:ino"));
  }

  @Test
  void link_throwsFileNotFoundForMissingSrc() {
    String missing = tempDir.resolve("nonexistent").toString();
    String dest = tempDir.resolve("dest").toString();

    assertThrows(FileNotFoundException.class, () -> IO.link(missing, dest));
  }

  @Test
  void link_throwsIOExceptionForExistingDest() throws IOException {
    Path src = tempDir.resolve("src.txt");
    Path dest = tempDir.resolve("dest.txt");
    Files.writeString(src, "a");
    Files.writeString(dest, "b");

    assertThrows(IOException.class, () -> IO.link(src.toString(), dest.toString()));
  }

  @Test
  void fileInfo_returnsInodeSizeAndLinkCount() throws IOException {
    Path file = tempDir.resolve("test.txt");
    Files.writeString(file, "content");

    IO.FileInfo info = IO.fileInfo(file.toString());

    assertNotNull(info);
    assertTrue(info.getInodeNum() > 0);
    assertEquals(7, info.getSize()); // "content" is 7 bytes
    assertEquals(1, info.getLinkCount());
  }

  @Test
  void fileInfo_linkCountIncreasesAfterHardLink() throws IOException {
    Path file = tempDir.resolve("original.txt");
    Files.writeString(file, "data");

    IO.FileInfo before = IO.fileInfo(file.toString());
    assertEquals(1, before.getLinkCount());

    IO.link(file.toString(), tempDir.resolve("linked.txt").toString());

    IO.FileInfo after = IO.fileInfo(file.toString());
    assertEquals(2, after.getLinkCount());
    assertEquals(before.getInodeNum(), after.getInodeNum());
  }

  @Test
  void fileInfo_throwsFileNotFoundForMissingPath() {
    String missing = tempDir.resolve("nonexistent").toString();
    assertThrows(FileNotFoundException.class, () -> IO.fileInfo(missing));
  }

  @Test
  void linkCount_returnsCorrectCount() throws IOException {
    Path file = tempDir.resolve("file.txt");
    Files.writeString(file, "x");

    assertEquals(1, IO.linkCount(file.toString()));

    IO.link(file.toString(), tempDir.resolve("link1.txt").toString());
    assertEquals(2, IO.linkCount(file.toString()));

    IO.link(file.toString(), tempDir.resolve("link2.txt").toString());
    assertEquals(3, IO.linkCount(file.toString()));
  }

  @Test
  void chmod_setsPermissions() throws IOException {
    Path file = tempDir.resolve("perms.txt");
    Files.writeString(file, "test");

    IO.chmod(file.toString(), IO.S_IRUSR | IO.S_IWUSR);

    var perms = Files.getPosixFilePermissions(file);
    assertTrue(perms.contains(PosixFilePermission.OWNER_READ));
    assertTrue(perms.contains(PosixFilePermission.OWNER_WRITE));
    assertFalse(perms.contains(PosixFilePermission.OWNER_EXECUTE));
    assertFalse(perms.contains(PosixFilePermission.GROUP_READ));
    assertFalse(perms.contains(PosixFilePermission.OTHERS_READ));
  }

  @Test
  void chmod_throwsFileNotFoundForMissingPath() {
    String missing = tempDir.resolve("nonexistent").toString();
    assertThrows(FileNotFoundException.class, () -> IO.chmod(missing, IO.S_IRUSR));
  }

  // setStdoutStderrTo() tests are intentionally excluded:
  // dup2() redirects stdout/stderr for the entire JVM process, which breaks
  // surefire's communication pipe and causes "forked VM terminated" errors.
  // This function is only used by MailboxServer output rotation and is tested
  // in production via systemd service startup.

  @Test
  void permissionConstants_haveCorrectValues() {
    assertEquals(0400, IO.S_IRUSR);
    assertEquals(0200, IO.S_IWUSR);
    assertEquals(0100, IO.S_IXUSR);
    assertEquals(0040, IO.S_IRGRP);
    assertEquals(0020, IO.S_IWGRP);
    assertEquals(0010, IO.S_IXGRP);
    assertEquals(0004, IO.S_IROTH);
    assertEquals(0002, IO.S_IWOTH);
    assertEquals(0001, IO.S_IXOTH);
    assertEquals(04000, IO.S_ISUID);
    assertEquals(02000, IO.S_ISGID);
    assertEquals(01000, IO.S_ISVTX);
  }
}
