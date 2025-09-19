// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.zimbra.common.localconfig.LC;
import java.io.File;
import java.io.IOException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class SpoolingCacheTest {

  private static final String[] STRINGS = new String[] {"foo", "bar", "baz"};
  @TempDir private static File testDirectory;

  @BeforeAll
  public static void init() throws Exception {
    LC.zimbra_tmp_directory.setDefault(testDirectory.getAbsolutePath());
  }

  private void test(SpoolingCache<String> scache, boolean shouldSpool) throws IOException {
    for (String v : STRINGS) {
      scache.add(v);
    }

    assertEquals(shouldSpool, scache.isSpooled(), "spooled");
    assertEquals(STRINGS.length, scache.size(), "entry count matches");
    int i = 0;
    for (String v : scache) {
      assertEquals(STRINGS[i++], v, "entry matched: #" + i);
    }
    assertEquals(STRINGS.length, i, "correct number of items iterated");
  }

  @Test
  void memory() throws Exception {
    SpoolingCache<String> scache = new SpoolingCache<String>(STRINGS.length + 3);
    test(scache, false);
    scache.cleanup();
  }

  @Test
  void disk() throws Exception {
    SpoolingCache<String> scache = new SpoolingCache<String>(0);
    test(scache, true);
    scache.cleanup();
  }

  @Test
  void both() throws Exception {
    SpoolingCache<String> scache = new SpoolingCache<String>(1);
    test(scache, true);
    scache.cleanup();
  }
}
