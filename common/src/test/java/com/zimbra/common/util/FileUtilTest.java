// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.util;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.google.common.collect.Lists;
import com.google.common.io.Files;

public class FileUtilTest {

    private List<File> tempPaths = Lists.newArrayList();

    /**
     * Overrides {@code lastModified} to return system time.  {@code File.lastModified()} may
     * return values in second-level granularity, which causes the unit test to fail.
     */
    @SuppressWarnings("serial")
    class VirtualFile extends File {
        final long lastModified = System.currentTimeMillis();

        VirtualFile(String name) {
            super(name);
        }

        @Override
        public long lastModified() {
            return lastModified;
        }
    }

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
    File tempDir = createTempDir();
    FileUtil.deleteDir(tempDir);
    assertFalse(tempDir.exists());
  }

  @Test
  void deleteDirContents() throws IOException {
    File tempDir = createTempDir();
    FileUtil.deleteDirContents(tempDir);
    assertTrue(tempDir.exists());
    assertEquals(0, tempDir.listFiles().length);
  }

    private File createTempDir() throws IOException {
        File tempDir = Files.createTempDir();
        tempPaths.add(tempDir);
        File.createTempFile("FileUtilTest", null, tempDir);
        File childDir = new File(tempDir, "child");
        assertTrue(childDir.mkdir());
        File.createTempFile("FileUtilTest", null, childDir);
        return tempDir;
    }

    @AfterEach
    public void tearDown() throws IOException {
        for (File file : tempPaths) {
            if (file.isFile()) {
                file.delete();
            } else {
                FileUtil.deleteDir(file);
            }
        }
    }
}
