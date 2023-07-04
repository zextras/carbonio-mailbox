// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


import org.junit.jupiter.api.Test;

public class SystemUtilTest {

  @Test
  void coalesce() {
    assertEquals(1, (int) SystemUtil.coalesce(null, 1));
    assertNull(SystemUtil.coalesce(null, null, null, null));
    assertEquals(2, (int) SystemUtil.coalesce(2, 3));
    assertEquals("good", SystemUtil.coalesce("good", "bad", "ugly"));
  }
}
