// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import com.google.common.primitives.Ints;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Test;

class ArrayUtilTest {

  @Test
  void toIntArray() {
      assertArrayEquals(new int[] {1, 2}, Ints.toArray(List.of(1, 2)));
  }

  @Test
  void isEmpty() {
      assertTrue(ArrayUtils.isEmpty((Object[]) null));
      assertTrue(ArrayUtils.isEmpty(new Object[0]));
      assertFalse(ArrayUtils.isEmpty(new Object[]{1}));
  }

  @Test
  void getFirstElement() {
    assertNull(ArrayUtil.getFirstElement(null));
    assertNull(ArrayUtil.getFirstElement(new String[0]));
    assertEquals("a", ArrayUtil.getFirstElement(new String[] {"a", "b"}));
  }
}
