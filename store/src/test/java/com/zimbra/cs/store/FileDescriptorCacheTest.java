// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.store;

import static org.junit.jupiter.api.Assertions.*;

import com.zimbra.common.util.ByteUtil;
import com.zimbra.common.util.FileCache;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class FileDescriptorCacheTest {

  private static final String NAME_PREFIX = FileDescriptorCacheTest.class.getSimpleName();

  @TempDir private File uncompressedDir;

  @Test
  void testUncompressedCache() throws Exception {
    FileCache<String> uc = FileCache.Builder.createWithStringKey(uncompressedDir, false).build();
    FileDescriptorCache fdc = new FileDescriptorCache(uc);
    fdc.setMaxSize(2);

    File file1 = File.createTempFile(NAME_PREFIX, ".tmp.gz");
    File file2 = File.createTempFile(NAME_PREFIX, ".tmp.gz");
    File file3 = File.createTempFile(NAME_PREFIX, ".tmp.gz");
    File file4 = File.createTempFile(NAME_PREFIX, ".tmp.gz");

    String content1 = "Tempted";
    String content2 = "Tempted";
    String content3 = "Black Coffee In Bed";
    String content4 = "Pulling Mussels";

    String digest1 = ByteUtil.getDigest(content1.getBytes());
    String digest3 = ByteUtil.getDigest(content3.getBytes());
    String digest4 = ByteUtil.getDigest(content4.getBytes());

    write(file1, content1);
    write(file2, content2);
    write(file3, content3);
    write(file4, content4);

    assertFalse(uc.containsDigest(digest1));
    assertFalse(uc.containsDigest(digest3));
    assertFalse(uc.containsDigest(digest4));

    assertEquals(0, fdc.getSize());
    byte[] buf = new byte[10];

    fdc.read(file1.getPath(), content1.length(), 0, buf, 0, buf.length);
    assertTrue(uc.containsDigest(digest1));
    assertFalse(uc.containsDigest(digest3));
    assertFalse(uc.containsDigest(digest4));
    assertEquals(1, fdc.getSize());

    fdc.read(file2.getPath(), content2.length(), 0, buf, 0, buf.length);
    assertTrue(uc.containsDigest(digest1));
    assertFalse(uc.containsDigest(digest3));
    assertFalse(uc.containsDigest(digest4));
    assertEquals(2, fdc.getSize());

    fdc.read(file3.getPath(), content3.length(), 0, buf, 0, buf.length);
    assertTrue(uc.containsDigest(digest1));
    assertTrue(uc.containsDigest(digest3));
    assertFalse(uc.containsDigest(digest4));
    assertEquals(2, fdc.getSize());

    fdc.read(file4.getPath(), content4.length(), 0, buf, 0, buf.length);
    assertFalse(uc.containsDigest(digest1));
    assertTrue(uc.containsDigest(digest3));
    assertTrue(uc.containsDigest(digest4));
    assertEquals(2, fdc.getSize());
  }

  private void write(File file, String content) throws IOException {
    OutputStream out = new GZIPOutputStream(new FileOutputStream(file));
    out.write(content.getBytes());
    out.close();
  }
}
