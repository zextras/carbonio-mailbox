// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class ArrayUtilTest {

  @Test
  void reverseObjectsInPlace() {
    String[] array = {"a", "b", "c"};
    assertSame(array, ArrayUtil.reverse(array));
    assertArrayEquals(new String[] {"c", "b", "a"}, array);
  }

  @Test
  void reverseInts() {
    assertArrayEquals(new int[] {4, 3, 2, 1}, ArrayUtil.reverse(new int[] {1, 2, 3, 4}));
  }

  @Test
  void reverseBytes() {
    assertArrayEquals(new byte[] {3, 2, 1}, ArrayUtil.reverse(new byte[] {1, 2, 3}));
  }

  @Test
  void reverseChars() {
    assertArrayEquals(new char[] {'b', 'a'}, ArrayUtil.reverse(new char[] {'a', 'b'}));
  }

  @Test
  void reverseLeavesNullAndSingleElementArrays() {
    assertNull(ArrayUtil.reverse((String[]) null));
    assertNull(ArrayUtil.reverse((int[]) null));
    assertNull(ArrayUtil.reverse((byte[]) null));
    assertNull(ArrayUtil.reverse((char[]) null));
    assertArrayEquals(new String[] {"a"}, ArrayUtil.reverse(new String[] {"a"}));
    assertArrayEquals(new int[] {1}, ArrayUtil.reverse(new int[] {1}));
    assertArrayEquals(new byte[] {1}, ArrayUtil.reverse(new byte[] {1}));
    assertArrayEquals(new char[] {'a'}, ArrayUtil.reverse(new char[] {'a'}));
  }

  @Test
  void toByteArray() {
    assertArrayEquals(new byte[] {1, 2}, ArrayUtil.toByteArray(List.of((byte) 1, (byte) 2)));
  }

  @Test
  void toIntArray() {
    assertArrayEquals(new int[] {1, 2}, ArrayUtil.toIntArray(List.of(1, 2)));
  }

  @Test
  void isEmpty() {
    assertTrue(ArrayUtil.isEmpty(null));
    assertTrue(ArrayUtil.isEmpty(new Object[0]));
    assertFalse(ArrayUtil.isEmpty(new Object[] {1}));
  }

  @Test
  void byteArrayContains() {
    ArrayUtil arrayUtil = new ArrayUtil();
    assertTrue(arrayUtil.byteArrayContains(new byte[] {1, 2, 3}, (byte) 1));
    assertFalse(arrayUtil.byteArrayContains(new byte[] {1, 2, 3}, (byte) 4));
  }

  @Test
  void getFirstElement() {
    assertNull(ArrayUtil.getFirstElement(null));
    assertNull(ArrayUtil.getFirstElement(new String[0]));
    assertEquals("a", ArrayUtil.getFirstElement(new String[] {"a", "b"}));
  }
}
