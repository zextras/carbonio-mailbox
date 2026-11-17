// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;


import org.junit.jupiter.api.Test;

public class SystemUtilTest {

  @Test
  void coalesce() {
    assertEquals(1, (int) SystemUtil.coalesce(null, 1));
    assertNull(SystemUtil.coalesce(null, null, null, null));
    assertEquals(2, (int) SystemUtil.coalesce(2, 3));
    assertEquals("good", SystemUtil.coalesce("good", "bad", "ugly"));
    assertNull(SystemUtil.coalesce((Object[]) null));
  }

  @Test
  void getStackTraceOfCurrentThread() {
    assertTrue(SystemUtil.getStackTrace().contains("getStackTraceOfCurrentThread"));
  }

  @Test
  void getStackTraceOfThrowable() {
    String trace = SystemUtil.getStackTrace(new IllegalStateException("boom"));
    assertTrue(trace.startsWith("java.lang.IllegalStateException: boom"));
  }

  @Test
  void getInnermostException() {
    IllegalStateException root = new IllegalStateException("root");
    RuntimeException wrapper = new RuntimeException(new IllegalArgumentException(root));

    assertSame(root, SystemUtil.getInnermostException(wrapper));
    assertSame(root, SystemUtil.getInnermostException(root));
    assertNull(SystemUtil.getInnermostException(null));
  }

  @Test
  void getProductVersionReadsPackageManifest() {
    assertDoesNotThrow(SystemUtil::getProductVersion);
  }

  @Test
  void onWindowsMatchesOsName() {
    assertEquals(System.getProperty("os.name").toLowerCase().startsWith("win"), SystemUtil.ON_WINDOWS);
  }
}
