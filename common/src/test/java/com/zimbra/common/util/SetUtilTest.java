// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

class SetUtilTest {

  private final Set<Integer> lhs = Set.of(1, 2, 3);
  private final Set<Integer> rhs = Set.of(2, 3, 4);

  @Test
  void subtract() {
    assertEquals(Set.of(1), SetUtil.subtract(lhs, rhs));
  }

  @Test
  void intersect() {
    assertEquals(Set.of(2, 3), SetUtil.intersect(lhs, rhs));
  }

  @Test
  void intersectIntoProvidedSet() {
    Set<Integer> out = new HashSet<>(Set.of(9));
    assertSame(out, SetUtil.intersect(out, lhs, rhs));
    assertEquals(Set.of(2, 3, 9), out);
  }

  @Test
  void unionIntoProvidedSet() {
    Set<Integer> out = new HashSet<>(Set.of(9));
    assertSame(out, SetUtil.union(out, lhs, rhs));
    assertEquals(Set.of(1, 2, 3, 4, 9), out);
  }

  @Test
  void unionIntoLeftHandSide() {
    Set<Integer> left = new HashSet<>(lhs);
    assertSame(left, SetUtil.union(left, rhs));
    assertEquals(Set.of(1, 2, 3, 4), left);
  }
}
