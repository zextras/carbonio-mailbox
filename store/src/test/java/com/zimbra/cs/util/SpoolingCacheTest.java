// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.util;

import com.zimbra.common.localconfig.LC;
import java.io.File;
import java.io.IOException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class SpoolingCacheTest {

  @BeforeClass
  public static void init() throws Exception {
    new File("build/test").mkdirs();
    LC.zimbra_tmp_directory.setDefault("build/test");
  }

  @AfterClass
  public static void destroy() throws Exception {
    new File("build/test").delete();
  }

  private static final String[] STRINGS = new String[] {"foo", "bar", "baz"};

  private void test(SpoolingCache<String> scache, boolean shouldSpool) throws IOException {
    for (String v : STRINGS) {
      scache.add(v);
    }

    Assert.assertEquals("spooled", shouldSpool, scache.isSpooled());
    Assert.assertEquals("entry count matches", STRINGS.length, scache.size());
    int i = 0;
    for (String v : scache) {
      Assert.assertEquals("entry matched: #" + i, STRINGS[i++], v);
    }
    Assert.assertEquals("correct number of items iterated", STRINGS.length, i);
  }

  @Test
  public void memory() throws Exception {
    SpoolingCache<String> scache = new SpoolingCache<String>(STRINGS.length + 3);
    test(scache, false);
    scache.cleanup();
  }

  @Test
  public void disk() throws Exception {
    SpoolingCache<String> scache = new SpoolingCache<String>(0);
    test(scache, true);
    scache.cleanup();
  }

  @Test
  public void both() throws Exception {
    SpoolingCache<String> scache = new SpoolingCache<String>(1);
    test(scache, true);
    scache.cleanup();
  }
}
