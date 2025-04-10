// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.util;

import static org.junit.jupiter.api.Assertions.*;

import com.google.common.collect.Lists;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class FileUtilTest {

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
