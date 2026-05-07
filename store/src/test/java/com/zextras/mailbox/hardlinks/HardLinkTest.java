// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zextras.mailbox.hardlinks;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HardLinkTest {

  @TempDir
  Path tempDir;

  @Test
  void link_createsHardLink() throws IOException {
    Path src = tempDir.resolve("source.txt");
    Files.writeString(src, "hello");

    String destPath = tempDir.resolve("dest.txt").toString();
    HardLink.link(src.toString(), destPath);

    assertEquals("hello", Files.readString(Path.of(destPath)));
    // Hard link means same inode
    assertEquals(
            Files.getAttribute(src, "unix:ino"),
            Files.getAttribute(Path.of(destPath), "unix:ino"));
  }

  @Test
  void link_createsHardLink_withPath() throws IOException {
    Path src = tempDir.resolve("source.txt");
    Files.writeString(src, "hello");

    String destPath = tempDir.resolve("dest.txt").toString();
    HardLink.link(Path.of(src.toString()), Path.of(destPath));

    assertEquals("hello", Files.readString(Path.of(destPath)));
    // Hard link means same inode
    assertEquals(
            Files.getAttribute(src, "unix:ino"),
            Files.getAttribute(Path.of(destPath), "unix:ino"));
  }
}
